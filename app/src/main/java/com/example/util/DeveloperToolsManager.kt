package com.jackattackk246.files.util

import android.content.Context
import android.content.SharedPreferences

object DeveloperToolsManager {
  private const val PREFS_NAME = "developer_tools_prefs"
  
  private const val KEY_ORIGINAL_SETTINGS = "dev_original_settings_enabled"
  private const val KEY_FPS_OVERLAY = "dev_fps_overlay_enabled"
  private const val KEY_POWER_PROFILE = "dev_power_gating_profile"
  private const val KEY_UNIVERSAL_TEXT_OVERRIDE = "dev_universal_text_override_enabled"
  private const val KEY_CUSTOM_STRING_MAP = "dev_custom_string_map_"

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  fun isOriginalSettingsEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_ORIGINAL_SETTINGS, true)
  fun setOriginalSettingsEnabled(context: Context, enabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_ORIGINAL_SETTINGS, enabled).apply()

  fun isFpsOverlayEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_FPS_OVERLAY, false)
  fun setFpsOverlayEnabled(context: Context, enabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_FPS_OVERLAY, enabled).apply()

  fun getPowerProfile(context: Context): String = getPrefs(context).getString(KEY_POWER_PROFILE, "Standard Balance Profile") ?: "Standard Balance Profile"
  fun setPowerProfile(context: Context, profile: String) = getPrefs(context).edit().putString(KEY_POWER_PROFILE, profile).apply()

  fun isUniversalTextOverrideEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_UNIVERSAL_TEXT_OVERRIDE, false)
  fun setUniversalTextOverrideEnabled(context: Context, enabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_UNIVERSAL_TEXT_OVERRIDE, enabled).apply()

  fun getCustomText(context: Context, originalText: String, defaultReturn: String): String {
    if (!isUniversalTextOverrideEnabled(context)) return defaultReturn
    return getPrefs(context).getString(KEY_CUSTOM_STRING_MAP + originalText.hashCode(), defaultReturn) ?: defaultReturn
  }

  fun setCustomText(context: Context, originalText: String, newText: String) {
    getPrefs(context).edit().putString(KEY_CUSTOM_STRING_MAP + originalText.hashCode(), newText).apply()
  }

  fun clearAllCaches(context: Context) {
    try {
      context.cacheDir.deleteRecursively()
      context.externalCacheDir?.deleteRecursively()
      Runtime.getRuntime().gc()
    } catch (_: Exception) {}
  }
}
