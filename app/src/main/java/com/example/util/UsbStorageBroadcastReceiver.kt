package com.jackattackk246.files.util

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build

class UsbStorageBroadcastReceiver : BroadcastReceiver() {
  companion object {
    const val ACTION_USB_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"
    const val ACTION_USB_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED"
    const val ACTION_USB_PERMISSION = "com.jackattackk246.files.USB_PERMISSION"
  }

  override fun onReceive(context: Context, intent: Intent) {
    val action = intent.action ?: return
    when (action) {
      UsbManager.ACTION_USB_DEVICE_ATTACHED,
      ACTION_USB_DEVICE_ATTACHED,
      Intent.ACTION_MEDIA_MOUNTED -> {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
        
        if (device != null) {
          val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
          val permissionIntent = PendingIntent.getBroadcast(
             context, 0, Intent(ACTION_USB_PERMISSION), flags
          )
          usbManager.requestPermission(device, permissionIntent)
        } else {
            for (dev in usbManager.deviceList.values) {
               val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
               val permissionIntent = PendingIntent.getBroadcast(
                  context, 0, Intent(ACTION_USB_PERMISSION), flags
               )
               usbManager.requestPermission(dev, permissionIntent)
            }
        }
        UsbStorageManager.handleDeviceAttached(context, intent)
      }
      ACTION_USB_PERMISSION -> {
        synchronized(this) {
          val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
          if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            device?.apply {
              UsbStorageManager.handlePermissionGranted(context, device)
            }
          }
        }
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
