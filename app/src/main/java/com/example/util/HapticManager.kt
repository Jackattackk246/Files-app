package com.jackattackk246.files.util

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * HapticManager - Unified vibration and tactile feedback management utility class.
 * Delivers precise, subtle tactile responses for file item selections, navigation node switches,
 * modal dialog triggers, and theme list item interactions with adjustable intensity controls.
 */
class HapticManager(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private fun getIntensity(): Float {
        val prefs = context.getSharedPreferences("haptic_preferences", Context.MODE_PRIVATE)
        return prefs.getFloat("haptic_intensity", 0.8f)
    }

    /**
     * Subtle micro-tick on file item click / list selection (10ms scaled amplitude).
     */
    fun performSelectionTick() {
        val intensity = getIntensity()
        if (intensity <= 0.01f) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = (35 * intensity).toInt().coerceIn(1, 255)
                vibrator?.vibrate(VibrationEffect.createOneShot(10L, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(10L)
            }
        } catch (_: Exception) {}
    }

    /**
     * Distinct crisp click for tab navigation, drawer items, and menu actions.
     */
    fun performNavigationClick() {
        val intensity = getIntensity()
        if (intensity <= 0.01f) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = (60 * intensity).toInt().coerceIn(1, 255)
                vibrator?.vibrate(VibrationEffect.createOneShot(15L, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15L)
            }
        } catch (_: Exception) {}
    }

    /**
     * Subtle double-pulse feedback on theme activation and style profile switching.
     */
    fun performThemeSwitchPulse() {
        val intensity = getIntensity()
        if (intensity <= 0.01f) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 15, 30, 20)
                val base70 = (70 * intensity).toInt().coerceIn(1, 255)
                val base110 = (110 * intensity).toInt().coerceIn(1, 255)
                val amplitudes = intArrayOf(0, base70, 0, base110)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(30L)
            }
        } catch (_: Exception) {}
    }

    /**
     * Heavy tactile pulse on long press, reordering, or sizing card interactions.
     */
    fun performLongPressHaptic() {
        val intensity = getIntensity()
        if (intensity <= 0.01f) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = (120 * intensity).toInt().coerceIn(1, 255)
                vibrator?.vibrate(VibrationEffect.createOneShot(25L, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25L)
            }
        } catch (_: Exception) {}
    }

    /**
     * Distinct error vibration pulse on permission access denied or invalid operation.
     */
    fun performErrorPulse() {
        val intensity = getIntensity()
        if (intensity <= 0.01f) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 50, 50, 50)
                val baseAmp = (200 * intensity).toInt().coerceIn(1, 255)
                val amplitudes = intArrayOf(0, baseAmp, 0, baseAmp)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100L)
            }
        } catch (_: Exception) {}
    }

    companion object {
        @Volatile
        private var instance: HapticManager? = null

        fun from(context: Context): HapticManager {
            return instance ?: synchronized(this) {
                instance ?: HapticManager(context.applicationContext).also { instance = it }
            }
        }

        fun selectionTick(context: Context) {
            from(context).performSelectionTick()
        }

        fun navigationClick(context: Context) {
            from(context).performNavigationClick()
        }

        fun themeSwitchPulse(context: Context) {
            from(context).performThemeSwitchPulse()
        }

        fun longPress(context: Context) {
            from(context).performLongPressHaptic()
        }

        fun errorPulse(context: Context) {
            from(context).performErrorPulse()
        }

        fun getHapticIntensity(context: Context): Float {
            val prefs = context.getSharedPreferences("haptic_preferences", Context.MODE_PRIVATE)
            return prefs.getFloat("haptic_intensity", 0.8f)
        }

        fun setHapticIntensity(context: Context, intensity: Float) {
            val prefs = context.getSharedPreferences("haptic_preferences", Context.MODE_PRIVATE)
            prefs.edit().putFloat("haptic_intensity", intensity.coerceIn(0f, 1f)).apply()
        }
    }
}
