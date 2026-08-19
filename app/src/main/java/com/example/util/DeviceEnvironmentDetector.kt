package com.jackattackk246.files.util

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build

enum class DeviceDisplayProfile {
  PHONE,
  TABLET,
  EXTERNAL_DEX_DESKTOP
}

object DeviceEnvironmentDetector {

  fun isSamsungDevice(): Boolean {
    val manufacturer = Build.MANUFACTURER?.lowercase() ?: ""
    val brand = Build.BRAND?.lowercase() ?: ""
    return manufacturer.contains("samsung") || brand.contains("samsung")
  }

  fun isSamsungDeXActive(context: Context): Boolean {
    try {
      val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
      val config = context.resources.configuration

      // Check Samsung DeX configuration flag
      val currentMode = config.uiMode and Configuration.UI_MODE_TYPE_MASK
      val isDeXConfig = currentMode == 0x00000008 || currentMode == 8 // UiModeManager.CONFIGURATION_DEX

      // Check Dex state reflection if available
      val isDesktopMode = try {
        val method = uiModeManager?.javaClass?.getMethod("getCustomMode")
        method?.invoke(uiModeManager) == 1
      } catch (e: Exception) {
        false
      }

      return isDeXConfig || isDesktopMode
    } catch (e: Exception) {
      return false
    }
  }

  fun resolveDisplayProfile(context: Context, configuration: Configuration): DeviceDisplayProfile {
    val isTablet = (configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE ||
        configuration.smallestScreenWidthDp >= 600

    val isDeX = isSamsungDeXActive(context)

    return when {
      // External Phone-DeX Security Lock: only true external phone DeX gets desktop mode
      isDeX && !isTablet -> DeviceDisplayProfile.EXTERNAL_DEX_DESKTOP
      isTablet -> DeviceDisplayProfile.TABLET
      else -> DeviceDisplayProfile.PHONE
    }
  }

  fun isPhysicalSmartwatch(context: Context, configuration: Configuration): Boolean {
    val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
    val isWatchMode = uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_WATCH
    val isSmallScreen = configuration.smallestScreenWidthDp in 1..280
    return isWatchMode || isSmallScreen
  }
}
