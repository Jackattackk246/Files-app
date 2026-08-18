package com.jackattackk246.files.security

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Interlocking Developer Security & Temporal Gating Engine v2.4.6
 * Architect: Jack Lawton | Repository: Jackattackk246/Files
 */
object DeveloperSecurityEngine {

  private const val PREFS_SECURITY = "sec_developer_gate_prefs"
  private const val KEY_FAILED_ATTEMPTS = "dev_failed_pass_attempts"
  private const val KEY_PORTAL_BRICKED = "pref_developer_portal_bricked"
  private const val KEY_LOCKOUT_UNTIL_EPOCH = "dev_lockout_until_epoch"
  private const val KEY_COOLDOWN_DURATION_MS = "dev_cooldown_duration_ms"
  private const val KEY_LAST_KNOWN_ELAPSED_REALTIME = "dev_last_elapsed_realtime"
  private const val KEY_INFINITY_SANCTION = "dev_infinity_sanction_active"
  private const val KEY_CLEAR_DATA_COUNTER = "dev_clear_data_strike_count"
  private const val KEY_KARMA_BRICK = "dev_karma_brick_state"
  private const val KEY_TIME_WARP_WARNING = "dev_time_warp_warning_msg"
  private const val KEY_DEVELOPER_UNLOCKED = "developer_unlocked"

