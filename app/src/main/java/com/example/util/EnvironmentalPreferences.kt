package com.jackattackk246.files.util

import android.content.Context
import android.content.SharedPreferences
import com.jackattackk246.files.model.EnvironmentalBackdropConfig
import com.jackattackk246.files.model.EnvironmentalSeason
import com.jackattackk246.files.model.WeatherReport

object EnvironmentalPreferences {
  private const val PREFS_NAME = "files_environmental_preferences"
  private const val KEY_SEASON = "env_selected_season"
  private const val KEY_ENABLE_PARTICLES = "env_enable_particles"
  private const val KEY_CLOUD_OPACITY = "env_cloud_opacity"
  private const val KEY_SUNBURST_EXPANSION = "env_sunburst_expansion"
  private const val KEY_TEMP_C = "env_temp_c"
  private const val KEY_CONDITION_NAME = "env_condition_name"

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  fun getConfig(context: Context): EnvironmentalBackdropConfig {
    val prefs = getPrefs(context)
    val seasonId = prefs.getString(KEY_SEASON, EnvironmentalSeason.AUTO.id)
    val season = EnvironmentalSeason.fromId(seasonId)
    val enableParticles = prefs.getBoolean(KEY_ENABLE_PARTICLES, true)
    val cloudOpacity = prefs.getFloat(KEY_CLOUD_OPACITY, 0.45f)
    val sunburstExpansion = prefs.getFloat(KEY_SUNBURST_EXPANSION, 1.15f)
    val tempC = prefs.getInt(KEY_TEMP_C, 26)
    val conditionName = prefs.getString(KEY_CONDITION_NAME, "Clear Radiant Sunburst") ?: "Clear Radiant Sunburst"

    return EnvironmentalBackdropConfig(
      selectedSeason = season,
      enableParticles = enableParticles,
      weatherReport = WeatherReport(
        temperatureCelsius = tempC,
        conditionName = conditionName,
        cloudOpacity = cloudOpacity,
        sunburstExpansion = sunburstExpansion
      ),
      darkGlassMaskOpacity = 0.60f
    )
  }

  fun saveConfig(context: Context, config: EnvironmentalBackdropConfig) {
    getPrefs(context).edit()
      .putString(KEY_SEASON, config.selectedSeason.id)
      .putBoolean(KEY_ENABLE_PARTICLES, config.enableParticles)
      .putFloat(KEY_CLOUD_OPACITY, config.weatherReport.cloudOpacity)
      .putFloat(KEY_SUNBURST_EXPANSION, config.weatherReport.sunburstExpansion)
      .putInt(KEY_TEMP_C, config.weatherReport.temperatureCelsius)
      .putString(KEY_CONDITION_NAME, config.weatherReport.conditionName)
      .apply()
  }
}
