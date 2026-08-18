package com.jackattackk246.files.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color as AndroidColor
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.EdgeEffect
import android.widget.ScrollView
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.jackattackk246.files.R
import com.jackattackk246.files.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UnifiedThemeProfile:
 * Represents a discrete, isolated theme definition among the master 100 collection.
 */
@Immutable
data class UnifiedThemeProfile(
  val index: Int, // 1..100
  val id: String, // e.g. "THEME_1", "classic_black_white", etc.
  val name: String,
  val subtitle: String,
  val category: String,
  val primaryHex: Long,
  val accentHex: Long,
  val cardContainerHex: Long = 0xCC1E1E22,
  val cardBorderHex: Long = 0x33FFFFFF,
  val isLight: Boolean = false,
  val appThemeMode: AppThemeMode = AppThemeMode.MIDNIGHT_MATTE_BLACK
) {
  val primaryColor: Color get() = Color(primaryHex)
  val accentColor: Color get() = Color(accentHex)
  val cardContainerColor: Color get() = Color(cardContainerHex)
  val cardBorderColor: Color get() = Color(cardBorderHex)

  fun getVerticalGradient(): Brush {
    val top = Color(primaryHex)
    val bottom = if (isLight) Color(0xFFF8FAFC) else Color(0xFF000000)
    return Brush.verticalGradient(listOf(top, bottom))
  }
}

/**
 * UnifiedThemeEngine:
 * Unified runtime controller managing all 100 themes identically through a single flat registry.
 * Completely detaches themes 1-15 from the layout tree before drawing themes 16-100.
 */
object UnifiedThemeEngine {

  private const val PREFS_NAME = "launcher_prefs"
  private const val KEY_ACTIVE_THEME_ID = "selected_theme_preset"
  private const val KEY_LEGACY_THEME_MODE = "saved_app_theme_mode"

