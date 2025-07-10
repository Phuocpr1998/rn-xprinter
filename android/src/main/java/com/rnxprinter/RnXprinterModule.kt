package com.rnxprinter

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
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

class RnXprinterModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {
  private lateinit var printer : POSPrinter
  private lateinit var cpclPrinter : CPCLPrinter
  private lateinit var zplPrinter : ZPLPrinter
  private lateinit var tsplPrinter : TSPLPrinter

  private var curConnect: IDeviceConnection? = null


  @ReactMethod
  fun getUsbDevices(promise: Promise) {
    val listDevice = POSConnect.getUsbDevice(reactApplicationContext)
    promise.resolve(listDevice)
  }

  @ReactMethod
  fun getSerialDevices(promise: Promise) {
    val listSerial = POSConnect.getSerialPort()
    promise.resolve(listSerial)
  }

  @ReactMethod
  fun serialConnect(serialPort: String, promise: Promise) {
    curConnect?.close()
    try {
      curConnect = POSConnect.createDevice(POSConnect.DEVICE_TYPE_SERIAL)
      curConnect!!.connect(serialPort) { code, connInfo, msg ->
        when (code) {
          POSConnect.CONNECT_SUCCESS -> {
            printer = POSPrinter(curConnect)
            cpclPrinter = CPCLPrinter(curConnect)
            zplPrinter = ZPLPrinter(curConnect)
            tsplPrinter= TSPLPrinter(curConnect)
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
            Log.e("XPrinterModule", "connectListener.USB_DETACHED --> $msg")
            promise.reject(code.toString(), msg)
          }
        }
      }
    }catch (ex: Exception) {
      Log.e("XPrinterModule", "connectListener.CONNECT_FAIL --> ${ex.message}")
      promise.reject("-1", ex.message)
    }
  }


  @ReactMethod
  fun usbConnect(device: String, promise: Promise) {
    curConnect?.close()
    try {
      curConnect = POSConnect.createDevice(POSConnect.DEVICE_TYPE_USB)
      curConnect!!.connect(device) { code, connInfo, msg ->
        when (code) {
          POSConnect.CONNECT_SUCCESS -> {
            printer = POSPrinter(curConnect)
            cpclPrinter = CPCLPrinter(curConnect)
            zplPrinter = ZPLPrinter(curConnect)
            tsplPrinter= TSPLPrinter(curConnect)
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
            Log.e("XPrinterModule", "connectListener.USB_DETACHED --> $msg")
            promise.reject(code.toString(), msg)
          }
        }
      }
    }catch (ex: Exception) {
      Log.e("XPrinterModule", "connectListener.CONNECT_FAIL --> ${ex.message}")
      promise.reject("-1", ex.message)
    }
  }


  @ReactMethod
  fun netConnect(ip: String, promise: Promise) {
    curConnect?.close()
    try {
      curConnect = POSConnect.createDevice(POSConnect.DEVICE_TYPE_ETHERNET)
      curConnect!!.connect(ip) { code, connInfo, msg ->
        when (code) {
          POSConnect.CONNECT_SUCCESS -> {
            printer = POSPrinter(curConnect)
            cpclPrinter = CPCLPrinter(curConnect)
            zplPrinter = ZPLPrinter(curConnect)
            tsplPrinter= TSPLPrinter(curConnect)
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
            Log.e("XPrinterModule", "connectListener.USB_DETACHED --> $msg")
            promise.reject(code.toString(), msg)
          }
        }
      }
    }catch (ex: Exception) {
      Log.e("XPrinterModule", "connectListener.CONNECT_FAIL --> ${ex.message}")
      promise.reject("-1", ex.message)
    }
  }

  @ReactMethod
  fun printQRCode(content: String) {
    printer.initializePrinter()
      .printQRCode(content)
      .feedLine()
      .cutHalfAndFeed(1)
  }

  @ReactMethod
  fun printText(content: String) {
    printer.initializePrinter()
      .printString(content)
      .cutHalfAndFeed(1)
  }

  @ReactMethod
  fun tsplPrintTest() {
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
  fun zplPrintTest() {
    zplPrinter.setCharSet("UTF-8")
    zplPrinter.addStart()
      .setCustomFont("LZHONGHEI.TTF", '1', ZPLConst.CODE_PAGE_UTF8)
      .addText(0, 0, '1', 24,24, "custom Font")
      .addText(100, 100, '1', ZPLConst.ROTATION_90, 24,24, "customFont 90")
      .addEnd()
  }

  @ReactMethod
  fun cpclPrintTest() {
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
  fun printPageModelData() {
    printer.initializePrinter()
      .printPageModelData()
      .cutHalfAndFeed(1)
  }

  @ReactMethod
  fun setCharSet(charSet: String) {
    printer.initializePrinter()
      .setCharSet(charSet)
  }

  @ReactMethod
  private fun printBarcode(data: String, codeType: Int) {
    printer.initializePrinter()
      .printBarCode(data, codeType)
      .cutHalfAndFeed(1)
  }

  @ReactMethod
  private fun tsplPrintBitmap(sWidth: Double, sHeight: Double, bitmapData: String, width: Int) {
    val decodedString: ByteArray = Base64.decode(bitmapData, Base64.DEFAULT)
    val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    tsplPrinter.sizeMm(sWidth, sHeight)
      .gapMm(2.0, 0.0)
      .cls()
      .bitmap(0, 0, TSPLConst.BMP_MODE_OVERWRITE, width, bitmap, AlgorithmType.Threshold)
      .print(1)
  }

  @ReactMethod
  private fun tsplFormFeed(sWidth: Double, sHeight: Double) {
    tsplPrinter.sizeMm(sWidth, sHeight)
      .formFeed()
  }

  @ReactMethod
  private fun printBitmap(bitmapData: String,  alignment: Int,width: Int, model: Int) {
    val decodedString: ByteArray = Base64.decode(bitmapData, Base64.DEFAULT)
    val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    printer.initializePrinter()
      .printBitmap(decodedByte, alignment, width, model)
      .feedLine()
      .cutHalfAndFeed(1)
  }

  @ReactMethod
  private fun closeConnection() {
    curConnect?.close()
  }

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "RnXprinter"
  }
}
