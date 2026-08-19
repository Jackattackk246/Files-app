package com.jackattackk246.files.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.jackattackk246.files.model.EnvironmentalSeason

enum class AppThemeMode(
  val id: String,
  val displayName: String,
  val subtitle: String,
  val defaultAccentHex: Long
) {
  CLASSIC_BLACK_WHITE(
    "classic_black_white",
    "Classic Black and White",
    "Stark white typography over a pure pitch-black (#000000) canvas container.",
    0xFFFFFFFF
  ),
  CLASSIC_WHITE_BLACK(
    "classic_white_black",
    "Classic White and Black",
    "High-contrast deep black typography over a clean, solid white (#FFFFFF) canvas layout.",
    0xFF000000
  ),
  PITCH_BLACK_OLED(
    "pitch_black_oled",
    "OLED Pitch Black",
    "Pure black backdrop (#000000) optimized for extreme panel power savings.",
    0xFF38BDF8
  ),
  MIDNIGHT_MATTE_BLACK(
    "midnight_matte_black",
    "Midnight Matte Black",
    "Smooth dark charcoal canvas (#141414) with subtle slate contrast frames.",
    0xFF38BDF8
  ),
  SPRING_EMERALD(
    "spring_emerald",
    "Spring Emerald",
    "Rich forest green backdrop (#0A1F0D) with vivid neon green (#00FF66) active radio indicators.",
    0xFF00FF66
  ),
  CYBERPUNK_AMBER(
    "cyberpunk_amber",
    "Cyberpunk Amber",
    "Deep graphite backing with high-saturation amber yellow (#FFB000) structural highlight borders.",
    0xFFFFB000
  ),
  DEEP_PURPLE(
    "deep_purple",
    "Deep Purple",
    "Royal amethyst gradient (#2E0854) shifting down into a deep obsidian void.",
    0xFFC084FC
  ),
  OCEAN_BLUE(
    "ocean_blue",
    "Ocean Blue",
    "Deep maritime sapphire canvas (#0D2B45) with ice blue highlights.",
    0xFF38BDF8
  ),
  TAILS_MECHANICAL_SANDBOX(
    "tails_mechanical_sandbox",
    "Tails' Mechanical Sandbox",
    "A crisp cell-shaded industrial gray and brushed metallic bronze architectural layout grid.",
    0xFFD97706
  ),
  AUTUMN_COPPER(
    "autumn_copper",
    "Autumn Copper",
    "A warm ambient gradient shifting smoothly from Deep Burgundy (#4A1525) down to Muted Burnt Orange (#A84B24).",
    0xFFEA580C
  ),
  CRIMSON_FURY(
    "crimson_fury",
    "Crimson Fury",
    "Aggressive dark charcoal backing with striking crimson red (#DC2626) structural borders.",
    0xFFDC2626
  ),
  NEON_SYNTHWAVE(
    "neon_synthwave",
    "Neon Synthwave",
    "Dark retro violet canvas (#1E1B4B) with radiant hot pink (#F43F5E) accent rings.",
    0xFFF43F5E
  ),
  SOLAR_FLARE(
    "solar_flare",
    "Solar Flare",
    "Muted ash-gray backdrop featuring an intense energetic orange (#F97316) primary layout accent.",
    0xFFF97316
  ),
  DESERT_SAGE(
    "desert_sage",
    "Desert Sage",
    "A clean, earthy pale olive-green backdrop canvas (#2F3E36) with warm cream typography accents.",
    0xFF84CC16
  ),
  SAMSUNG_EXPERIENCE(
    "samsung_experience",
    "Samsung Experience",
    "Classic Dream UX palette with Deep Midnight Galaxy void (#0F111A), Dream Blue (#1473E6) and Samsung Orange (#FF9500) accents.",
    0xFF1473E6
  );

  // Backward compatibility alias & lookup
  companion object {
    // Aliases for backward compatibility with previous versions
    val MATRIX_GREEN = SPRING_EMERALD
    val DEFENSIVE_BASELINE = MIDNIGHT_MATTE_BLACK
    val DARK_STANDARD = MIDNIGHT_MATTE_BLACK
    val SYSTEM = MIDNIGHT_MATTE_BLACK

    fun fromId(id: String?): AppThemeMode {
      if (id == null) return MIDNIGHT_MATTE_BLACK
      if (id.equals("matrix_green", ignoreCase = true)) return SPRING_EMERALD
      if (id.equals("defensive_baseline", ignoreCase = true) || id.equals("dark_standard", ignoreCase = true) || id.equals("system", ignoreCase = true)) return MIDNIGHT_MATTE_BLACK
      return entries.firstOrNull { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
        ?: MIDNIGHT_MATTE_BLACK
    }
  }
}

// 01. Classic Black and White Scheme (Stark white typography over pure pitch-black #000000)
private val ClassicBlackWhiteColorScheme = darkColorScheme(
  primary = Color(0xFFFFFFFF),
  onPrimary = Color(0xFF000000),
  primaryContainer = Color(0xFF222222),
  onPrimaryContainer = Color(0xFFFFFFFF),
  secondary = Color(0xFFFFFFFF),
  onSecondary = Color(0xFF000000),
  background = Color(0xFF000000),
  onBackground = Color(0xFFFFFFFF),
  surface = Color(0xCC111111),
  onSurface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xCC1A1A1A),
  onSurfaceVariant = Color(0xFFE5E5EA),
  outline = Color(0x66FFFFFF),
  outlineVariant = Color(0x33FFFFFF)
)