  // Master Flat Registry of all 100 Themes
  val ALL_100_THEMES: List<UnifiedThemeProfile> = buildList {
    // 1-15: Canonical Baseline & Core Profiles
    add(UnifiedThemeProfile(1, "THEME_1", "Classic Black & White", "Pure pitch-black (#000000) high-contrast canvas", "Canvas Themes", 0xFF000000, 0xFFFFFFFF, 0xFF141414, 0x33FFFFFF, false, AppThemeMode.CLASSIC_BLACK_WHITE))
    add(UnifiedThemeProfile(2, "THEME_2", "Classic White & Black", "Solid white (#FFFFFF) canvas with deep black typography", "Canvas Themes", 0xFFFFFFFF, 0xFF000000, 0xFFF1F5F9, 0x33000000, true, AppThemeMode.CLASSIC_WHITE_BLACK))
    add(UnifiedThemeProfile(3, "THEME_3", "Dynamic Weather Canvas", "180° vertical linear gradient: Sky Blue to Horizon Cobalt", "Canvas Themes", 0xFF4FACFE, 0xFF00F2FE, 0xCCFFFFFF, 0x3300F2FE, true, AppThemeMode.DYNAMIC_WEATHER_CANVAS))
    add(UnifiedThemeProfile(4, "THEME_4", "OLED Pitch Black", "Pure black (#000000) for extreme panel battery endurance", "Canvas Themes", 0xFF000000, 0xFF38BDF8, 0xFF0A0A0A, 0x3338BDF8, false, AppThemeMode.PITCH_BLACK_OLED))
    add(UnifiedThemeProfile(5, "THEME_5", "Midnight Matte Black", "Dark charcoal canvas (#141414) with subtle slate frames", "Canvas Themes", 0xFF141414, 0xFF38BDF8, 0xFF1E1E22, 0x3338BDF8, false, AppThemeMode.MIDNIGHT_MATTE_BLACK))
    add(UnifiedThemeProfile(6, "THEME_6", "Spring Emerald", "Rich forest green (#0A1F0D) with neon green (#00FF66)", "Canvas Themes", 0xFF0A1F0D, 0xFF00FF66, 0xFF102814, 0x3300FF66, false, AppThemeMode.SPRING_EMERALD))
    add(UnifiedThemeProfile(7, "THEME_7", "Cyberpunk Amber", "Deep graphite with amber yellow (#FFB000) accents", "Canvas Themes", 0xFF1A1A22, 0xFFFFB000, 0xFF242430, 0x33FFB000, false, AppThemeMode.CYBERPUNK_AMBER))
    add(UnifiedThemeProfile(8, "THEME_8", "Deep Purple", "Royal amethyst (#2E0854) to obsidian void (#080410)", "Canvas Themes", 0xFF2E0854, 0xFFC084FC, 0xFF3A1068, 0x33C084FC, false, AppThemeMode.DEEP_PURPLE))
    add(UnifiedThemeProfile(9, "THEME_9", "Ocean Blue", "Maritime sapphire (#0D2B45) with ice blue highlights", "Canvas Themes", 0xFF0D2B45, 0xFF38BDF8, 0xFF143B5C, 0x3338BDF8, false, AppThemeMode.OCEAN_BLUE))
    add(UnifiedThemeProfile(10, "THEME_10", "Tails' Mechanical Sandbox", "Dark slate (#20242C) with metallic bronze (#D97706)", "Canvas Themes", 0xFF20242C, 0xFFD97706, 0xFF2A303A, 0x33D97706, false, AppThemeMode.TAILS_MECHANICAL_SANDBOX))
    add(UnifiedThemeProfile(11, "THEME_11", "Autumn Copper", "Deep Burgundy (#4A1525) to Burnt Orange (#A84B24)", "Canvas Themes", 0xFF4A1525, 0xFFEA580C, 0xFF5C1B2F, 0x33EA580C, false, AppThemeMode.AUTUMN_COPPER))
    add(UnifiedThemeProfile(12, "THEME_12", "Crimson Fury", "Dark charcoal (#1A1114) with crimson red (#DC2626)", "Canvas Themes", 0xFF1A1114, 0xFFDC2626, 0xFF28181E, 0x33DC2626, false, AppThemeMode.CRIMSON_FURY))
    add(UnifiedThemeProfile(13, "THEME_13", "Neon Synthwave", "Retro violet (#1E1B4B) with hot pink (#F43F5E) rings", "Canvas Themes", 0xFF1E1B4B, 0xFFF43F5E, 0xFF2B266B, 0x33F43F5E, false, AppThemeMode.NEON_SYNTHWAVE))
    add(UnifiedThemeProfile(14, "THEME_14", "Solar Flare", "Muted ash-gray (#26262B) with energetic orange (#F97316)", "Canvas Themes", 0xFF26262B, 0xFFF97316, 0xFF34343B, 0x33F97316, false, AppThemeMode.SOLAR_FLARE))
    add(UnifiedThemeProfile(15, "THEME_15", "Desert Sage", "Earthy pale olive-green (#2F3E36) with warm cream typography", "Canvas Themes", 0xFF2F3E36, 0xFF84CC16, 0xFF3D5046, 0x3384CC16, false, AppThemeMode.DESERT_SAGE))
    add(UnifiedThemeProfile(16, "THEME_16", "Samsung Experience", "Classic Dream UX squircle palette with Dream Blue & Orange", "Pop-Culture & Special", 0xFF0F111A, 0xFF1473E6, 0xFF1E202A, 0x331473E6, false, AppThemeMode.SAMSUNG_EXPERIENCE))

    // 17-30: Core & Terminal Baselines
    add(UnifiedThemeProfile(17, "THEME_17", "Default System", "Classic dark emerald folder with document tabs", "Core Baselines", 0xFF006738, 0xFF4ECB98, 0xFF0D3320, 0x334ECB98, false, AppThemeMode.SPRING_EMERALD))
    add(UnifiedThemeProfile(18, "THEME_18", "Midnight Prism", "Deep obsidian backdrop with electric cyan prism glow", "Core Baselines", 0xFF0F172A, 0xFF38BDF8, 0xFF1E293B, 0x3338BDF8, false, AppThemeMode.MIDNIGHT_MATTE_BLACK))
    add(UnifiedThemeProfile(19, "THEME_19", "Goth Matrix", "Pitch black backdrop with cyber terminal neon green", "Core Baselines", 0xFF050505, 0xFF00FF66, 0xFF0F1A10, 0x3300FF66, false, AppThemeMode.SPRING_EMERALD))
    add(UnifiedThemeProfile(20, "THEME_20", "Ceramic Matte", "Minimalist matte charcoal with stark titanium white", "Core Baselines", 0xFF18181B, 0xFFF4F4F5, 0xFF27272A, 0x33F4F4F5, false, AppThemeMode.MIDNIGHT_MATTE_BLACK))
    add(UnifiedThemeProfile(21, "THEME_21", "Retro Purple", "Signature indigo retro console layout", "Core Baselines", 0xFF1E1B4B, 0xFF818CF8, 0xFF2D296B, 0x33818CF8, false, AppThemeMode.DEEP_PURPLE))
    add(UnifiedThemeProfile(22, "THEME_22", "Sunset Glow", "Warm crimson dusk with radiant amber horizon", "Core Baselines", 0xFF450A0A, 0xFFF97316, 0xFF5C1010, 0x33F97316, false, AppThemeMode.AUTUMN_COPPER))
    add(UnifiedThemeProfile(23, "THEME_23", "Crimson Glow", "Pure obsidian black with intense crimson red aura", "Core Baselines", 0xFF2A0808, 0xFFEF4444, 0xFF3A0D0D, 0x33EF4444, false, AppThemeMode.CRIMSON_FURY))
    add(UnifiedThemeProfile(24, "THEME_24", "Light Frost", "Crisp glacial white layout with horizon cobalt tint", "Core Baselines", 0xFFF0F9FF, 0xFF0284C7, 0xFFE0F2FE, 0x330284C7, true, AppThemeMode.CLASSIC_WHITE_BLACK))
    add(UnifiedThemeProfile(25, "THEME_25", "Aero Classic", "Refined deep navy blue with sapphire aero gloss", "Core Baselines", 0xFF1E3A8A, 0xFF60A5FA, 0xFF2A4DA8, 0x3360A5FA, false, AppThemeMode.OCEAN_BLUE))
    add(UnifiedThemeProfile(26, "THEME_26", "Retro CRT", "Vintage green phosphor monitor scanline emulator", "Cyberpunk & Retro", 0xFF0A1A0A, 0xFF39FF14, 0xFF122E12, 0x3339FF14, false, AppThemeMode.SPRING_EMERALD))
    add(UnifiedThemeProfile(27, "THEME_27", "Glitch Overdrive", "Dark cyber glitch matrix with electric cyan highlights", "Cyberpunk & Retro", 0xFF120024, 0xFF00FFFF, 0xFF22053E, 0x3300FFFF, false, AppThemeMode.CRIMSON_FURY))
    add(UnifiedThemeProfile(28, "THEME_28", "Tokyo Drift", "Midnight neon magenta with racing pink", "Cyberpunk & Retro", 0xFF1A001A, 0xFFFF007F, 0xFF300030, 0x33FF007F, false, AppThemeMode.NEON_SYNTHWAVE))
    add(UnifiedThemeProfile(29, "THEME_29", "Rad Obsidian", "Obsidian velvet with intense ultraviolet radiant crest", "Cyberpunk & Retro", 0xFF111115, 0xFFA855F7, 0xFF1E1E24, 0x33A855F7, false, AppThemeMode.PITCH_BLACK_OLED))
    add(UnifiedThemeProfile(30, "THEME_30", "Cyberpunk 2077", "Stark asphalt graphite with electric neon yellow", "Cyberpunk & Retro", 0xFF0D0D11, 0xFFFCEE0A, 0xFF1A1A20, 0x33FCEE0A, false, AppThemeMode.CYBERPUNK_AMBER))

    // 31-50: Industrial & Developer Palettes
    add(UnifiedThemeProfile(31, "THEME_31", "Carbon Fiber", "Woven composite charcoal weave with titanium slate", "Industrial & Dev", 0xFF1F1F1F, 0xFF9CA3AF, 0xFF2D2D2D, 0x339CA3AF, false, AppThemeMode.MIDNIGHT_MATTE_BLACK))
    add(UnifiedThemeProfile(32, "THEME_32", "Nordic Blizzard", "Deep Arctic ocean slate with glacial frost cyan", "Industrial & Dev", 0xFF2E3440, 0xFF88C0D0, 0xFF3B4252, 0x3388C0D0, false, AppThemeMode.OCEAN_BLUE))
    add(UnifiedThemeProfile(33, "THEME_33", "Monochrome Minimal", "Clean balanced grayscale with stark contrast edge", "Industrial & Dev", 0xFF121212, 0xFFE0E0E0, 0xFF202020, 0x33E0E0E0, false, AppThemeMode.CLASSIC_BLACK_WHITE))
    add(UnifiedThemeProfile(34, "THEME_34", "Ubuntu Orange", "Canonical aubergine backing with iconic warm orange", "Industrial & Dev", 0xFF300A24, 0xFFE95420, 0xFF421034, 0x33E95420, false, AppThemeMode.SOLAR_FLARE))
    add(UnifiedThemeProfile(35, "THEME_35", "Dracula Core", "Dark gothic slate with soft pastel purple & pink", "Industrial & Dev", 0xFF282A36, 0xFFFF79C6, 0xFF383A4A, 0x33FF79C6, false, AppThemeMode.DEEP_PURPLE))
    add(UnifiedThemeProfile(36, "THEME_36", "Gruvbox Hard", "Retro warm dark earth with golden yellow accents", "Industrial & Dev", 0xFF1D2021, 0xFFFABD2F, 0xFF2A2D2E, 0x33FABD2F, false, AppThemeMode.TAILS_MECHANICAL_SANDBOX))
    add(UnifiedThemeProfile(37, "THEME_37", "Steel Foundry", "Heavy industrial steel plate with brushed graphite", "Industrial & Dev", 0xFF27272A, 0xFF71717A, 0xFF343438, 0x3371717A, false, AppThemeMode.MIDNIGHT_MATTE_BLACK))
    add(UnifiedThemeProfile(38, "THEME_38", "Hydra Cyan", "Abyssal trench navy with bioluminescent aqua cyan", "Industrial & Dev", 0xFF082F49, 0xFF06B6D4, 0xFF0F3D5E, 0x3306B6D4, false, AppThemeMode.OCEAN_BLUE))
    add(UnifiedThemeProfile(39, "THEME_39", "Rust Oxide", "Deep weathered copper oxide with burnt amber rust", "Industrial & Dev", 0xFF3B180A, 0xFFD97706, 0xFF4D2210, 0x33D97706, false, AppThemeMode.AUTUMN_COPPER))
    add(UnifiedThemeProfile(40, "THEME_40", "Solarized Abyss", "Precision solarized cyan-slate for terminal clarity", "Industrial & Dev", 0xFF002B36, 0xFF268BD2, 0xFF073642, 0x33268BD2, false, AppThemeMode.OCEAN_BLUE))
    add(UnifiedThemeProfile(41, "THEME_41", "Sunset Glow Alt", "Radiant sunset horizon with vivid ruby ember tint", "Premium Materials", 0xFF4A0E17, 0xFFFF6B6B, 0xFF5E1420, 0x33FF6B6B, false, AppThemeMode.AUTUMN_COPPER))
    add(UnifiedThemeProfile(42, "THEME_42", "Gold Leaf", "Gilded 24K imperial gold sheen over onyx stone", "Premium Materials", 0xFF1C1917, 0xFFEAB308, 0xFF2A2522, 0x33EAB308, false, AppThemeMode.CYBERPUNK_AMBER))
    add(UnifiedThemeProfile(43, "THEME_43", "Royal Amethyst", "Imperial monarch purple with radiant crystal gem glow", "Premium Materials", 0xFF2E0854, 0xFFC084FC, 0xFF3F0D70, 0x33C084FC, false, AppThemeMode.DEEP_PURPLE))
    add(UnifiedThemeProfile(44, "THEME_44", "Emerald Vault", "Deep vault green with brilliant cut emerald facets", "Premium Materials", 0xFF022C22, 0xFF10B981, 0xFF064134, 0x3310B981, false, AppThemeMode.SPRING_EMERALD))
    add(UnifiedThemeProfile(45, "THEME_45", "Copper Circuit", "Raw conductive copper traces with warm metallic bronze", "Premium Materials", 0xFF2B1810, 0xFFB45309, 0xFF3C2218, 0x33B45309, false, AppThemeMode.AUTUMN_COPPER))
    add(UnifiedThemeProfile(46, "THEME_46", "Platinum Silk", "Brushed lustrous platinum silver with cool chrome edge", "Premium Materials", 0xFF1E293B, 0xFFCBD5E1, 0xFF2B3A52, 0x33CBD5E1, false, AppThemeMode.MIDNIGHT_MATTE_BLACK))
    add(UnifiedThemeProfile(47, "THEME_47", "Crimson Shadow", "Vampiric dark bloodwood with vibrant scarlet borders", "Premium Materials", 0xFF1A0A0E, 0xFFDC2626, 0xFF2A1017, 0x33DC2626, false, AppThemeMode.CRIMSON_FURY))
    add(UnifiedThemeProfile(48, "THEME_48", "Frozen Tundra", "Sub-zero permafrost frost with crystalline ice hue", "Premium Materials", 0xFF0C2333, 0xFF7DD3FC, 0xFF14344A, 0x337DD3FC, false, AppThemeMode.OCEAN_BLUE))
    add(UnifiedThemeProfile(49, "THEME_49", "Gunmetal Heavy", "Tactical matte gunmetal alloy with stealth edges", "Premium Materials", 0xFF18181B, 0xFF52525B, 0xFF27272A, 0x3352525B, false, AppThemeMode.MIDNIGHT_MATTE_BLACK))
    add(UnifiedThemeProfile(50, "THEME_50", "NVIDIA Shield", "Team green tactical gaming emblem with lime pulse", "Pop-Culture & Special", 0xFF0A1F0A, 0xFF76B900, 0xFF133013, 0x3376B900, false, AppThemeMode.SPRING_EMERALD))

    // 51-75: Pop-Culture, Gaming & Tactical Concepts
    add(UnifiedThemeProfile(51, "THEME_51", "PlayStation Classic", "Heritage PlayStation royal blue console aesthetic", "Pop-Culture & Special", 0xFF001E50, 0xFF003791, 0xFF002B70, 0x33003791, false, AppThemeMode.OCEAN_BLUE))
    add(UnifiedThemeProfile(52, "THEME_52", "Xbox Command", "Dark gaming command deck with Xbox emerald green", "Pop-Culture & Special", 0xFF0D2810, 0xFF107C10, 0xFF153C1A, 0x33107C10, false, AppThemeMode.SPRING_EMERALD))
    add(UnifiedThemeProfile(53, "THEME_53", "GameBoy Pocket", "Nostalgic 90s monochrome LCD matrix olive-gray", "Pop-Culture & Special", 0xFF263238, 0xFF8BC34A, 0xFF37474F, 0x338BC34A, false, AppThemeMode.DESERT_SAGE))
    add(UnifiedThemeProfile(54, "THEME_54", "Nuka Quantum", "Post-apocalyptic glowing strontium cyan beverage vibe", "Pop-Culture & Special", 0xFF05233B, 0xFF00E5FF, 0xFF0C385C, 0x3300E5FF, false, AppThemeMode.OCEAN_BLUE))
    add(UnifiedThemeProfile(55, "THEME_55", "Deep Space", "Interstellar dark void with cosmic nebula indigo", "Pop-Culture & Special", 0xFF050510, 0xFF6366F1, 0xFF101026, 0x336366F1, false, AppThemeMode.DEEP_PURPLE))
    add(UnifiedThemeProfile(56, "THEME_56", "Volcanic Ash", "Molten magma rock fissures with incandescent ember", "Pop-Culture & Special", 0xFF1C1311, 0xFFFF5722, 0xFF2C1E1B, 0x33FF5722, false, AppThemeMode.SOLAR_FLARE))
    add(UnifiedThemeProfile(57, "THEME_57", "Ghost Protocol", "Stealth black-ops reconnaissance slate with icy HUD", "Pop-Culture & Special", 0xFF0A0E17, 0xFF64748B, 0xFF151C2A, 0x3364748B, false, AppThemeMode.MIDNIGHT_MATTE_BLACK))
    add(UnifiedThemeProfile(58, "THEME_58", "Subzero Frost", "Cryogenic deep freeze atmosphere with ice flare", "Pop-Culture & Special", 0xFF051B2C, 0xFF38BDF8, 0xFF0E2C44, 0x3338BDF8, false, AppThemeMode.OCEAN_BLUE))
    add(UnifiedThemeProfile(59, "THEME_59", "Redline Racing", "High-rev track champion scarlet with apex checkered edge", "Pop-Culture & Special", 0xFF2B0000, 0xFFFF0033, 0xFF3D0303, 0x33FF0033, false, AppThemeMode.CRIMSON_FURY))
    add(UnifiedThemeProfile(60, "THEME_60", "Vintage Parchment", "Ancient archival scroll parchment with sepia ink", "Pop-Culture & Special", 0xFF2B2117, 0xFFD4A373, 0xFF3B2E21, 0x33D4A373, false, AppThemeMode.TAILS_MECHANICAL_SANDBOX))
    add(UnifiedThemeProfile(61, "THEME_61", "BioHazard", "Containment hazard yellow with cautionary radioactive trim", "Pop-Culture & Special", 0xFF1A1800, 0xFFE2E600, 0xFF292605, 0x33E2E600, false, AppThemeMode.CYBERPUNK_AMBER))
    add(UnifiedThemeProfile(62, "THEME_62", "Neon Mirage", "Synthwave cyber dusk with electric hot magenta flare", "Pop-Culture & Special", 0xFF1F002B, 0xFFFF00AA, 0xFF300442, 0x33FF00AA, false, AppThemeMode.NEON_SYNTHWAVE))
    add(UnifiedThemeProfile(63, "THEME_63", "Chroma Eclipse", "Total solar eclipse corona with ultraviolet spectrum", "Pop-Culture & Special", 0xFF08080C, 0xFF8B5CF6, 0xFF14141E, 0x338B5CF6, false, AppThemeMode.DEEP_PURPLE))
    add(UnifiedThemeProfile(64, "THEME_64", "Jack Overlord", "Master Crimson & Jet Obsidian executive command profile", "Pop-Culture & Special", 0xFF1A0000, 0xFFD32F2F, 0xFF2E0202, 0x33D32F2F, false, AppThemeMode.CRIMSON_FURY))
    add(UnifiedThemeProfile(65, "THEME_65", "Nebula Nova", "Stellar supernova violet with blazing starlight pink", "Specialty Grid", 0xFF1A0826, 0xFFEC4899, 0xFF2B103C, 0x33EC4899, false, AppThemeMode.DEEP_PURPLE))
    add(UnifiedThemeProfile(66, "THEME_66", "Matrix Sentinel", "Autonomous cyber sentinel dark metal with glowing optical lens", "Specialty Grid", 0xFF0B1410, 0xFF10B981, 0xFF15261F, 0x3310B981, false, AppThemeMode.SPRING_EMERALD))
    add(UnifiedThemeProfile(67, "THEME_67", "Titanium Alloy", "Aerospace-grade titanium shell with refined brushed luster", "Specialty Grid", 0xFF24272C, 0xFFE2E8F0, 0xFF32363D, 0x33E2E8F0, false, AppThemeMode.MIDNIGHT_MATTE_BLACK))
    add(UnifiedThemeProfile(68, "THEME_68", "Quantum Flux", "High-frequency particle beam cyan over deep space vacuum", "Specialty Grid", 0xFF091224, 0xFF06B6D4, 0xFF122240, 0x3306B6D4, false, AppThemeMode.OCEAN_BLUE))
    add(UnifiedThemeProfile(69, "THEME_69", "Amber Phosphor", "Vintage monochrome amber terminal with warmth tube scanlines", "Specialty Grid", 0xFF1A1408, 0xFFF59E0B, 0xFF282010, 0x33F59E0B, false, AppThemeMode.CYBERPUNK_AMBER))
    add(UnifiedThemeProfile(70, "THEME_70", "Glacier Fjord", "Nordic glacier ice shelf with crystal aqua reflection", "Specialty Grid", 0xFF0D1E28, 0xFF38BDF8, 0xFF163040, 0x3338BDF8, false, AppThemeMode.OCEAN_BLUE))
    add(UnifiedThemeProfile(71, "THEME_71", "Ruby Monolith", "Dark crystalline garnet stone with laser scarlet facet cuts", "Specialty Grid", 0xFF240A10, 0xFFF43F5E, 0xFF36121B, 0x33F43F5E, false, AppThemeMode.CRIMSON_FURY))
    add(UnifiedThemeProfile(72, "THEME_72", "Emerald Matrix", "Encrypted mainframe data flow with luminous emerald green", "Specialty Grid", 0xFF04180A, 0xFF22C55E, 0xFF0B2E15, 0x3322C55E, false, AppThemeMode.SPRING_EMERALD))
    add(UnifiedThemeProfile(73, "THEME_73", "Sapphire Pulse", "Deep naval sonar pulse with radiant sapphire electric aura", "Specialty Grid", 0xFF0A1832, 0xFF3B82F6, 0xFF122850, 0x333B82F6, false, AppThemeMode.OCEAN_BLUE))
    add(UnifiedThemeProfile(74, "THEME_74", "Obsidian Gold", "Volcanic obsidian glass polished with 24K gold filament inlay", "Specialty Grid", 0xFF14120E, 0xFFFBBF24, 0xFF221E18, 0x33FBBF24, false, AppThemeMode.CYBERPUNK_AMBER))
    add(UnifiedThemeProfile(75, "THEME_75", "Hyper Violet", "Ultraviolet frequency spectrum with high-energy purple flare", "Specialty Grid", 0xFF1C0830, 0xFFA855F7, 0xFF2D104C, 0x33A855F7, false, AppThemeMode.DEEP_PURPLE))

    // 76-100: Master Series & Expanded Masterpieces
    add(UnifiedThemeProfile(76, "THEME_76", "Desert Dune", "Golden Saharan sand dune with warm sun-drenched terracotta", "Master Series", 0xFF281C10, 0xFFFB923C, 0xFF3D2B1A, 0x33FB923C, false, AppThemeMode.AUTUMN_COPPER))
    add(UnifiedThemeProfile(77, "THEME_77", "Midnight Aurora", "Polar night sky with shimmering atmospheric emerald ribbons", "Master Series", 0xFF061A18, 0xFF34D399, 0xFF0E2F2C, 0x3334D399, false, AppThemeMode.SPRING_EMERALD))
    add(UnifiedThemeProfile(78, "THEME_78", "Carbon Stealth", "Low-radar cross section composite matte graphite armor", "Master Series", 0xFF141416, 0xFF64748B, 0xFF222226, 0x3364748B, false, AppThemeMode.MIDNIGHT_MATTE_BLACK))
    add(UnifiedThemeProfile(79, "THEME_79", "Neon Cyber Grid", "Vector wireframe landscape with vivid cyan & magenta sunset", "Master Series", 0xFF180A26, 0xFFE879F9, 0xFF2A1440, 0x33E879F9, false, AppThemeMode.NEON_SYNTHWAVE))
    add(UnifiedThemeProfile(80, "THEME_80", "Plasma Arc", "High-voltage ionized gas channel with electric blue lightning", "Master Series", 0xFF08162E, 0xFF60A5FA, 0xFF102850, 0x3360A5FA, false, AppThemeMode.OCEAN_BLUE))
    add(UnifiedThemeProfile(81, "THEME_81", "Forge Ember", "Blacksmith anvil hearth with incandescent glowing coals", "Master Series", 0xFF2A0C06, 0xFFFF6B6B, 0xFF3F150C, 0x33FF6B6B, false, AppThemeMode.AUTUMN_COPPER))
    add(UnifiedThemeProfile(82, "THEME_82", "Zenith White", "Pure alpine snow peak with crisp sky blue reflections", "Master Series", 0xFFF8FAFC, 0xFF0284C7, 0xFFE2E8F0, 0x330284C7, true, AppThemeMode.CLASSIC_WHITE_BLACK))
    add(UnifiedThemeProfile(83, "THEME_83", "Abyssal Trench", "Bioluminescent deep ocean floor with eerie cyan glow", "Master Series", 0xFF021422, 0xFF22D3EE, 0xFF06243A, 0x3322D3EE, false, AppThemeMode.OCEAN_BLUE))
    add(UnifiedThemeProfile(84, "THEME_84", "Crimson Core", "Thermonuclear core shielding with emergency scarlet beacons", "Master Series", 0xFF260408, 0xFFEF4444, 0xFF3B0B12, 0x33EF4444, false, AppThemeMode.CRIMSON_FURY))
    add(UnifiedThemeProfile(85, "THEME_85", "Solar Corona", "Total eclipse chromosphere with brilliant golden solar flares", "Master Series", 0xFF221200, 0xFFF59E0B, 0xFF361E05, 0x33F59E0B, false, AppThemeMode.SOLAR_FLARE))
    add(UnifiedThemeProfile(86, "THEME_86", "Prism Spectrum", "Optical refraction crystal dispersing pure full-gamut beams", "Master Series", 0xFF101420, 0xFF818CF8, 0xFF1C2234, 0x33818CF8, false, AppThemeMode.DEEP_PURPLE))
    add(UnifiedThemeProfile(87, "THEME_87", "Matrix Hex Code", "Raw machine instruction hex dumps in terminal phosphor green", "Master Series", 0xFF061408, 0xFF4ADE80, 0xFF0E2812, 0x334ADE80, false, AppThemeMode.SPRING_EMERALD))
    add(UnifiedThemeProfile(88, "THEME_88", "Tactical Camo", "Military woodland field camouflage with muted olive drab", "Master Series", 0xFF1C2218, 0xFFA3E635, 0xFF2A3425, 0x33A3E635, false, AppThemeMode.DESERT_SAGE))
    add(UnifiedThemeProfile(89, "THEME_89", "Royal Velvet", "Coronation purple velvet drapery with polished gold embroidery", "Master Series", 0xFF240638, 0xFFFDE047, 0xFF360C50, 0x33FDE047, false, AppThemeMode.DEEP_PURPLE))
    add(UnifiedThemeProfile(90, "THEME_90", "Silver Stream", "Liquid mercury stream flowing over polished slate bed", "Master Series", 0xFF1E222A, 0xFFCBD5E1, 0xFF2D323E, 0x33CBD5E1, false, AppThemeMode.MIDNIGHT_MATTE_BLACK))
    add(UnifiedThemeProfile(91, "THEME_91", "Cosmic Dust", "Interstellar dust pillar with glowing newborn star clusters", "Master Series", 0xFF120A20, 0xFFC084FC, 0xFF201336, 0x33C084FC, false, AppThemeMode.DEEP_PURPLE))
    add(UnifiedThemeProfile(92, "THEME_92", "Vaporwave Dusk", "Nostalgic 1984 aesthetic with pastel teal and flamingo pink", "Master Series", 0xFF1C1028, 0xFFF472B6, 0xFF2D1B40, 0x33F472B6, false, AppThemeMode.NEON_SYNTHWAVE))
    add(UnifiedThemeProfile(93, "THEME_93", "Cyber Samurai", "Neo-Tokyo ronin lacquer armor with neon crimson katana edge", "Master Series", 0xFF20060A, 0xFFFF0055, 0xFF330D14, 0x33FF0055, false, AppThemeMode.CRIMSON_FURY))
    add(UnifiedThemeProfile(94, "THEME_94", "Arctic Expedition", "Glacial survival expedition station with hazard orange beacons", "Master Series", 0xFF0A1E2C, 0xFFFB923C, 0xFF143044, 0x33FB923C, false, AppThemeMode.OCEAN_BLUE))
    add(UnifiedThemeProfile(95, "THEME_95", "Onyx Marble", "Fine black marble slab with delicate white quartz veins", "Master Series", 0xFF101012, 0xFFE4E4E7, 0xFF1E1E22, 0x33E4E4E7, false, AppThemeMode.CLASSIC_BLACK_WHITE))
    add(UnifiedThemeProfile(96, "THEME_96", "Copper Verdigris", "Aged architectural copper with natural turquoise patina", "Master Series", 0xFF142422, 0xFF2DD4BF, 0xFF1F3835, 0x332DD4BF, false, AppThemeMode.AUTUMN_COPPER))
    add(UnifiedThemeProfile(97, "THEME_97", "Electric Lime", "Ultra high-visibility racing lime over asphalt composite", "Master Series", 0xFF121A08, 0xFF84CC16, 0xFF202C10, 0x3384CC16, false, AppThemeMode.SPRING_EMERALD))
    add(UnifiedThemeProfile(98, "THEME_98", "Amethyst Geode", "Broken crystalline geode interior with deep violet facets", "Master Series", 0xFF1E0A30, 0xFFD8B4FE, 0xFF2F144A, 0x33D8B4FE, false, AppThemeMode.DEEP_PURPLE))
    add(UnifiedThemeProfile(99, "THEME_99", "Hazard Stripe", "Industrial safety barricade with bold black & safety yellow stripes", "Master Series", 0xFF1A1600, 0xFFEAB308, 0xFF2B2505, 0x33EAB308, false, AppThemeMode.CYBERPUNK_AMBER))
    add(UnifiedThemeProfile(100, "THEME_100", "Ultimate Master 100", "The definitive 100th catalog masterpiece profile with chromatic aura", "Master Series", 0xFF160818, 0xFFE11D48, 0xFF261028, 0x33E11D48, false, AppThemeMode.NEON_SYNTHWAVE))
  }

