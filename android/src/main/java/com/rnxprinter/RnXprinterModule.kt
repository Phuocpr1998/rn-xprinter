package com.rnxprinter

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Arguments
import net.posprinter.CPCLConst
import net.posprinter.CPCLPrinter
import net.posprinter.IDeviceConnection
import net.posprinter.POSConnect
import net.posprinter.POSPrinter
import net.posprinter.TSPLConst
import net.posprinter.TSPLPrinter
import net.posprinter.ZPLConst
import net.posprinter.ZPLPrinter
import net.posprinter.model.AlgorithmType
import java.util.concurrent.ConcurrentHashMap

data class PrinterInstance(
  var connection: IDeviceConnection? = null,
  var posPrinter: POSPrinter? = null,
  var cpclPrinter: CPCLPrinter? = null,
  var zplPrinter: ZPLPrinter? = null,
  var tsplPrinter: TSPLPrinter? = null
)

class RnXprinterModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  companion object {
    const val NAME = "RnXprinter"
    private const val ACTION_USB_PERMISSION = "com.rnxprinter.USB_PERMISSION"
  }

  // Store instances by instance ID
  private val instances = ConcurrentHashMap<String, PrinterInstance>()

  // For backward compatibility with legacy calls
  private val legacyInstance = PrinterInstance()

  // USB permission handling
  private val usbManager = reactContext.getSystemService(Context.USB_SERVICE) as UsbManager
  private var pendingUsbPromise: Promise? = null
  private var pendingInstanceId: String = "legacy"

  // USB permission receiver
  private val usbReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val action = intent.action
      if (ACTION_USB_PERMISSION == action) {
        synchronized(this) {
          val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
          if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            Log.d("XPrinterModule", "USB permission granted for device: ${device?.deviceName}")
            pendingUsbPromise?.let { promise ->
              connectToUsbDeviceWithInstance(pendingInstanceId, device?.deviceName ?: "", promise)
            }
          } else {
            Log.e("XPrinterModule", "USB permission denied for device: ${device?.deviceName}")
            pendingUsbPromise?.reject("-1", "USB permission denied")
          }
          pendingUsbPromise = null
          pendingInstanceId = "legacy"
        }
      }
    }
  }

  init {
    // Register USB permission receiver
    val filter = IntentFilter(ACTION_USB_PERMISSION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      reactContext.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
    } else {
      ContextCompat.registerReceiver(
        reactContext,
        usbReceiver,
        filter,
        ContextCompat.RECEIVER_NOT_EXPORTED
      )
    }
  }

  @ReactMethod
  fun createInstance(instanceId: String) {
    instances[instanceId] = PrinterInstance()
    Log.d("XPrinterModule", "Created instance: $instanceId")
  }

  @ReactMethod
  fun destroyInstance(instanceId: String) {
    val instance = instances[instanceId]
    instance?.connection?.close()
    instances.remove(instanceId)
    Log.d("XPrinterModule", "Destroyed instance: $instanceId")
  }

  private fun getInstance(instanceId: String): PrinterInstance {
    return if (instanceId == "legacy") {
      legacyInstance
    } else {
      instances[instanceId] ?: throw Exception("Instance $instanceId not found. Call createInstance first.")
    }
  }



  @ReactMethod
  fun getUsbDevices(promise: Promise) {
    try {
      // Use both methods: original printer library and system USB manager
      val listDevice = POSConnect.getUsbDevices(reactApplicationContext)
      val writableArray = Arguments.createArray()

      if (listDevice != null) {
        for (device in listDevice) {
          writableArray.pushString(device)
        }
      } else {
        // Fallback to system USB manager if printer library fails
        val deviceList = usbManager.deviceList
        for (device in deviceList.values) {
          writableArray.pushString(device.deviceName)
        }
      }

      promise.resolve(writableArray)
    } catch (ex: Exception) {
      Log.e("XPrinterModule", "getUsbDevices failed --> ${ex.message}")
      promise.reject("-1", ex.message)
    }
  }

  @ReactMethod
  fun getSerialDevices(promise: Promise) {
    try {
      val listSerial = POSConnect.getSerialPort()
      val writableArray = Arguments.createArray()
      if (listSerial != null) {
        for (device in listSerial) {
          writableArray.pushString(device)
        }
      }
      promise.resolve(writableArray)
    } catch (ex: Exception) {
      Log.e("XPrinterModule", "getSerialDevices failed --> ${ex.message}")
      promise.reject("-1", ex.message)
    }
  }

  @ReactMethod
  fun serialConnect(instanceId: String, serialPort: String, promise: Promise) {
    val instance = getInstance(instanceId)
    instance.connection?.close()

    try {
      instance.connection = POSConnect.createDevice(POSConnect.DEVICE_TYPE_SERIAL)
      instance.connection!!.connect(serialPort) { code, connInfo, msg ->
        when (code) {
          POSConnect.CONNECT_SUCCESS -> {
            instance.posPrinter = POSPrinter(instance.connection)
            instance.cpclPrinter = CPCLPrinter(instance.connection)
            instance.zplPrinter = ZPLPrinter(instance.connection)
            instance.tsplPrinter = TSPLPrinter(instance.connection)
            promise.resolve(connInfo)
          }

          POSConnect.CONNECT_FAIL -> {
            Log.e("XPrinterModule", "connectListener.CONNECT_FAIL --> $msg")
            promise.reject(code.toString(), msg)
          }

          POSConnect.CONNECT_INTERRUPT -> {
            Log.e("XPrinterModule", "connectListener.CONNECT_INTERRUPT --> $msg")
            promise.reject(code.toString(), msg)
          }

          POSConnect.SEND_FAIL -> {
            Log.e("XPrinterModule", "connectListener.SEND_FAIL --> $msg")
            promise.reject(code.toString(), msg)
          }

          POSConnect.USB_DETACHED -> {
            Log.e("XPrinterModule", "connectListener.USB_DETACHED --> $msg")
            promise.reject(code.toString(), msg)
          }

          POSConnect.USB_ATTACHED -> {
            Log.e("XPrinterModule", "connectListener.USB_ATTACHED --> $msg")
            promise.reject(code.toString(), msg)
          }
        }
      }
    } catch (ex: Exception) {
      Log.e("XPrinterModule", "serialConnect failed --> ${ex.message}")
      promise.reject("-1", ex.message)
    }
  }

  @ReactMethod
  fun usbConnect(instanceId: String, device: String, promise: Promise) {
    try {
      // First, check if we can find the USB device in the system
      val deviceList = usbManager.deviceList
      var targetDevice: UsbDevice? = null

      // Find the device by name
      for (usbDevice in deviceList.values) {
        if (usbDevice.deviceName == device || device.contains(usbDevice.deviceName)) {
          targetDevice = usbDevice
          break
        }
      }

      if (targetDevice != null) {
        // Check/request USB permission before connecting
        if (usbManager.hasPermission(targetDevice)) {
          // Permission already granted, connect directly
          connectToUsbDeviceWithInstance(instanceId, device, promise)
        } else {
          // Request permission first
          requestUsbPermissionForInstance(targetDevice, instanceId, promise)
        }
      } else {
        // Fallback to original method if device not found in system
        Log.w("XPrinterModule", "Device not found in system USB list, trying original method")
        connectToUsbDeviceWithInstance(instanceId, device, promise)
      }
    } catch (ex: Exception) {
      Log.e("XPrinterModule", "usbConnect failed --> ${ex.message}")
      // Fallback to original method on error
      connectToUsbDeviceWithInstance(instanceId, device, promise)
    }
  }

  private fun connectToUsbDeviceWithInstance(instanceId: String, device: String, promise: Promise) {
    val instance = getInstance(instanceId)
    instance.connection?.close()

    try {
      instance.connection = POSConnect.createDevice(POSConnect.DEVICE_TYPE_USB)
      instance.connection!!.connect(device) { code, connInfo, msg ->
        when (code) {
          POSConnect.CONNECT_SUCCESS -> {
            instance.posPrinter = POSPrinter(instance.connection)
            instance.cpclPrinter = CPCLPrinter(instance.connection)
            instance.zplPrinter = ZPLPrinter(instance.connection)
            instance.tsplPrinter = TSPLPrinter(instance.connection)
            promise.resolve(connInfo)
          }

          POSConnect.CONNECT_FAIL -> {
            Log.e("XPrinterModule", "connectListener.CONNECT_FAIL --> $msg")
            promise.reject(code.toString(), msg)
          }

          POSConnect.CONNECT_INTERRUPT -> {
            Log.e("XPrinterModule", "connectListener.CONNECT_INTERRUPT --> $msg")
            promise.reject(code.toString(), msg)
          }

          POSConnect.SEND_FAIL -> {
            Log.e("XPrinterModule", "connectListener.SEND_FAIL --> $msg")
            promise.reject(code.toString(), msg)
          }

          POSConnect.USB_DETACHED -> {
            Log.e("XPrinterModule", "connectListener.USB_DETACHED --> $msg")
            promise.reject(code.toString(), msg)
          }

          POSConnect.USB_ATTACHED -> {
            Log.e("XPrinterModule", "connectListener.USB_ATTACHED --> $msg")
            promise.reject(code.toString(), msg)
          }
        }
      }
    } catch (ex: Exception) {
      Log.e("XPrinterModule", "usbConnect failed --> ${ex.message}")
      promise.reject("-1", ex.message)
    }
  }

  private fun requestUsbPermissionForInstance(device: UsbDevice, instanceId: String, promise: Promise) {
    if (usbManager.hasPermission(device)) {
      // Permission already granted
      connectToUsbDeviceWithInstance(instanceId, device.deviceName, promise)
    } else {
      // Request permission
      pendingUsbPromise = promise
      pendingInstanceId = instanceId
      val permissionIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Create explicit intent for Android 14 compatibility
        val explicitIntent = Intent(ACTION_USB_PERMISSION).apply {
          setPackage(reactApplicationContext.packageName)
        }
        PendingIntent.getBroadcast(
          reactApplicationContext,
          0,
          explicitIntent,
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
      } else {
        PendingIntent.getBroadcast(
          reactApplicationContext,
          0,
          Intent(ACTION_USB_PERMISSION),
          PendingIntent.FLAG_IMMUTABLE,
        )
      }
      usbManager.requestPermission(device, permissionIntent)
    }
  }

  @ReactMethod
  fun netConnect(instanceId: String, ip: String, promise: Promise) {
    val instance = getInstance(instanceId)
    instance.connection?.close()

    try {
      instance.connection = POSConnect.createDevice(POSConnect.DEVICE_TYPE_ETHERNET)
      instance.connection!!.connect(ip) { code, connInfo, msg ->
        when (code) {
          POSConnect.CONNECT_SUCCESS -> {
            Log.e("XPrinterModule", "connectListener.CONNECT_SUCCESS --> $connInfo")
            instance.posPrinter = POSPrinter(instance.connection)
            instance.cpclPrinter = CPCLPrinter(instance.connection)
            instance.zplPrinter = ZPLPrinter(instance.connection)
            instance.tsplPrinter = TSPLPrinter(instance.connection)
            promise.resolve(connInfo)
          }

          POSConnect.CONNECT_FAIL -> {
            Log.e("XPrinterModule", "connectListener.CONNECT_FAIL --> $msg")
            promise.reject(code.toString(), msg)
          }

          POSConnect.CONNECT_INTERRUPT -> {
            Log.e("XPrinterModule", "connectListener.CONNECT_INTERRUPT --> $msg")
            promise.reject(code.toString(), msg)
          }

          POSConnect.SEND_FAIL -> {
            Log.e("XPrinterModule", "connectListener.SEND_FAIL --> $msg")
            promise.reject(code.toString(), msg)
          }

          POSConnect.USB_DETACHED -> {
            Log.e("XPrinterModule", "connectListener.USB_DETACHED --> $msg")
            promise.reject(code.toString(), msg)
          }

          POSConnect.USB_ATTACHED -> {
            Log.e("XPrinterModule", "connectListener.USB_ATTACHED --> $msg")
            promise.reject(code.toString(), msg)
          }
        }
      }
    } catch (ex: Exception) {
      Log.e("XPrinterModule", "netConnect failed --> ${ex.message}")
      promise.reject("-1", ex.message)
    }
  }

  @ReactMethod
  fun printQRCode(instanceId: String, content: String) {
    val instance = getInstance(instanceId)
    val printer = instance.posPrinter ?: throw Exception("No printer connection for instance $instanceId")

    printer.initializePrinter()
      .printQRCode(content)
      .feedLine()
      .cutHalfAndFeed(1)
  }

  @ReactMethod
  fun printText(instanceId: String, content: String) {
    val instance = getInstance(instanceId)
    val printer = instance.posPrinter ?: throw Exception("No printer connection for instance $instanceId")

    printer.initializePrinter()
      .printString(content)
      .cutHalfAndFeed(1)
  }

  @ReactMethod
  fun tsplPrintTest(instanceId: String) {
    val instance = getInstance(instanceId)
    val tsplPrinter = instance.tsplPrinter ?: throw Exception("No TSPL printer connection for instance $instanceId")

    tsplPrinter.sizeMm(60.0, 30.0)
      .gapInch(0.0, 0.0)
      .offsetInch(0.0)
      .speed(5.0)
      .density(10)
      .direction(TSPLConst.DIRECTION_FORWARD)
      .reference(20, 0)
      .cls()
      .box(6, 6, 378, 229, 5)
      .box(16, 16, 360, 209, 5)
      .barcode(30, 30, TSPLConst.CODE_TYPE_93, 100, TSPLConst.READABLE_LEFT, TSPLConst.ROTATION_0, 2, 2, "ABCDEFGH")
      .qrcode(265, 30, TSPLConst.EC_LEVEL_H, 4, TSPLConst.QRCODE_MODE_MANUAL, TSPLConst.ROTATION_0, "test qrcode")
      .text(200, 144, TSPLConst.FNT_16_24, TSPLConst.ROTATION_0, 1, 1, "Test EN")
      .text(38, 165, TSPLConst.FNT_16_24, TSPLConst.ROTATION_0, 1, 2, "HELLO")
      .bar(200, 183, 166, 30)
      .bar(334, 145, 30, 30)
      .print(1)
  }

  @ReactMethod
  fun zplPrintTest(instanceId: String) {
    val instance = getInstance(instanceId)
    val zplPrinter = instance.zplPrinter ?: throw Exception("No ZPL printer connection for instance $instanceId")

    zplPrinter.setCharSet("UTF-8")
    zplPrinter.addStart()
      .setCustomFont("LZHONGHEI.TTF", '1', ZPLConst.CODE_PAGE_UTF8)
      .addText(0, 0, '1', 24, 24, "custom Font")
      .addText(100, 100, '1', ZPLConst.ROTATION_90, 24, 24, "customFont 90")
      .addEnd()
  }

  @ReactMethod
  fun cpclPrintTest(instanceId: String) {
    val instance = getInstance(instanceId)
    val cpclPrinter = instance.cpclPrinter ?: throw Exception("No CPCL printer connection for instance $instanceId")

    cpclPrinter.initializePrinter(800)
      .addBarcodeText()
      .addText(0, 0, "Code 128")
      .addBarcode(0, 30, CPCLConst.BCS_128, 1, CPCLConst.BCS_RATIO_1, 50, "123456789")
      .addText(0, 120, "UPC-E")
      .addBarcode(0, 150, CPCLConst.BCS_UPCE, 50, "223456")
      .addText(0, 240, "EAN/JAN-13")
      .addBarcode(0, 270, CPCLConst.BCS_EAN13, 50, "323456791234")
      .addText(0, 360, "Code 39")
      .addBarcode(0, 390, CPCLConst.BCS_39, 50, "72233445")
      .addText(250, 0, "UPC-A")
      .addBarcode(250, 30, CPCLConst.BCS_UPCA, 50, "423456789012")
      .addText(250, 120, "EAN/JAN-8")
      .addBarcode(250, 150, CPCLConst.BCS_EAN8, 50, "52233445")
      .addText(300, 360, "CODABAR")
      .addBarcodeV(300, 540, CPCLConst.BCS_CODABAR, 50, "A67859B")
      .addText(0, 480, "Code 93/Ext.93")
      .addBarcode(0, 510, CPCLConst.BCS_93, 50, "823456789")
      .addBarcodeTextOff()
      .addPrint()
  }

  @ReactMethod
  fun printPageModelData(instanceId: String) {
    val instance = getInstance(instanceId)
    val printer = instance.posPrinter ?: throw Exception("No printer connection for instance $instanceId")

    printer.initializePrinter()
      .printPageModelData()
      .cutHalfAndFeed(1)
  }

  @ReactMethod
  fun setCharSet(instanceId: String, charSet: String) {
    val instance = getInstance(instanceId)
    val printer = instance.posPrinter ?: throw Exception("No printer connection for instance $instanceId")

    printer.initializePrinter()
      .setCharSet(charSet)
  }

  @ReactMethod
  fun printBarcode(instanceId: String, data: String, codeType: Int) {
    val instance = getInstance(instanceId)
    val printer = instance.posPrinter ?: throw Exception("No printer connection for instance $instanceId")

    printer.initializePrinter()
      .printBarCode(data, codeType)
      .cutHalfAndFeed(1)
  }

  @ReactMethod
  fun tsplPrintBitmap(instanceId: String, sWidth: Double, sHeight: Double, bitmapData: String, width: Int) {
    val instance = getInstance(instanceId)
    val tsplPrinter = instance.tsplPrinter ?: throw Exception("No TSPL printer connection for instance $instanceId")

    val decodedString: ByteArray = Base64.decode(bitmapData, Base64.DEFAULT)
    val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    tsplPrinter.sizeMm(sWidth, sHeight)
      .gapMm(2.0, 0.0)
      .cls()
      .bitmap(0, 0, TSPLConst.BMP_MODE_OVERWRITE, width, bitmap, AlgorithmType.Threshold)
      .print(1)
  }

  @ReactMethod
  fun tsplFormFeed(instanceId: String, sWidth: Double, sHeight: Double) {
    val instance = getInstance(instanceId)
    val tsplPrinter = instance.tsplPrinter ?: throw Exception("No TSPL printer connection for instance $instanceId")

    tsplPrinter.sizeMm(sWidth, sHeight)
      .formFeed()
  }

  @ReactMethod
  fun printBitmap(instanceId: String, bitmapData: String, alignment: Int, width: Int, model: Int) {
    val instance = getInstance(instanceId)
    val printer = instance.posPrinter ?: throw Exception("No printer connection for instance $instanceId")

    val decodedString: ByteArray = Base64.decode(bitmapData, Base64.DEFAULT)
    val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    printer.initializePrinter()
      .printBitmap(decodedByte, alignment, width, model)
      .feedLine()
      .cutHalfAndFeed(1)
  }

  @ReactMethod
  fun closeConnection(instanceId: String) {
    val instance = getInstance(instanceId)
    instance.connection?.close()
    instance.connection = null
    instance.posPrinter = null
    instance.cpclPrinter = null
    instance.zplPrinter = null
    instance.tsplPrinter = null
  }

  override fun getName(): String {
    return NAME
  }
}
