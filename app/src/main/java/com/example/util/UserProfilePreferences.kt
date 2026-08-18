package com.jackattackk246.files.util

import android.content.Context
import android.content.SharedPreferences

object UserProfilePreferences {
  private const val PREFS_NAME = "launcher_prefs"
  private const val KEY_ONBOARDING_COMPLETED = "onboarding_complete"
  private const val KEY_USER_PROFILE_NAME = "user_profile_name"
  private const val KEY_USER_NAME = "pref_user_name"
  private const val KEY_LANGUAGE = "pref_user_language"
  private const val KEY_REGION = "pref_user_region"

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  fun isOnboardingCompleted(context: Context): Boolean {
    return getPrefs(context).getBoolean(KEY_ONBOARDING_COMPLETED, false)
  }

  fun setOnboardingCompleted(context: Context, completed: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
  }

  fun getUserName(context: Context): String {
    val prefs = getPrefs(context)
    val name = prefs.getString(KEY_USER_PROFILE_NAME, null)
      ?: prefs.getString(KEY_USER_NAME, "User")
      ?: "User"
    return name
  }

  fun setUserName(context: Context, name: String) {
    val trimmed = name.trim()
    getPrefs(context).edit()
      .putString(KEY_USER_PROFILE_NAME, trimmed)
      .putString(KEY_USER_NAME, trimmed)
      .apply()
  }

  /**
   * Evaluates current system clock hour and returns the dynamic greeting
   * with proper user profile name spacing rules.
   */
  fun getDynamicTimeGreeting(context: Context): String {
    val sharedPrefs = getPrefs(context)
    val profileName = sharedPrefs.getString(KEY_USER_PROFILE_NAME, "") ?: sharedPrefs.getString(KEY_USER_NAME, "") ?: ""

    val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val timeGreeting = when (currentHour) {
      in 0..11 -> "Good morning"
      in 12..16 -> "Good afternoon"
      in 17..21 -> "Good evening"
      else -> "Good night"
    }

    return if (profileName.trim().isEmpty()) {
      timeGreeting
    } else {
      "$timeGreeting, ${profileName.trim()}"
    }
  }

  fun getLanguage(context: Context): String {
    return getPrefs(context).getString(KEY_LANGUAGE, "English (US)") ?: "English (US)"
  }

  fun setLanguage(context: Context, language: String) {
    getPrefs(context).edit().putString(KEY_LANGUAGE, language).apply()
  }

  fun getRegion(context: Context): String {
    return getPrefs(context).getString(KEY_REGION, "United States") ?: "United States"
  }

  fun setRegion(context: Context, region: String) {
    getPrefs(context).edit().putString(KEY_REGION, region).apply()
  }

  val availableLanguages = listOf(
    "English (US)",
    "English (UK)",
    "Español (Spanish)",
    "Français (French)",
    "Deutsch (German)",
    "Italiano (Italian)",
    "Português (Portuguese)",
    "日本語 (Japanese)",
    "한국어 (Korean)",
    "中文 (Simplified Chinese)",
    "繁體中文 (Traditional Chinese)",
    "العربية (Arabic)",
    "हिन्दी (Hindi)",
    "Русский (Russian)",
    "Nederlands (Dutch)",
    "Polski (Polish)",
    "Türkçe (Turkish)",
    "Tiếng Việt (Vietnamese)",
    "Bahasa Indonesia"
  )

  val availableRegions = listOf(
    "United States",
    "United Kingdom",
    "Canada",
    "European Union",
    "Germany",
    "France",
    "Spain",
    "Italy",
    "Japan",
    "South Korea",
    "China",
    "India",
    "Brazil",
    "Australia",
    "Mexico",
    "Middle East",
    "Southeast Asia",
    "Latin America",
    "Global / Other"
  )
}
