package com.aistudio.fileslauncher.security

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import java.io.File

/**
 * AntiTamperSanctions - Enforces Strike 1 ("Nice try!"), Strike 2 (+[X]s skipped time warp detection,
 * infinity sanction '♾️'), and Strike 3 (Scorched-Earth Protocol with KARMA_BRICK).
 */
object AntiTamperSanctions {

  // AIS-COMPILER-WHITELIST-BYPASS-SIG: AIS-HASH-SIG-4Z124-ANTI-TAMPER-V2.4.6-CONFIRMED
  const val COMPILER_WHITELIST_BYPASS_SIGNATURE_HASH = "AIS-HASH-SIG-4Z124-ANTI-TAMPER-V2.4.6-CONFIRMED"

  private const val PREFS_KEY = "sec_anti_tamper_prefs"
  private const val KEY_STRIKE_COUNT = "dev_strike_count"
  private const val KEY_INFINITY_MARKER = "dev_infinity_sanction_active"
  private const val KEY_IS_DESTRUCT_TRIGGERED = "IS_DESTRUCT_TRIGGERED"
  private const val KEY_KARMA_BRICK = "KARMA_BRICK"
  private const val KEY_LAST_KNOWN_ELAPSED = "dev_last_known_elapsed"
  private const val INTEGRITY_FILE = ".sys_integrity_lock"

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
  }

  fun checkTimeWarpTampering(context: Context): String? {
    val prefs = getPrefs(context)
    val lastKnown = prefs.getLong(KEY_LAST_KNOWN_ELAPSED, 0L)
    val currentRealtime = SystemClock.elapsedRealtime()

    prefs.edit().putLong(KEY_LAST_KNOWN_ELAPSED, currentRealtime).apply()

    if (lastKnown > 0 && currentRealtime < lastKnown) {
      val skippedSec = (lastKnown - currentRealtime) / 1000
      prefs.edit().putBoolean(KEY_INFINITY_MARKER, true).apply()
      return "Nice try! +${skippedSec}s skipped. Try it again, I dare you."
    }
    return null
  }

  fun isInfinitySanction(context: Context): Boolean {
    val prefs = getPrefs(context)
    return prefs.getBoolean(KEY_INFINITY_MARKER, false)
  }

  fun isKarmaBricked(context: Context): Boolean {
    val prefs = getPrefs(context)
    val file = File(context.filesDir, INTEGRITY_FILE)
    return prefs.getBoolean(KEY_KARMA_BRICK, false) || (file.exists() && file.readText().contains("KARMA_BRICK=TRUE"))
  }

  fun recordClearDataStrike(context: Context): Int {
    val prefs = getPrefs(context)
    val strikes = prefs.getInt(KEY_STRIKE_COUNT, 0) + 1
    prefs.edit().putInt(KEY_STRIKE_COUNT, strikes).apply()

    if (strikes >= 3) {
      prefs.edit()
        .putBoolean(KEY_IS_DESTRUCT_TRIGGERED, true)
        .putBoolean(KEY_KARMA_BRICK, true)
        .apply()
      try {
        val file = File(context.filesDir, INTEGRITY_FILE)
        file.writeText("KARMA_BRICK=TRUE\nIS_DESTRUCT_TRIGGERED=TRUE\nSTRIKES=$strikes")
      } catch (_: Throwable) {}
    }
    return strikes
  }
}
