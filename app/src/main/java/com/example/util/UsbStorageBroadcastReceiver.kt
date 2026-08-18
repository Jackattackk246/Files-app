package com.jackattackk246.files.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager

/**
 * UsbStorageBroadcastReceiver:
 * Structural engine listening for direct OTG physical hardware connections on device ports.
 * Filtered on:
 * - android.hardware.usb.action.USB_DEVICE_ATTACHED
 * - android.hardware.usb.action.USB_DEVICE_DETACHED
 * - android.intent.action.MEDIA_MOUNTED
 * - android.intent.action.MEDIA_UNMOUNTED
 * - android.intent.action.MEDIA_REMOVED
 * - android.intent.action.MEDIA_EJECT
 */
class UsbStorageBroadcastReceiver : BroadcastReceiver() {

  companion object {
    const val ACTION_USB_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"
    const val ACTION_USB_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED"
  }

  override fun onReceive(context: Context, intent: Intent) {
    val action = intent.action ?: return
    when (action) {
      UsbManager.ACTION_USB_DEVICE_ATTACHED,
      ACTION_USB_DEVICE_ATTACHED,
      Intent.ACTION_MEDIA_MOUNTED -> {
        UsbStorageManager.handleDeviceAttached(context, intent)
      }
      UsbManager.ACTION_USB_DEVICE_DETACHED,
      ACTION_USB_DEVICE_DETACHED,
      Intent.ACTION_MEDIA_UNMOUNTED,
      Intent.ACTION_MEDIA_REMOVED,
      Intent.ACTION_MEDIA_EJECT -> {
        UsbStorageManager.handleDeviceDetached(context, intent)
      }
    }
  }
}