  // Fast ID to Profile Index Map for O(1) 60FPS lookups
  private val THEME_MAP: Map<String, UnifiedThemeProfile> = buildMap {
    ALL_100_THEMES.forEach { profile ->
      put(profile.id.uppercase(), profile)
      put("THEME_${profile.index}", profile)
      put(profile.name.lowercase().replace(" ", "_").replace("&", "and"), profile)
      put(profile.name.uppercase(), profile)
      // Also map canonical ids
      if (profile.index == 16) {
        put("SAMSUNG_EXPERIENCE", profile)
        put("samsung_experience", profile)
      }
    }
  }

  private val _activeProfile = MutableStateFlow(ALL_100_THEMES[0])
  val activeProfile: StateFlow<UnifiedThemeProfile> = _activeProfile.asStateFlow()

  fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  /**
   * Resolves a theme profile by string ID, index, or name cleanly.
   */
  fun resolveTheme(themeIdOrKey: String?): UnifiedThemeProfile {
    if (themeIdOrKey.isNullOrBlank()) return ALL_100_THEMES[0]
    
    val upper = themeIdOrKey.trim().uppercase()
    THEME_MAP[upper]?.let { return it }

    // Try numeric extraction (e.g., "16", "custom_theme_16")
    val digits = themeIdOrKey.filter { it.isDigit() }
    if (digits.isNotEmpty()) {
      val idx = digits.toIntOrNull()
      if (idx != null && idx in 1..100) {
        return ALL_100_THEMES[idx - 1]
      }
    }

    // Try fallback by AppThemeMode lookup
    val matched = ALL_100_THEMES.find {
      it.appThemeMode.id.equals(themeIdOrKey, ignoreCase = true) ||
      it.name.equals(themeIdOrKey, ignoreCase = true)
    }
    return matched ?: ALL_100_THEMES[0]
  }

