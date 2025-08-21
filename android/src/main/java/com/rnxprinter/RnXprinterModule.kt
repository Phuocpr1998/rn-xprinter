package com.rnxprinter

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
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

  // Store instances by instance ID
  private val instances = ConcurrentHashMap<String, PrinterInstance>()

  // For backward compatibility with legacy calls
  private val legacyInstance = PrinterInstance()

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
      val listDevice = POSConnect.getUsbDevice(reactApplicationContext)
      val writableArray = Arguments.createArray()

      if (listDevice != null) {
        for (device in listDevice) {
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

  @ReactMethod
  fun netConnect(instanceId: String, ip: String, promise: Promise) {
    val instance = getInstance(instanceId)
    instance.connection?.close()

    try {
      instance.connection = POSConnect.createDevice(POSConnect.DEVICE_TYPE_ETHERNET)
      instance.connection!!.connect(ip) { code, connInfo, msg ->
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

  companion object {
    const val NAME = "RnXprinter"
  }
}