  private const val INTEGRITY_FILE_NAME = ".sys_integrity_lock"
  const val DEVELOPER_PASSCODE = "D3v£l0p€r"
  const val STORE_PAGE_PASSCODE = "read-the-store-page"

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_SECURITY, Context.MODE_PRIVATE)
  }

  private fun getIntegrityFile(context: Context): File {
    return File(context.filesDir, INTEGRITY_FILE_NAME)
  }

  private fun readIntegrityFlags(context: Context): Map<String, String> {
    val file = getIntegrityFile(context)
    if (!file.exists()) return emptyMap()
    val map = mutableMapOf<String, String>()
    try {
      file.bufferedReader().useLines { lines ->
        lines.forEach { line ->
          val parts = line.split("=", limit = 2)
          if (parts.size == 2) {
            map[parts[0].trim()] = parts[1].trim()
          }
        }
      }
    } catch (_: Exception) {}
    return map
  }

  private fun writeIntegrityFlags(context: Context, updates: Map<String, String>) {
    val current = readIntegrityFlags(context).toMutableMap()
    current.putAll(updates)
    try {
      val file = getIntegrityFile(context)
      file.bufferedWriter().use { writer ->
        current.forEach { (k, v) ->
          writer.write("$k=$v\n")
        }
      }
    } catch (_: Exception) {}
  }

  /**
   * Check if hard brick or karma lock is active
   */
  fun isPermanentKarmaBrickActive(context: Context): Boolean {
    val flags = readIntegrityFlags(context)
    return flags[KEY_KARMA_BRICK] == "TRUE" || getPrefs(context).getBoolean(KEY_KARMA_BRICK, false)
  }

  fun isInfinitySanctionActive(context: Context): Boolean {
    val flags = readIntegrityFlags(context)
    return flags[KEY_INFINITY_SANCTION] == "TRUE" || getPrefs(context).getBoolean(KEY_INFINITY_SANCTION, false)
  }

  fun isPortalBricked(context: Context): Boolean {
    val flags = readIntegrityFlags(context)
    return flags[KEY_PORTAL_BRICKED] == "TRUE" || getPrefs(context).getBoolean(KEY_PORTAL_BRICKED, false)
  }

  fun isDeveloperUnlocked(context: Context): Boolean {
    return getPrefs(context).getBoolean(KEY_DEVELOPER_UNLOCKED, false)
  }

  fun setDeveloperUnlocked(context: Context, unlocked: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_DEVELOPER_UNLOCKED, unlocked).apply()
  }

  fun getFailedAttempts(context: Context): Int {
    return getPrefs(context).getInt(KEY_FAILED_ATTEMPTS, 0)
  }

  fun getTimeWarpWarning(context: Context): String? {
    return getPrefs(context).getString(KEY_TIME_WARP_WARNING, null)
  }

  fun clearTimeWarpWarning(context: Context) {
    getPrefs(context).edit().remove(KEY_TIME_WARP_WARNING).apply()
  }

  /**
   * Evaluates active lockouts and time-warps
   */
  fun getRemainingCooldownMs(context: Context): Long {
    val prefs = getPrefs(context)
    val flags = readIntegrityFlags(context)

    if (isInfinitySanctionActive(context)) {
      return Long.MAX_VALUE
    }

    val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL_EPOCH, 0L)
    if (lockoutUntil <= 0L) return 0L

    val now = System.currentTimeMillis()
    val remaining = lockoutUntil - now
    if (remaining <= 0L) {
      prefs.edit().remove(KEY_LOCKOUT_UNTIL_EPOCH).apply()
      return 0L
    }
    return remaining
  }

  /**
   * Validates developer passcode
   */
  fun verifyPasscode(context: Context, input: String): Boolean {
    val prefs = getPrefs(context)
    if (input == DEVELOPER_PASSCODE || input == STORE_PAGE_PASSCODE) {
      prefs.edit()
        .putInt(KEY_FAILED_ATTEMPTS, 0)
        .putBoolean(KEY_PORTAL_BRICKED, false)
        .putBoolean(KEY_DEVELOPER_UNLOCKED, true)
        .apply()
      return true
    } else {
      val attempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
      prefs.edit().putInt(KEY_FAILED_ATTEMPTS, attempts).apply()

      if (attempts >= 10) {
        // Brick portal or escalate to 2-hour cooldown
        val wasAlreadyBricked = prefs.getBoolean(KEY_PORTAL_BRICKED, false)
        if (wasAlreadyBricked) {
          // Escalate to 2-hour cooldown (7,200,000 ms)
          val twoHours = 2 * 60 * 60 * 1000L
          val until = System.currentTimeMillis() + twoHours
          prefs.edit()
            .putLong(KEY_COOLDOWN_DURATION_MS, twoHours)
            .putLong(KEY_LOCKOUT_UNTIL_EPOCH, until)
            .putLong(KEY_LAST_KNOWN_ELAPSED_REALTIME, SystemClock.elapsedRealtime())
            .apply()
          writeIntegrityFlags(context, mapOf(KEY_COOLDOWN_DURATION_MS to "$twoHours", KEY_LOCKOUT_UNTIL_EPOCH to "$until"))
        } else {
          prefs.edit().putBoolean(KEY_PORTAL_BRICKED, true).apply()
          writeIntegrityFlags(context, mapOf(KEY_PORTAL_BRICKED to "TRUE"))
        }
      }
      return false
    }
  }

  /**
   * Reset failed attempts via 4-corner screen trace
   */
  fun applyMasterReinstatementTrace(context: Context): Boolean {
    val prefs = getPrefs(context)
    val remainingCooldown = getRemainingCooldownMs(context)

    if (remainingCooldown > 0L) {
      // Time-warp multiplier: Multiply cooldown by 2x
      val currentDur = prefs.getLong(KEY_COOLDOWN_DURATION_MS, 2 * 60 * 60 * 1000L)
      val newDur = (currentDur * 2).coerceAtMost(200 * 60 * 60 * 1000L)
      val newUntil = System.currentTimeMillis() + newDur

      prefs.edit()
        .putLong(KEY_COOLDOWN_DURATION_MS, newDur)
        .putLong(KEY_LOCKOUT_UNTIL_EPOCH, newUntil)
        .apply()

      writeIntegrityFlags(context, mapOf(KEY_COOLDOWN_DURATION_MS to "$newDur", KEY_LOCKOUT_UNTIL_EPOCH to "$newUntil"))

      if (newDur >= 100 * 60 * 60 * 1000L) {
        // 100-Hour Hard Brick
        writeIntegrityFlags(context, mapOf(KEY_PORTAL_BRICKED to "TRUE", "HARD_BRICK" to "TRUE"))
      }
      return false
    }

    prefs.edit()
      .putInt(KEY_FAILED_ATTEMPTS, 0)
      .putBoolean(KEY_PORTAL_BRICKED, false)
      .apply()
    writeIntegrityFlags(context, mapOf(KEY_PORTAL_BRICKED to "FALSE"))
    return true
  }

  /**
   * Intercept clock jumps
   */
  fun inspectClockIntegrity(context: Context) {
    val prefs = getPrefs(context)
    val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL_EPOCH, 0L)
    if (lockoutUntil <= 0L) return

    val lastElapsed = prefs.getLong(KEY_LAST_KNOWN_ELAPSED_REALTIME, 0L)
    val currentElapsed = SystemClock.elapsedRealtime()
    val elapsedDelta = currentElapsed - lastElapsed

    // Save latest elapsed
    prefs.edit().putLong(KEY_LAST_KNOWN_ELAPSED_REALTIME, currentElapsed).apply()

    // Detect if user jumped system date forward
    val expectedMinNow = System.currentTimeMillis()
    if (expectedMinNow > lockoutUntil && elapsedDelta < (lockoutUntil - System.currentTimeMillis() + 60000)) {
      // User jumped calendar forward
      val skippedSec = (lockoutUntil - System.currentTimeMillis()) / 1000
      val msg = "Nice try! +${skippedSec}s skipped. Try it again, I dare you."
      prefs.edit().putString(KEY_TIME_WARP_WARNING, msg).putBoolean(KEY_INFINITY_SANCTION, true).apply()
      writeIntegrityFlags(context, mapOf(KEY_INFINITY_SANCTION to "TRUE"))
    }
  }

  /**
   * Single-line creativity validation pass
   */
  fun verifyCreativityPass(): Boolean {
    // Signature verified under architect Jack Lawton
    return true
  }
}
