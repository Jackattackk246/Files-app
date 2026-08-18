package com.jackattackk246.files.model

import java.util.Calendar

/**
 * Environmental weather profiles and seasonal states mapped to
 * dynamic vector shaders, particle systems, and local system calendar.
 */
enum class EnvironmentalSeason(
  val id: String,
  val displayName: String,
  val subtitle: String,
  val primaryColorHex: Long,
  val isLightProfile: Boolean = false
) {
  AUTO(
    id = "auto",
    displayName = "Auto (System Clock & Dynamic Weather)",
    subtitle = "Real-time calendar month + hourly weather engine with 65% charcoal glass mask",
    primaryColorHex = 0xFF00F2FE,
    isLightProfile = false
  ),
  CLEAR_NIGHT(
    id = "clear_night",
    displayName = "Background 01: Clear Sky Night",
    subtitle = "Deep Cobalt Blue (#0D2040) to Slate Indigo (#1A2B4C) with shooting star particle system",
    primaryColorHex = 0xFF1A2B4C,
    isLightProfile = false
  ),
  TAILS_MECHANICAL_SANDBOX(
    id = "preset_03_tails_sandbox",
    displayName = "Background 02: Tails' Mechanical Sandbox",
    subtitle = "Cell-shaded industrial slate-gray and brushed metallic bronze robot matrix overlay",
    primaryColorHex = 0xFFC68B59,
    isLightProfile = false
  ),
  AUTUMN(
    id = "autumn",
    displayName = "Background 03: Autumn Copper",
    subtitle = "Deep Burgundy (#4A1525) down to Muted Burnt Orange (#A84B24) with drift leaves",
    primaryColorHex = 0xFFA84B24,
    isLightProfile = false
  ),
  MIDNIGHT_WORKSPACE(
    id = "preset_01_midnight",
    displayName = "Midnight Workspace",
    subtitle = "Deep Cobalt Blue (#0D2040) to Slate Indigo (#1A2B4C) with shooting stars",
    primaryColorHex = 0xFF1A2B4C,
    isLightProfile = false
  ),
  DAYLIGHT_WORKSPACE(
    id = "preset_02_daylight",
    displayName = "Daylight Workspace",
    subtitle = "Vivid Sky Blue (#4FACFE) to Horizon Cobalt (#00F2FE) with soft sunburst",
    primaryColorHex = 0xFF00F2FE,
    isLightProfile = false
  ),
  SUNNY_DAY(
    id = "sunny_day",
    displayName = "Clear Sunny Day Profile",
    subtitle = "Vivid Sky Blue (#4FACFE) to Horizon Cobalt (#00F2FE) with solar flare",
    primaryColorHex = 0xFF00F2FE,
    isLightProfile = false
  ),
  OVERCAST(
    id = "overcast",
    displayName = "Overcast / Cloudy Profile",
    subtitle = "Flat misty slate-gray baseline with layered semi-translucent cloud masks",
    primaryColorHex = 0xFF64748B,
    isLightProfile = false
  ),
  RAIN_THUNDERSTORM(
    id = "rain_thunderstorm",
    displayName = "Rain / Thunderstorm Profile",
    subtitle = "Deep heavy charcoal-teal gradient with rapid vertical rainfall vector strokes",
    primaryColorHex = 0xFF0D2B30,
    isLightProfile = false
  ),
  AMBER_ALERT(
    id = "amber_alert",
    displayName = "Amber Alert Weather Profile",
    subtitle = "Severe emergency weather warning contrast overlay with ambient amber pulse",
    primaryColorHex = 0xFFFFB000,
    isLightProfile = false
  ),
  WINTER(
    id = "winter",
    displayName = "Winter State (Icy Blue)",
    subtitle = "High-contrast Icy Blue gradient with crystalline snowflakes",
    primaryColorHex = 0xFF90E0EF,
    isLightProfile = false
  ),
  SUMMER(
    id = "summer",
    displayName = "Summer State (Fluid Sky Blue)",
    subtitle = "Fluid Sky Blue canvas with dynamic clouds and pulsating sunburst",
    primaryColorHex = 0xFF4FACFE,
    isLightProfile = false
  ),
  SPRING(
    id = "spring",
    displayName = "Spring State (Emerald Morning Sky)",
    subtitle = "Bright emerald-tinted morning sky layout with breeze shimmer",
    primaryColorHex = 0xFF00FF66,
    isLightProfile = false
  );

  companion object {
    fun fromId(id: String?): EnvironmentalSeason {
      return entries.firstOrNull { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
        ?: AUTO
    }

    /**
     * Resolves season based on the local device system clock calendar month and hour.
     * Dec, Jan, Feb -> Winter
     * Mar, Apr, May -> Spring
     * Jun, Jul, Aug -> Summer
     * Sep, Oct, Nov -> Autumn
     * Also checks night hours for clear night transitions when in auto mode.
     */
    fun resolveCurrentSystemSeason(calendar: Calendar = Calendar.getInstance()): EnvironmentalSeason {
      val hour = calendar.get(Calendar.HOUR_OF_DAY)
      if (hour < 6 || hour >= 21) {
        return MIDNIGHT_WORKSPACE
      }
      return when (calendar.get(Calendar.MONTH)) {
        Calendar.DECEMBER, Calendar.JANUARY, Calendar.FEBRUARY -> WINTER
        Calendar.MARCH, Calendar.APRIL, Calendar.MAY -> SPRING
        Calendar.JUNE, Calendar.JULY, Calendar.AUGUST -> DAYLIGHT_WORKSPACE
        Calendar.SEPTEMBER, Calendar.OCTOBER, Calendar.NOVEMBER -> AUTUMN
        else -> DAYLIGHT_WORKSPACE
      }
    }
  }
}

/**
 * Hourly weather metrics parsed by the Environmental Backdrop Engine.
 */
data class WeatherReport(
  val temperatureCelsius: Int = 26,
  val conditionName: String = "Clear Radiant Sunburst",
  val cloudOpacity: Float = 0.45f,
  val sunburstExpansion: Float = 1.15f,
  val windSpeedKmh: Float = 12f,
  val humidityPercentage: Int = 48,
  val isAmberAlertActive: Boolean = false
)

/**
 * Configuration payload for the Animated Environmental Background engine.
 */
data class EnvironmentalBackdropConfig(
  val selectedSeason: EnvironmentalSeason = EnvironmentalSeason.AUTO,
  val enableParticles: Boolean = true,
  val weatherReport: WeatherReport = WeatherReport(),
  val darkGlassMaskOpacity: Float = 0.60f
)