// 02. Classic White and Black Scheme (Solid white canvas with high-contrast deep black typography)
private val ClassicWhiteBlackColorScheme = lightColorScheme(
  primary = Color(0xFF000000),
  onPrimary = Color(0xFFFFFFFF),
  primaryContainer = Color(0xFFE5E5E5),
  onPrimaryContainer = Color(0xFF000000),
  secondary = Color(0xFF000000),
  onSecondary = Color(0xFFFFFFFF),
  background = Color(0xFFFFFFFF),
  onBackground = Color(0xFF000000),
  surface = Color(0xEEFFFFFF),
  onSurface = Color(0xFF000000),
  surfaceVariant = Color(0xFFF0F0F0),
  onSurfaceVariant = Color(0xFF1F2937),
  outline = Color(0x44000000),
  outlineVariant = Color(0x22000000)
)

// 03. Dynamic Weather Canvas Scheme
private val DynamicWeatherCanvasDarkColorScheme = darkColorScheme(
  primary = Color(0xFF00F2FE),
  onPrimary = Color(0xFF001E2E),
  primaryContainer = Color(0x3300F2FE),
  onPrimaryContainer = Color(0xFFFFFFFF),
  secondary = Color(0xFF00F2FE),
  onSecondary = Color(0xFFFFFFFF),
  background = Color.Transparent,
  onBackground = Color(0xFFFFFFFF),
  surface = Color(0xCC1E1E22),
  onSurface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xCC1E1E22),
  onSurfaceVariant = Color(0xFFE5E5EA),
  outline = Color(0x4000F2FE),
  outlineVariant = Color(0x25FFFFFF)
)

private val DynamicWeatherCanvasLightColorScheme = lightColorScheme(
  primary = Color(0xFF0288D1),
  onPrimary = Color(0xFFFFFFFF),
  primaryContainer = Color(0x440288D1),
  onPrimaryContainer = Color(0xFF000000),
  secondary = Color(0xFF0288D1),
  onSecondary = Color(0xFFFFFFFF),
  background = Color.Transparent,
  onBackground = Color(0xFF000000),
  surface = Color(0xCCFFFFFF),
  onSurface = Color(0xFF000000),
  surfaceVariant = Color(0xEEFFFFFF),
  onSurfaceVariant = Color(0xFF000000),
  outline = Color(0x44000000),
  outlineVariant = Color(0x22000000)
)

