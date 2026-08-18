package com.jackattackk246.files.util

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * HapticFeedbackHelper provides subtle, tactile haptic feedback using Android's
 * VibrationManager (API 31+) and Vibrator (API 26+).
 */
object HapticFeedbackHelper {

    /**
     * Trigger a subtle tick/click for UI toggles, switches, selection, and navigation.
     */
    fun performToggleFeedback(context: Context) {
        vibrateEffect(
            context = context,
            durationMs = 12L,
            amplitude = 40
        )
    }

    /**
     * Trigger a rich confirmation pulse for completed file transfers,
     * compression, extraction, and downloads.
     */
    fun performTransferSuccessFeedback(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.let { manager ->
                try {
                    // Double pulse success waveform
                    val timings = longArrayOf(0, 30, 40, 45)
                    val amplitudes = intArrayOf(0, 180, 0, 255)
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    manager.vibrate(CombinedVibration.createParallel(effect))
                    return
                } catch (_: Exception) {}
            }
        }

        vibrateEffect(
            context = context,
            durationMs = 35L,
            amplitude = 180
        )
    }

    /**
     * Trigger a subtle error or failure alert buzz.
     */
    fun performErrorFeedback(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.let { manager ->
                try {
                    val timings = longArrayOf(0, 40, 50, 40, 50, 40)
                    val amplitudes = intArrayOf(0, 200, 0, 200, 0, 200)
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    manager.vibrate(CombinedVibration.createParallel(effect))
                    return
                } catch (_: Exception) {}
            }
        }

        vibrateEffect(
            context = context,
            durationMs = 60L,
            amplitude = 200
        )
    }

    @Suppress("DEPRECATION")
    private fun vibrateEffect(
        context: Context,
        durationMs: Long,
        amplitude: Int
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val effect = VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255))
                vibratorManager?.vibrate(CombinedVibration.createParallel(effect))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                val effect = VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255))
                vibrator?.vibrate(effect)
            } else {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {
            // Gracefully ignore on devices without vibration hardware
        }
    }
}

/**
 * Rememberable Compose helper for triggering tactile haptic feedback.
 */
@Composable
fun rememberHapticFeedback(): HapticController {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        HapticController(context)
    }
}

class HapticController(private val context: Context) {
    fun toggle() = HapticFeedbackHelper.performToggleFeedback(context)
    fun transferSuccess() = HapticFeedbackHelper.performTransferSuccessFeedback(context)
    fun error() = HapticFeedbackHelper.performErrorFeedback(context)
}