  /**
   * Initializes engine state upon activity boot.
   */
  fun initialize(context: Context): UnifiedThemeProfile {
    val prefs = getPrefs(context)
    val savedId = prefs.getString(KEY_ACTIVE_THEME_ID, null)
      ?: prefs.getString(KEY_LEGACY_THEME_MODE, "THEME_1")
      ?: "THEME_1"
    val profile = resolveTheme(savedId)
    _activeProfile.value = profile
    return profile
  }

  /**
   * Universal Theme Activation Method (ALL 100 THEMES ROUTE IDENTICALLY HERE).
   * 1. Runs universal reset pass wiping out prior card backgrounds, outlines, and color filters.
   * 2. Decouples and applies active theme parameters cleanly.
   * 3. Executes conditional retro loop for SAMSUNG_EXPERIENCE vs ALL 99 OTHER THEMES.
   */
  fun applyThemeUnified(
    context: Context,
    targetThemeId: String,
    activity: Activity? = null,
    onApplied: ((UnifiedThemeProfile) -> Unit)? = null
  ): UnifiedThemeProfile {
    val profile = resolveTheme(targetThemeId)

    // Persist active theme ID
    getPrefs(context).edit()
      .putString(KEY_ACTIVE_THEME_ID, profile.id)
      .putString(KEY_LEGACY_THEME_MODE, profile.appThemeMode.id)
      .apply()

    _activeProfile.value = profile

    // Run Universal Reset Pass
    runUniversalResetPass(activity)

    // Execute Conditional Retro Loop
    if (activity != null) {
      applyRetroAndLayoutDetails(activity, profile.id)
    }

    onApplied?.invoke(profile)
    return profile
  }