// 04. OLED Pitch Black Scheme
private val OledPitchBlackColorScheme = darkColorScheme(
  primary = Color(0xFF38BDF8),
  onPrimary = Color.Black,
  primaryContainer = Color(0xFF18181C),
  onPrimaryContainer = Color.White,
  secondary = Color(0xFF38BDF8),
  onSecondary = Color.White,
  background = Color(0xFF000000),
  onBackground = Color(0xFFFFFFFF),
  surface = Color(0xCC121214),
  onSurface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xCC18181C),
  onSurfaceVariant = Color(0xFFE5E5EA),
  outline = Color(0x4438BDF8),
  outlineVariant = Color(0x22FFFFFF)
)

// 05. Midnight Matte Black Scheme
private val MidnightMatteBlackColorScheme = darkColorScheme(
  primary = Color(0xFF38BDF8),
  onPrimary = Color(0xFF0A0A0F),
  primaryContainer = Color(0xFF242436),
  onPrimaryContainer = Color(0xFFFFFFFF),
  secondary = Color(0xFF38BDF8),
  onSecondary = Color(0xFFFFFFFF),
  background = Color(0xFF141414),
  onBackground = Color(0xFFFFFFFF),
  surface = Color(0xCC1E1E22),
  onSurface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xCC1E1E22),
  onSurfaceVariant = Color(0xFFE5E5EA),
  outline = Color(0x4038BDF8),
  outlineVariant = Color(0x25FFFFFF)
)

// 06. Spring Emerald Scheme
private val SpringEmeraldColorScheme = darkColorScheme(
  primary = Color(0xFF00FF66),
  onPrimary = Color(0xFF02260B),
  primaryContainer = Color(0xFF0E3819),
  onPrimaryContainer = Color(0xFFFFFFFF),
  secondary = Color(0xFF00FF66),
  onSecondary = Color(0xFFFFFFFF),
  background = Color(0xFF0A1F0D),
  onBackground = Color(0xFFFFFFFF),
  surface = Color(0xCC0F2412),
  onSurface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xCC142E18),
  onSurfaceVariant = Color(0xFFE5E5EA),
  outline = Color(0xFF00FF66),
  outlineVariant = Color(0x4000FF66)
)

// 07. Cyberpunk Amber Scheme
private val CyberpunkAmberColorScheme = darkColorScheme(
  primary = Color(0xFFFFB000),
  onPrimary = Color(0xFF2A1B00),
  primaryContainer = Color(0xFF3D2700),
  onPrimaryContainer = Color(0xFFFFFFFF),
  secondary = Color(0xFFFFB000),
  onSecondary = Color(0xFFFFFFFF),
  background = Color(0xFF141418),
  onBackground = Color(0xFFFFFFFF),
  surface = Color(0xCC1E1E24),
  onSurface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xCC262630),
  onSurfaceVariant = Color(0xFFE5E5EA),
  outline = Color(0xFFFFB000),
  outlineVariant = Color(0x55FFB000)
)

// 08. Deep Purple Scheme
private val DeepPurpleColorScheme = darkColorScheme(
  primary = Color(0xFFC084FC),
  onPrimary = Color(0xFF2E0061),
  primaryContainer = Color(0xFF451978),
  onPrimaryContainer = Color(0xFFFFFFFF),
  secondary = Color(0xFFC084FC),
  onSecondary = Color(0xFFFFFFFF),
  background = Color(0xFF0B0716),
  onBackground = Color(0xFFFFFFFF),
  surface = Color(0xCC200E38),
  onSurface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xCC2A1448),
  onSurfaceVariant = Color(0xFFE5E5EA),
  outline = Color(0xFFC084FC),
  outlineVariant = Color(0x40C084FC)
)

