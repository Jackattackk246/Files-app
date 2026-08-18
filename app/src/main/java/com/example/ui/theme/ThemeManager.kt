package com.jackattackk246.files.ui.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.jackattackk246.files.model.EnvironmentalSeason
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Production Theme & Environmental State Manager v2.4.6.
 *
 * Exact 15 Synchronized Profiles:
 * 01. Classic Black and White — Pure pitch-black (#000000) canvas with stark white typography.
 * 02. Classic White and Black — Solid white (#FFFFFF) canvas with deep black typography.
 * 03. Dynamic Weather Canvas — 180° vertical linear gradient: Sky Blue (#4FACFE) -> Horizon Cobalt (#00F2FE).
 * 04. OLED Pitch Black — Pure black (#000000) for OLED panels.
 * 05. Midnight Matte Black — Dark charcoal canvas (#141414) with subtle slate frames.
 * 06. Spring Emerald — Forest green base (#0A1F0D) with neon green (#00FF66) indicators.
 * 07. Cyberpunk Amber — Deep graphite (#1A1A22) with high-contrast amber yellow (#FFB000).
 * 08. Deep Purple — Royal amethyst (#2E0854) to obsidian void (#080410).
 * 09. Ocean Blue — Maritime sapphire (#0D2B45) with ice blue highlights.
 * 10. Tails' Mechanical Sandbox — Industrial dark slate (#20242C) with metallic bronze (#D97706).
 * 11. Autumn Copper — Deep Burgundy (#4A1525) to Muted Burnt Orange (#A84B24).
 * 12. Crimson Fury — Dark charcoal (#1A1114) with crimson red (#DC2626) borders.
 * 13. Neon Synthwave — Retro violet (#1E1B4B) with hot pink (#F43F5E) rings.
 * 14. Solar Flare — Muted ash-gray (#26262B) with energetic orange (#F97316) accents.
 * 15. Desert Sage — Earthy pale olive-green (#2F3E36) with warm cream typography.
 */
object ThemeManager {
  // Translucent Charcoal Glass Mask
  val GlassMaskCharcoal = Color(0xA6121214)
  val CardContainerSlateCharcoal = Color(0xCC1E1E22)
  val GlassMaskLight = Color(0xCCFFFFFF)

  // High-Visibility Typography Anchors
  val TextPrimaryWhite = Color(0xFFFFFFFF)
  val TextSecondarySilver = Color(0xFFE5E5EA)
  val TextSecondaryWhite = Color(0xFFFFFFFF)
  val TextHighVisibilityWhite = Color(0xFFFFFFFF)
  val TextSolidBlack = Color(0xFF000000)
  val TextSecondaryDarkCharcoal = Color(0xFF1F2937)

  val OutlineBorderGlass = Color(0x33FFFFFF)
  val OutlineBorderGlassDark = Color(0x331C1C1E)

  private val _isBackdropActive = MutableStateFlow(false)
  val isBackdropActive: StateFlow<Boolean> = _isBackdropActive.asStateFlow()

  private val _isThrottled = MutableStateFlow(false)
  val isThrottled: StateFlow<Boolean> = _isThrottled.asStateFlow()

  /**
   * Hard-reset all active background state event listeners.
   */
  fun flushResidualRenderLocks() {
    _isBackdropActive.value = false
    _isThrottled.value = false
  }

  /**
   * Evaluates whether the dynamic environmental canvas should be mounted.
   */
  fun shouldMountBackdropCanvas(themeMode: AppThemeMode): Boolean {
    val shouldMount = themeMode == AppThemeMode.DYNAMIC_WEATHER_CANVAS
    _isBackdropActive.value = shouldMount
    return shouldMount
  }

  fun setThermalThrottling(throttled: Boolean) {
    _isThrottled.value = throttled
  }

  /**
   * 180-Degree Vertical Linear Gradient Mapping per theme mode.
   * Shift smoothly from top edge down to bottom edge across all 15 themes.
   */
  fun getThemeVerticalGradient(
    themeMode: AppThemeMode,
    season: EnvironmentalSeason = EnvironmentalSeason.AUTO
  ): Brush {
    return when (themeMode) {
      AppThemeMode.CLASSIC_BLACK_WHITE -> Brush.verticalGradient(
        colors = listOf(
          Color(0xFF000000), // Pure Black (Top 0.0)
          Color(0xFF000000)  // Pure Black (Bottom 1.0)
        )
      )
      AppThemeMode.CLASSIC_WHITE_BLACK -> Brush.verticalGradient(
        colors = listOf(
          Color(0xFFFFFFFF), // Solid White (Top 0.0)
          Color(0xFFF8FAFC)  // Crisp Off-White (Bottom 1.0)
        )
      )
      AppThemeMode.DYNAMIC_WEATHER_CANVAS -> Brush.verticalGradient(
        colors = listOf(
          Color(0xFF4FACFE), // Vivid Sky Blue (Top 0.0)
          Color(0xFF00F2FE)  // Deep Horizon Cobalt (Bottom 1.0)
        )
      )
      AppThemeMode.PITCH_BLACK_OLED -> Brush.verticalGradient(
        colors = listOf(
          Color(0xFF000000), // Pure Black (Top 0.0)
          Color(0xFF000000)  // Pure Black (Bottom 1.0)
        )
      )
      AppThemeMode.MIDNIGHT_MATTE_BLACK -> Brush.verticalGradient(
        colors = listOf(
          Color(0xFF141414), // Dark Charcoal (Top 0.0)
          Color(0xFF000000)  // Jet Black (Bottom 1.0)
        )
      )
      AppThemeMode.SPRING_EMERALD -> Brush.verticalGradient(
        colors = listOf(
          Color(0xFF0A1F0D), // Forest Green Base (Top 0.0)
          Color(0xFF000000)  // Pitch Black (Bottom 1.0)
        )
      )
      AppThemeMode.CYBERPUNK_AMBER -> Brush.verticalGradient(
        colors = listOf(
          Color(0xFF1A1A22), // Deep Graphite (Top 0.0)
          Color(0xFF0D0D12)  // Void Charcoal (Bottom 1.0)
        )
      )
      AppThemeMode.DEEP_PURPLE -> Brush.verticalGradient(
        colors = listOf(
          Color(0xFF2E0854), // Royal Amethyst (Top 0.0)
          Color(0xFF080410)  // Obsidian Void (Bottom 1.0)
        )
      )
      AppThemeMode.OCEAN_BLUE -> Brush.verticalGradient(
        colors = listOf(
          Color(0xFF0D2B45), // Maritime Sapphire (Top 0.0)
          Color(0xFF040D18)  // Abyssal Black (Bottom 1.0)
        )
      )
      AppThemeMode.TAILS_MECHANICAL_SANDBOX -> Brush.verticalGradient(
        colors = listOf(
          Color(0xFF20242C), // Industrial Slate Gray (Top 0.0)
          Color(0xFF12151B)  // Brushed Iron Black (Bottom 1.0)
        )
      )
      AppThemeMode.AUTUMN_COPPER -> Brush.verticalGradient(
        colors = listOf(
          Color(0xFF4A1525), // Deep Burgundy (Top 0.0)
          Color(0xFF20090F)  // Muted Burnt Void (Bottom 1.0)
        )
      )
      AppThemeMode.CRIMSON_FURY -> Brush.verticalGradient(
        colors = listOf(
          Color(0xFF1E0E12), // Dark Crimson Charcoal (Top 0.0)
          Color(0xFF000000)  // Pitch Black (Bottom 1.0)
        )
      )
      AppThemeMode.NEON_SYNTHWAVE -> Brush.verticalGradient(
        colors = listOf(
          Color(0xFF1E1B4B), // Retro Dark Violet (Top 0.0)
          Color(0xFF0B0A1C)  // Dark Indigo Void (Bottom 1.0)
        )
      )
      AppThemeMode.SOLAR_FLARE -> Brush.verticalGradient(
        colors = listOf(
          Color(0xFF26262B), // Muted Ash-Gray (Top 0.0)
          Color(0xFF131316)  // Deep Ash Charcoal (Bottom 1.0)
        )
      )
      AppThemeMode.DESERT_SAGE -> Brush.verticalGradient(
        colors = listOf(
          Color(0xFF2F3E36), // Earthy Pale Olive Green (Top 0.0)
          Color(0xFF151C18)  // Deep Moss Charcoal (Bottom 1.0)
        )
      )
      AppThemeMode.SAMSUNG_EXPERIENCE -> Brush.verticalGradient(
        colors = listOf(
          Color(0xFF0F111A), // Deep Midnight Galaxy Void (Top 0.0)
          Color(0xFF1E2022)  // Matte Dark Charcoal Core (Bottom 1.0)
        )
      )
    }
  }

  /**
   * Primary accent color token for each of the 16 theme slots.
   */
  fun getThemeAccentColor(themeMode: AppThemeMode, customAccentColor: Color? = null): Color {
    if (customAccentColor != null) return customAccentColor
    return when (themeMode) {
      AppThemeMode.CLASSIC_BLACK_WHITE -> Color(0xFFFFFFFF)
      AppThemeMode.CLASSIC_WHITE_BLACK -> Color(0xFF000000)
      AppThemeMode.DYNAMIC_WEATHER_CANVAS -> Color(0xFF00F2FE)
      AppThemeMode.PITCH_BLACK_OLED -> Color(0xFF38BDF8)
      AppThemeMode.MIDNIGHT_MATTE_BLACK -> Color(0xFF38BDF8)
      AppThemeMode.SPRING_EMERALD -> Color(0xFF00FF66)  // Vivid Neon Green (#00FF66)
      AppThemeMode.CYBERPUNK_AMBER -> Color(0xFFFFB000) // High-contrast Amber (#FFB000)
      AppThemeMode.DEEP_PURPLE -> Color(0xFFC084FC)    // Vivid Regal Purple (#C084FC)
      AppThemeMode.OCEAN_BLUE -> Color(0xFF38BDF8)     // Ice Blue (#38BDF8)
      AppThemeMode.TAILS_MECHANICAL_SANDBOX -> Color(0xFFD97706) // Metallic Bronze (#D97706)
      AppThemeMode.AUTUMN_COPPER -> Color(0xFFEA580C)  // Warm Amber Copper (#EA580C)
      AppThemeMode.CRIMSON_FURY -> Color(0xFFDC2626)   // Striking Crimson Red (#DC2626)
      AppThemeMode.NEON_SYNTHWAVE -> Color(0xFFF43F5E) // Radiant Hot Pink (#F43F5E)
      AppThemeMode.SOLAR_FLARE -> Color(0xFFF97316)    // Energetic Orange (#F97316)
      AppThemeMode.DESERT_SAGE -> Color(0xFF84CC16)    // Pale Olive Lime (#84CC16)
      AppThemeMode.SAMSUNG_EXPERIENCE -> Color(0xFF1473E6) // Dream UX Blue (#1473E6)
    }
  }

  /**
   * Theme-check logic loop:
   * Returns true if dynamic weather canvas or Classic White and Black is active.
   */
  fun isLightBackgroundProfile(themeMode: AppThemeMode, season: EnvironmentalSeason = EnvironmentalSeason.AUTO): Boolean {
    return themeMode == AppThemeMode.DYNAMIC_WEATHER_CANVAS || themeMode == AppThemeMode.CLASSIC_WHITE_BLACK
  }

  /**
   * Primary Typography Color:
   * Razor-sharp Solid Black (#000000) on Dynamic Weather Canvas and Classic White and Black,
   * 100% Crisp High-Visibility White (#FFFFFF) universally on all dark presets.
   */
  fun getAdaptivePrimaryTextColor(
    themeMode: AppThemeMode,
    season: EnvironmentalSeason = EnvironmentalSeason.AUTO
  ): Color {
    return if (isLightBackgroundProfile(themeMode, season)) {
      TextSolidBlack
    } else {
      TextPrimaryWhite
    }
  }

  /**
   * Secondary Typography & Sub-navigation Details Color:
   * Razor-sharp Solid Black / Charcoal on light backdrops,
   * 100% Crisp High-Visibility White / Silver on dark backdrops.
   */
  fun getAdaptiveSecondaryTextColor(
    themeMode: AppThemeMode,
    season: EnvironmentalSeason = EnvironmentalSeason.AUTO
  ): Color {
    return if (isLightBackgroundProfile(themeMode, season)) {
      TextSecondaryDarkCharcoal
    } else {
      TextSecondarySilver
    }
  }

  /**
   * Sub-Text, Button Descriptors & Item Descriptions Color:
   */
  fun getAdaptiveSubTextColor(
    themeMode: AppThemeMode,
    season: EnvironmentalSeason = EnvironmentalSeason.AUTO
  ): Color {
    return if (isLightBackgroundProfile(themeMode, season)) {
      TextSecondaryDarkCharcoal
    } else {
      TextSecondarySilver
    }
  }

  fun getAdaptiveMetricsTrackColor(
    themeMode: AppThemeMode,
    season: EnvironmentalSeason = EnvironmentalSeason.AUTO
  ): Color {
    return if (isLightBackgroundProfile(themeMode, season)) {
      Color(0x33000000)
    } else {
      Color(0x44FFFFFF)
    }
  }

  fun getAdaptiveMetricsFillColor(
    themeMode: AppThemeMode,
    season: EnvironmentalSeason = EnvironmentalSeason.AUTO
  ): Color {
    return getThemeAccentColor(themeMode)
  }

  /**
   * Card container color:
   * Semitransparent Light Glass on light backdrops,
   * Customized translucent charcoal/tinted tone per theme to prevent any mismatched slate blocks.
   */
  fun getAdaptiveCardContainerColor(
    themeMode: AppThemeMode,
    season: EnvironmentalSeason = EnvironmentalSeason.AUTO
  ): Color {
    return when (themeMode) {
      AppThemeMode.CLASSIC_WHITE_BLACK -> Color(0xEEFFFFFF)
      AppThemeMode.DYNAMIC_WEATHER_CANVAS -> Color(0xCCFFFFFF)
      AppThemeMode.CLASSIC_BLACK_WHITE -> Color(0xCC111111)
      AppThemeMode.PITCH_BLACK_OLED -> Color(0xCC121214)
      AppThemeMode.MIDNIGHT_MATTE_BLACK -> Color(0xCC1E1E22)
      AppThemeMode.SPRING_EMERALD -> Color(0xCC0F2412)
      AppThemeMode.CYBERPUNK_AMBER -> Color(0xCC1E1E24)
      AppThemeMode.DEEP_PURPLE -> Color(0xCC200E38)
      AppThemeMode.OCEAN_BLUE -> Color(0xCC0E2338)
      AppThemeMode.TAILS_MECHANICAL_SANDBOX -> Color(0xCC252A34)
      AppThemeMode.AUTUMN_COPPER -> Color(0xCC3D1520)
      AppThemeMode.CRIMSON_FURY -> Color(0xCC261418)
      AppThemeMode.NEON_SYNTHWAVE -> Color(0xCC241E4E)
      AppThemeMode.SOLAR_FLARE -> Color(0xCC2A2A30)
      AppThemeMode.DESERT_SAGE -> Color(0xCC2A3831)
      AppThemeMode.SAMSUNG_EXPERIENCE -> Color(0xCC1E2022)
    }
  }

  fun getAdaptiveCardBorderColor(
    themeMode: AppThemeMode,
    season: EnvironmentalSeason = EnvironmentalSeason.AUTO
  ): Color {
    return when (themeMode) {
      AppThemeMode.CLASSIC_BLACK_WHITE -> Color(0x66FFFFFF)
      AppThemeMode.CLASSIC_WHITE_BLACK -> Color(0x33000000)
      AppThemeMode.DYNAMIC_WEATHER_CANVAS -> Color(0x33000000)
      AppThemeMode.PITCH_BLACK_OLED -> Color(0x3338BDF8)
      AppThemeMode.MIDNIGHT_MATTE_BLACK -> Color(0x3338BDF8)
      AppThemeMode.SPRING_EMERALD -> Color(0x6600FF66)
      AppThemeMode.CYBERPUNK_AMBER -> Color(0x66FFB000)
      AppThemeMode.DEEP_PURPLE -> Color(0x66C084FC)
      AppThemeMode.OCEAN_BLUE -> Color(0x6638BDF8)
      AppThemeMode.TAILS_MECHANICAL_SANDBOX -> Color(0x66D97706)
      AppThemeMode.AUTUMN_COPPER -> Color(0x66EA580C)
      AppThemeMode.CRIMSON_FURY -> Color(0x66DC2626)
      AppThemeMode.NEON_SYNTHWAVE -> Color(0x66F43F5E)
      AppThemeMode.SOLAR_FLARE -> Color(0x66F97316)
      AppThemeMode.DESERT_SAGE -> Color(0x6684CC16)
      AppThemeMode.SAMSUNG_EXPERIENCE -> Color(0x448EC5FC)
    }
  }

  fun getAdaptiveTopBarColor(
    themeMode: AppThemeMode,
    season: EnvironmentalSeason = EnvironmentalSeason.AUTO
  ): Color {
    return if (isLightBackgroundProfile(themeMode, season)) {
      Color(0xCCFFFFFF)
    } else {
      GlassMaskCharcoal
    }
  }
}
