package com.rnxprinter
import android.util.Log
import com.facebook.react.bridge.Promise

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import net.posprinter.IDeviceConnection
import net.posprinter.POSConnect
import net.posprinter.POSPrinter

class RnXprinterModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private lateinit var printer : POSPrinter
  var curConnect: IDeviceConnection? = null

  @ReactMethod
  fun netConnect(ip: String, promise: Promise) {
    curConnect?.close()
    try {
      curConnect = POSConnect.createDevice(POSConnect.DEVICE_TYPE_ETHERNET)
      curConnect!!.connect(ip, { code,connInfo, msg ->
        when (code) {
          POSConnect.CONNECT_SUCCESS -> {
            printer = POSPrinter(curConnect)
            promise.resolve("success")
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
      })
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
  private fun printBitmap(bitmapPath: String,  alignment: Int,width: Int, model: Int) {
    printer.initializePrinter()
      .printBitmap(bitmapPath, alignment, width, model)
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