// 09. Ocean Blue Scheme
private val OceanBlueColorScheme = darkColorScheme(
  primary = Color(0xFF38BDF8),
  onPrimary = Color(0xFF032840),
  primaryContainer = Color(0xFF0C426D),
  onPrimaryContainer = Color(0xFFFFFFFF),
  secondary = Color(0xFF38BDF8),
  onSecondary = Color(0xFFFFFFFF),
  background = Color(0xFF060E1A),
  onBackground = Color(0xFFFFFFFF),
  surface = Color(0xCC0E2338),
  onSurface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xCC122E4A),
  onSurfaceVariant = Color(0xFFE5E5EA),
  outline = Color(0xFF38BDF8),
  outlineVariant = Color(0x4038BDF8)
)

// 10. Tails' Mechanical Sandbox Scheme
private val TailsMechanicalSandboxColorScheme = darkColorScheme(
  primary = Color(0xFFD97706),
  onPrimary = Color(0xFF1E1303),
  primaryContainer = Color(0xFF3B2206),
  onPrimaryContainer = Color(0xFFFFFFFF),
  secondary = Color(0xFFF59E0B),
  onSecondary = Color(0xFFFFFFFF),
  background = Color(0xFF161A22),
  onBackground = Color(0xFFFFFFFF),
  surface = Color(0xCC252A34),
  onSurface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xCC303642),
  onSurfaceVariant = Color(0xFFE5E5EA),
  outline = Color(0xFFD97706),
  outlineVariant = Color(0x55D97706)
)

// 11. Autumn Copper Scheme
private val AutumnCopperColorScheme = darkColorScheme(
  primary = Color(0xFFEA580C),
  onPrimary = Color(0xFF2A0D03),
  primaryContainer = Color(0xFF4A1E0B),
  onPrimaryContainer = Color(0xFFFFFFFF),
  secondary = Color(0xFFF97316),
  onSecondary = Color(0xFFFFFFFF),
  background = Color(0xFF2D0E17),
  onBackground = Color(0xFFFFFFFF),
  surface = Color(0xCC3D1520),
  onSurface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xCC4E1C2A),
  onSurfaceVariant = Color(0xFFE5E5EA),
  outline = Color(0xFFEA580C),
  outlineVariant = Color(0x55EA580C)
)

// 12. Crimson Fury Scheme
private val CrimsonFuryColorScheme = darkColorScheme(
  primary = Color(0xFFDC2626),
  onPrimary = Color(0xFF2E0404),
  primaryContainer = Color(0xFF4B0E0E),
  onPrimaryContainer = Color(0xFFFFFFFF),
  secondary = Color(0xFFEF4444),
  onSecondary = Color(0xFFFFFFFF),
  background = Color(0xFF140D0E),
  onBackground = Color(0xFFFFFFFF),
  surface = Color(0xCC261418),
  onSurface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xCC341A20),
  onSurfaceVariant = Color(0xFFE5E5EA),
  outline = Color(0xFFDC2626),
  outlineVariant = Color(0x55DC2626)
)

// 13. Neon Synthwave Scheme
private val NeonSynthwaveColorScheme = darkColorScheme(
  primary = Color(0xFFF43F5E),
  onPrimary = Color(0xFF2B030D),
  primaryContainer = Color(0xFF4C0B1B),
  onPrimaryContainer = Color(0xFFFFFFFF),
  secondary = Color(0xFFEC4899),
  onSecondary = Color(0xFFFFFFFF),
  background = Color(0xFF131032),
  onBackground = Color(0xFFFFFFFF),
  surface = Color(0xCC241E4E),
  onSurface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xCC2F2764),
  onSurfaceVariant = Color(0xFFE5E5EA),
  outline = Color(0xFFF43F5E),
  outlineVariant = Color(0x55F43F5E)
)

// 14. Solar Flare Scheme
private val SolarFlareColorScheme = darkColorScheme(
  primary = Color(0xFFF97316),
  onPrimary = Color(0xFF2E1202),
  primaryContainer = Color(0xFF4C2006),
  onPrimaryContainer = Color(0xFFFFFFFF),
  secondary = Color(0xFFFB923C),
  onSecondary = Color(0xFFFFFFFF),
  background = Color(0xFF1A1A1E),
  onBackground = Color(0xFFFFFFFF),
  surface = Color(0xCC2A2A30),
  onSurface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xCC36363E),
  onSurfaceVariant = Color(0xFFE5E5EA),
  outline = Color(0xFFF97316),
  outlineVariant = Color(0x55F97316)
)

