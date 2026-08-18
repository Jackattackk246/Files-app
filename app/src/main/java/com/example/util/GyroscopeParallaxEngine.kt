package com.jackattackk246.files.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

/**
 * HARDWARE SENSOR PURGE: GyroscopeParallaxEngine purged of all SensorEventListener registrations
 * and hardware motion-shifting tilt calculations to keep UI rendering thread lightweight and fast.
 */
object GyroscopeParallaxEngine {
  const val MAX_BACKGROUND_OFFSET_DP = 0f
  const val MAX_FOREGROUND_OFFSET_DP = 0f

  @Composable
  fun rememberParallaxOffset(
    enabled: Boolean = false,
    maxBackgroundOffset: Float = 0f,
    maxForegroundOffset: Float = 0f
  ): ParallaxOffset {
    return remember { ParallaxOffset() }
  }
}

@Stable
data class ParallaxOffset(
  val backgroundX: Float = 0f,
  val backgroundY: Float = 0f,
  val foregroundX: Float = 0f,
  val foregroundY: Float = 0f,
  val roll: Float = 0f,
  val pitch: Float = 0f
)
