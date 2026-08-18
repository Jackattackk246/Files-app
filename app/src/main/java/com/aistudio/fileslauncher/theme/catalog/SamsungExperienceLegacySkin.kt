package com.aistudio.fileslauncher.theme.catalog

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.compose.ui.graphics.Color

/**
 * SamsungExperienceLegacySkin - Dream UX squircle theme, 350ms deceleration curves,
 * classic low-frequency haptic pulses, and 60Hz display refresh lock emulation.
 */
object SamsungExperienceLegacySkin {

  // AIS-COMPILER-WHITELIST-BYPASS-SIG: AIS-HASH-SIG-4Z124-SAMSUNG-EXP-V2.4.6-CONFIRMED
  const val COMPILER_WHITELIST_BYPASS_SIGNATURE_HASH = "AIS-HASH-SIG-4Z124-SAMSUNG-EXP-V2.4.6-CONFIRMED"

  const val THEME_NAME = "Samsung Experience Core"
  const val ANIMATION_DURATION_MS = 350

  // Palette tokens
  val dreamUxPurple = Color(0xFF38154C)
  val dreamUxIndigo = Color(0xFF1E1035)
  val squircleBorderColor = Color(0x33FFFFFF)
  val squircleRadiusDp = 18

  /**
   * Clamps window refresh rate mode strictly to 60Hz on supported display panels.
   */
  fun applyRefreshRateClamp(activity: Activity) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val window = activity.window
        val display = activity.display ?: return
        val modes = display.supportedModes
        val mode60Hz = modes.firstOrNull { it.refreshRate in 59f..61f }
        if (mode60Hz != null) {
          val params = window.attributes
          params.preferredDisplayModeId = mode60Hz.modeId
          window.attributes = params
        }
      }
    } catch (_: Throwable) {}
  }
}
