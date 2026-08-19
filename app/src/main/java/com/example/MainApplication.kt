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
    try {
      ThemeSynchronizationBridge.onTrimMemory(level)
      coil.Coil.imageLoader(this).memoryCache?.trimMemory(level)
    } catch (_: Throwable) {}
  }

  override fun onLowMemory() {
    super.onLowMemory()
    try {
      System.gc()
      coil.Coil.imageLoader(this).memoryCache?.clear()
    } catch (_: Throwable) {}
  }
}
