package com.jackattackk246.files.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.jackattackk246.files.ui.theme.AppThemeMode

object ThemePreferences {
  private const val PREFS_NAME = "files_theme_preferences"
  private const val KEY_THEME_MODE = "saved_app_theme_mode"
  private const val KEY_CUSTOM_ACCENT = "saved_custom_accent_color"
  private const val KEY_DIRECT_ROOT_LAUNCH = "pref_direct_root_system_launch"
  private const val KEY_SMART_SEARCH_ENABLED = "pref_smart_search_enabled"
  private const val KEY_CONTAINER_BG_MODE = "pref_container_bg_mode" // "solid" or "transparent"
  private const val KEY_MIRROR_HOME_WALLPAPER = "pref_mirror_home_wallpaper"
  private const val KEY_SAMSUNG_THEME_SYNC = "pref_samsung_theme_sync"

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  fun getSavedThemeMode(context: Context): AppThemeMode {
    val themeId = getPrefs(context).getString(KEY_THEME_MODE, AppThemeMode.MIDNIGHT_MATTE_BLACK.id)
    return AppThemeMode.fromId(themeId)
  }

  fun setSavedThemeMode(context: Context, themeMode: AppThemeMode) {
    getPrefs(context).edit().putString(KEY_THEME_MODE, themeMode.id).apply()
  }

  fun getSavedCustomAccentColor(context: Context): Color? {
    val prefs = getPrefs(context)
    if (!prefs.contains(KEY_CUSTOM_ACCENT)) return null
    val colorInt = prefs.getInt(KEY_CUSTOM_ACCENT, 0)
    return if (colorInt != 0) Color(colorInt) else null
  }

  fun setSavedCustomAccentColor(context: Context, color: Color?) {
    val editor = getPrefs(context).edit()
    if (color == null) {
      editor.remove(KEY_CUSTOM_ACCENT)
    } else {
      editor.putInt(KEY_CUSTOM_ACCENT, color.toArgb())
    }
    editor.apply()
  }

  // Direct Root System Launch
  fun isDirectRootLaunchEnabled(context: Context): Boolean {
    return getPrefs(context).getBoolean(KEY_DIRECT_ROOT_LAUNCH, false)
  }

  fun setDirectRootLaunchEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_DIRECT_ROOT_LAUNCH, enabled).apply()
  }

  // Smart Search (default true)
  fun isSmartSearchEnabled(context: Context): Boolean {
    return getPrefs(context).getBoolean(KEY_SMART_SEARCH_ENABLED, true)
  }

  fun setSmartSearchEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_SMART_SEARCH_ENABLED, enabled).apply()
  }

  // Container Background Mode ("solid" vs "transparent", default "solid")
  fun getContainerBackgroundMode(context: Context): String {
    return getPrefs(context).getString(KEY_CONTAINER_BG_MODE, "solid") ?: "solid"
  }

  fun setContainerBackgroundMode(context: Context, mode: String) {
    getPrefs(context).edit().putString(KEY_CONTAINER_BG_MODE, mode).apply()
  }

  // Mirror Home Screen Background
  fun isMirrorHomeWallpaperEnabled(context: Context): Boolean {
    return getPrefs(context).getBoolean(KEY_MIRROR_HOME_WALLPAPER, false)
  }

  fun setMirrorHomeWallpaperEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_MIRROR_HOME_WALLPAPER, enabled).apply()
  }

  // Match Samsung Theme Sync
  fun isSamsungThemeSyncEnabled(context: Context): Boolean {
    return getPrefs(context).getBoolean(KEY_SAMSUNG_THEME_SYNC, false)
  }

  fun setSamsungThemeSyncEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_SAMSUNG_THEME_SYNC, enabled).apply()
  }

  // Universal Animation Toggle System ("animations_enabled")
  fun areAnimationsEnabled(context: Context): Boolean {
    return getPrefs(context).getBoolean("animations_enabled", true)
  }

  fun setAnimationsEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean("animations_enabled", enabled).apply()
  }
}