// 15. Desert Sage Scheme
private val DesertSageColorScheme = darkColorScheme(
  primary = Color(0xFF84CC16),
  onPrimary = Color(0xFF172803),
  primaryContainer = Color(0xFF2C4408),
  onPrimaryContainer = Color(0xFFFFFFFF),
  secondary = Color(0xFFA3E635),
  onSecondary = Color(0xFFFFFFFF),
  background = Color(0xFF1A241F),
  onBackground = Color(0xFFFFFFFF),
  surface = Color(0xCC2A3831),
  onSurface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xCC35483F),
  onSurfaceVariant = Color(0xFFE5E5EA),
  outline = Color(0xFF84CC16),
  outlineVariant = Color(0x5584CC16)
)

// 16. Samsung Experience Scheme
private val SamsungExperienceColorScheme = darkColorScheme(
  primary = Color(0xFF1473E6),
  onPrimary = Color.White,
  primaryContainer = Color(0xFF1E2022),
  onPrimaryContainer = Color(0xFF8EC5FC),
  secondary = Color(0xFFFF9500),
  onSecondary = Color.Black,
  background = Color(0xFF0F111A),
  onBackground = Color(0xFFF1F5F9),
  surface = Color(0xCC1E2022),
  onSurface = Color(0xFFF1F5F9),
  surfaceVariant = Color(0xFF25282C),
  onSurfaceVariant = Color(0xFF94A3B8),
  outline = Color(0x448EC5FC),
  outlineVariant = Color(0x228EC5FC)
)

@Composable
fun MyApplicationTheme(
  themeMode: AppThemeMode = AppThemeMode.MIDNIGHT_MATTE_BLACK,
  customAccentColor: Color? = null,
  season: EnvironmentalSeason = EnvironmentalSeason.AUTO,
  content: @Composable () -> Unit
) {
  val baseScheme = when (themeMode) {
    AppThemeMode.CLASSIC_BLACK_WHITE -> ClassicBlackWhiteColorScheme
    AppThemeMode.CLASSIC_WHITE_BLACK -> ClassicWhiteBlackColorScheme
    AppThemeMode.PITCH_BLACK_OLED -> OledPitchBlackColorScheme
    AppThemeMode.MIDNIGHT_MATTE_BLACK -> MidnightMatteBlackColorScheme
    AppThemeMode.SPRING_EMERALD -> SpringEmeraldColorScheme
    AppThemeMode.CYBERPUNK_AMBER -> CyberpunkAmberColorScheme
    AppThemeMode.DEEP_PURPLE -> DeepPurpleColorScheme
    AppThemeMode.OCEAN_BLUE -> OceanBlueColorScheme
    AppThemeMode.TAILS_MECHANICAL_SANDBOX -> TailsMechanicalSandboxColorScheme
    AppThemeMode.AUTUMN_COPPER -> AutumnCopperColorScheme
    AppThemeMode.CRIMSON_FURY -> CrimsonFuryColorScheme
    AppThemeMode.NEON_SYNTHWAVE -> NeonSynthwaveColorScheme
    AppThemeMode.SOLAR_FLARE -> SolarFlareColorScheme
    AppThemeMode.DESERT_SAGE -> DesertSageColorScheme
    AppThemeMode.SAMSUNG_EXPERIENCE -> SamsungExperienceColorScheme
    else -> MidnightMatteBlackColorScheme
  }

  val finalColorScheme = if (customAccentColor != null) {
    baseScheme.copy(
      primary = customAccentColor,
      primaryContainer = customAccentColor.copy(alpha = 0.35f),
      outline = customAccentColor.copy(alpha = 0.5f)
    )
  } else baseScheme

  MaterialTheme(
    colorScheme = finalColorScheme,
    typography = Typography,
    content = content
  )
}