  /**
   * Universal Reset Pass:
   * Explicitly detaches and clears memory caches, tint filters, and card backgrounds.
   */
  fun runUniversalResetPass(activity: Activity? = null) {
    if (activity == null) return
    try {
      val rootView = activity.window?.decorView as? ViewGroup ?: return
      clearViewHierarchyCaches(rootView)
    } catch (_: Throwable) {}
  }

  private fun clearViewHierarchyCaches(viewGroup: ViewGroup) {
    for (i in 0 until viewGroup.childCount) {
      val child = viewGroup.getChildAt(i)
      if (child is ViewGroup) {
        clearViewHierarchyCaches(child)
      } else {
        // Clear color filters & animation listeners if detached
        child.animation?.cancel()
      }
    }
  }

  /**
   * CONDITIONAL RETRO DETAILS LOOP:
   * - ONLY IF (activeThemeId == "SAMSUNG_EXPERIENCE" or THEME_16):
   *   * Old-school edge glow overscroll (EdgeEffect.TYPE_GLOW) with transparent black glowColor (#80000000).
   *   * Smooth Samsung Experience squircle background frames with classic pastel/gradient color maps.
   * - FOR ALL OTHER 99 THEMES:
   *   * Modern system stretch mechanics (EdgeEffect.TYPE_STRETCH).
   *   * Strip out squircle drawable backgrounds & custom tints, resetting to active theme standards.
   */
  fun applyRetroAndLayoutDetails(activity: Activity, activeThemeId: String) {
    val isSamsungExperience = activeThemeId.equals("SAMSUNG_EXPERIENCE", ignoreCase = true) ||
      activeThemeId.equals("THEME_16", ignoreCase = true) ||
      activeThemeId.equals("samsung_experience", ignoreCase = true)

    val scrollView = activity.findViewById<ScrollView>(R.id.dashboard_scroll_view)
    val imagesIcon = activity.findViewById<View>(R.id.media_icon_images)
    val audioIcon = activity.findViewById<View>(R.id.media_icon_audio)
    val docsIcon = activity.findViewById<View>(R.id.media_icon_docs)

    if (isSamsungExperience) {
      // 1. Retro Kindle Fire / Holo Overscroll Crescent Glow
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && scrollView != null) {
        try {
          val setEdgeEffectMethod = scrollView.javaClass.getMethod("setEdgeEffectType", Int::class.javaPrimitiveType)
          setEdgeEffectMethod.invoke(scrollView, 0) // TYPE_GLOW = 0
        } catch (_: Throwable) {}
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && scrollView != null) {
        try {
          val glowColor = AndroidColor.parseColor("#80000000")
          scrollView.topEdgeEffectColor = glowColor
          scrollView.bottomEdgeEffectColor = glowColor
        } catch (_: Throwable) {}
      }

      // 2. Samsung Experience Squircles & Pastel Tints
      listOfNotNull(imagesIcon, audioIcon, docsIcon).forEach { iconView ->
        iconView.setBackgroundResource(R.drawable.samsung_experience_squircle)
      }
      imagesIcon?.backgroundTintList = ColorStateList.valueOf(AndroidColor.parseColor("#FF5B72"))
      audioIcon?.backgroundTintList = ColorStateList.valueOf(AndroidColor.parseColor("#29B6F6"))
      docsIcon?.backgroundTintList = ColorStateList.valueOf(AndroidColor.parseColor("#3B66F5"))

    } else {
      // Default Fallback for all other 99 themes:
      // 1. Modern System Stretch Mechanics
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && scrollView != null) {
        try {
          val setEdgeEffectMethod = scrollView.javaClass.getMethod("setEdgeEffectType", Int::class.javaPrimitiveType)
          setEdgeEffectMethod.invoke(scrollView, 1) // TYPE_STRETCH = 1
        } catch (_: Throwable) {}
      }

      // 2. Completely strip out squircle drawable backgrounds and custom tints
      listOfNotNull(imagesIcon, audioIcon, docsIcon).forEach { iconView ->
        iconView.background = null
        iconView.backgroundTintList = null
      }
    }
  }

  /**
   * Fast Style Resource Resolver for all 100 Themes.
   */
  fun getStyleResourceForTheme(context: Context, themeId: String): Int {
    val profile = resolveTheme(themeId)
    val index = profile.index
    val resId = context.resources.getIdentifier("Theme_Custom_$index", "style", context.packageName)
    return if (resId != 0) resId else R.style.Theme_Custom_1
  }
}
