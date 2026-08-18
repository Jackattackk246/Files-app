package com.jackattackk246.files.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat

class DataTransferService : Service() {

  private var wakeLock: PowerManager.WakeLock? = null

  companion object {
    const val CHANNEL_ID = "files_data_transfer_channel"
    const val NOTIFICATION_ID = 24601
    const val ACTION_START_TRANSFER = "com.jackattackk246.files.action.START_TRANSFER"
    const val ACTION_STOP_TRANSFER = "com.jackattackk246.files.action.STOP_TRANSFER"

    fun startService(context: Context, taskTitle: String = "Processing Data Transfer...") {
      val intent = Intent(context, DataTransferService::class.java).apply {
        action = ACTION_START_TRANSFER
        putExtra("task_title", taskTitle)
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
      } else {
        context.startService(intent)
      }
    }

    fun stopService(context: Context) {
      val intent = Intent(context, DataTransferService::class.java).apply {
        action = ACTION_STOP_TRANSFER
      }
      context.stopService(intent)
    }
  }

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    wakeLock = powerManager.newWakeLock(
      PowerManager.PARTIAL_WAKE_LOCK,
      "FilesApp:DataTransferWakeLock"
    ).apply {
      setReferenceCounted(false)
    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent?.action == ACTION_STOP_TRANSFER) {
      releaseWakeLock()
      stopForeground(STOP_FOREGROUND_REMOVE)
      stopSelf()
      return START_NOT_STICKY
    }

    val taskTitle = intent?.getStringExtra("task_title") ?: "Active File Transfer Engine"
    acquireWakeLock()
    val notification = buildNotification(taskTitle, "Atomic transfer & checksum verification active")
    startForeground(NOTIFICATION_ID, notification)

    return START_STICKY
  }

  private fun acquireWakeLock() {
    try {
      if (wakeLock?.isHeld == false) {
        wakeLock?.acquire(30 * 60 * 1000L) // Max 30 mins safeguard
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun releaseWakeLock() {
    try {
      if (wakeLock?.isHeld == true) {
        wakeLock?.release()
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Files Data Transfer & Migration",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Persistent background progress and integrity verification for file operations"
      }
      val manager = getSystemService(NotificationManager::class.java)
      manager?.createNotificationChannel(channel)
    }
  }

  private fun buildNotification(title: String, text: String): Notification {
    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle(title)
      .setContentText(text)
      .setSmallIcon(android.R.drawable.stat_sys_upload)
      .setOngoing(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .build()
  }

  override fun onDestroy() {
    releaseWakeLock()
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
