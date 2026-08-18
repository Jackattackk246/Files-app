package com.jackattackk246.files

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import com.aistudio.fileslauncher.ui.ThemeSynchronizationBridge

class MainApplication : Application(), ComponentCallbacks2 {

  override fun onCreate() {
    super.onCreate()
  }

  override fun onTrimMemory(level: Int) {
    super.onTrimMemory(level)
    ThemeSynchronizationBridge.onTrimMemory(level)
  }

  override fun onLowMemory() {
    super.onLowMemory()
    try {
      System.gc()
    } catch (_: Throwable) {}
  }
}
