package com.jackattackk246.files.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

data class LauncherIconVariant(
  val id: String,
  val title: String,
  val subtitle: String,
  val aliasClass: String,
  val category: String = "General",
  val primaryColorHex: Long = 0xFF141414,
  val accentColorHex: Long = 0xFF38BDF8
)

class IconChangerEngine(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val basePackageToken = "com.jackattackk246.files.MainActivity"

    fun safelyDeployTargetTheme(themeSuffix: String) {
        val cleanSuffix = themeSuffix.removePrefix(basePackageToken).trim()
        val targetVariant = ICON_VARIANTS.find { it.aliasClass.endsWith(cleanSuffix) || it.id == cleanSuffix.lowercase() }
        val finalSuffix = targetVariant?.aliasClass?.removePrefix(basePackageToken) ?: cleanSuffix

        ICON_VARIANTS.take(52).forEach { currentVariant ->
            val curSuffix = currentVariant.aliasClass.removePrefix(basePackageToken)
            val fullComponentNamePath = "$basePackageToken$curSuffix"
            val componentName = ComponentName(context, fullComponentNamePath)
            
            val configurationStateSetting = if (curSuffix == finalSuffix) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }

            try {
                packageManager.setComponentEnabledSetting(
                    componentName,
                    configurationStateSetting,
                    PackageManager.DONT_KILL_APP
                )
            } catch (_: Exception) {
                // Safeguard against missing alias declaration in manifest
            }
        }
    }

    fun safelySwitchLauncherIcon(targetAliasName: String) {
        val suffix = targetAliasName.removePrefix(basePackageToken)
        safelyDeployTargetTheme(suffix)
    }

    companion object {
        // EXACT 100 DISTINCT SELECTABLE USER STYLES REGISTRY
        val ICON_VARIANTS: List<LauncherIconVariant> = buildList {
            // 1–15: Canvas Color Themes Direct Bundle (Moved from settings view tree)
            add(LauncherIconVariant("canvas_classic_black_white", "Classic Black & White", "Stark white typography over pitch-black canvas container", "com.jackattackk246.files.MainActivityDefault", "Canvas Themes", 0xFF000000, 0xFFFFFFFF))
            add(LauncherIconVariant("canvas_classic_white_black", "Classic White & Black", "High-contrast deep black typography over solid white canvas", "com.jackattackk246.files.MainActivityLightFrost", "Canvas Themes", 0xFFFFFFFF, 0xFF000000))
            add(LauncherIconVariant("canvas_dynamic_weather", "Dynamic Weather Canvas", "Flowing vertical linear gradient with sky blue to horizon cobalt", "com.jackattackk246.files.MainActivityAeroClassic", "Canvas Themes", 0xFF4FACFE, 0xFF00F2FE))
            add(LauncherIconVariant("canvas_pitch_black_oled", "OLED Pitch Black", "Pure black backdrop optimized for extreme panel battery endurance", "com.jackattackk246.files.MainActivityPureOLED", "Canvas Themes", 0xFF000000, 0xFF38BDF8))
            add(LauncherIconVariant("canvas_midnight_matte_black", "Midnight Matte Black", "Dark charcoal canvas with subtle slate contrast frames", "com.jackattackk246.files.MainActivityMidnightPrism", "Canvas Themes", 0xFF141414, 0xFF38BDF8))
            add(LauncherIconVariant("canvas_spring_emerald", "Spring Emerald", "Rich forest green backdrop with vivid neon green indicators", "com.jackattackk246.files.MainActivityGothMatrix", "Canvas Themes", 0xFF0A1F0D, 0xFF00FF66))
            add(LauncherIconVariant("canvas_cyberpunk_amber", "Cyberpunk Amber", "Deep graphite backing with high-saturation amber yellow highlights", "com.jackattackk246.files.MainActivityCyberpunk2077", "Canvas Themes", 0xFF1A1A22, 0xFFFFB000))
            add(LauncherIconVariant("canvas_deep_purple", "Deep Purple", "Royal amethyst to obsidian void with radiant purple glow", "com.jackattackk246.files.MainActivityRoyalAmethyst", "Canvas Themes", 0xFF2E0854, 0xFFC084FC))
            add(LauncherIconVariant("canvas_ocean_blue", "Ocean Blue", "Maritime sapphire with crystalline ice blue highlights", "com.jackattackk246.files.MainActivityHydraCyan", "Canvas Themes", 0xFF0D2B45, 0xFF38BDF8))
            add(LauncherIconVariant("canvas_tails_mechanical", "Tails' Mechanical Sandbox", "Industrial dark slate with metallic amber bronze accents", "com.jackattackk246.files.MainActivityGruvboxHard", "Canvas Themes", 0xFF20242C, 0xFFD97706))
            add(LauncherIconVariant("canvas_autumn_copper", "Autumn Copper", "Deep Burgundy to muted burnt void with warm copper sheen", "com.jackattackk246.files.MainActivityCopperCircuit", "Canvas Themes", 0xFF4A1525, 0xFFEA580C))
            add(LauncherIconVariant("canvas_crimson_fury", "Crimson Fury", "Dark charcoal with striking crimson red structural borders", "com.jackattackk246.files.MainActivityCrimsonGlow", "Canvas Themes", 0xFF1E0E12, 0xFFDC2626))
            add(LauncherIconVariant("canvas_neon_synthwave", "Neon Synthwave", "Retro dark violet with radiant hot pink neon rings", "com.jackattackk246.files.MainActivityNeonSynthwave", "Canvas Themes", 0xFF1E1B4B, 0xFFF43F5E))
            add(LauncherIconVariant("canvas_solar_flare", "Solar Flare", "Muted ash-gray with energetic solar corona orange accents", "com.jackattackk246.files.MainActivitySolarFlare", "Canvas Themes", 0xFF26262B, 0xFFF97316))
            add(LauncherIconVariant("canvas_desert_sage", "Desert Sage", "Earthy pale olive-green with warm cream typography", "com.jackattackk246.files.MainActivityDesertSage", "Canvas Themes", 0xFF2F3E36, 0xFF84CC16))

            // 16–30: Core & Terminal Baselines
            add(LauncherIconVariant("default_system", "Default System", "Classic dark emerald folder with document tabs", "com.jackattackk246.files.MainActivityDefault", "Core Baselines", 0xFF006738, 0xFF4ECB98))
            add(LauncherIconVariant("midnight_prism", "Midnight Prism", "Deep obsidian backdrop with electric cyan prism glow", "com.jackattackk246.files.MainActivityMidnightPrism", "Core Baselines", 0xFF0F172A, 0xFF38BDF8))
            add(LauncherIconVariant("goth_matrix", "Goth Matrix", "Pitch black backdrop with cyber terminal neon green", "com.jackattackk246.files.MainActivityGothMatrix", "Core Baselines", 0xFF050505, 0xFF00FF66))
            add(LauncherIconVariant("ceramic_matte", "Ceramic Matte", "Minimalist matte charcoal with stark titanium white", "com.jackattackk246.files.MainActivityCeramicMatte", "Core Baselines", 0xFF18181B, 0xFFF4F4F5))
            add(LauncherIconVariant("blurple_twilight", "Blurple Twilight", "Deep indigo nightshade with vivid royal violet", "com.jackattackk246.files.MainActivityBlurpleTwilight", "Core Baselines", 0xFF1E1B4B, 0xFF818CF8))
            add(LauncherIconVariant("sunset_glow", "Sunset Glow", "Warm crimson dusk with radiant amber horizon", "com.jackattackk246.files.MainActivitySunsetGlow", "Core Baselines", 0xFF450A0A, 0xFFF97316))
            add(LauncherIconVariant("crimson_glow", "Crimson Glow", "Pure obsidian black with intense crimson red aura", "com.jackattackk246.files.MainActivityCrimsonGlow", "Core Baselines", 0xFF2A0808, 0xFFEF4444))
            add(LauncherIconVariant("light_frost", "Light Frost", "Crisp glacial white layout with horizon cobalt tint", "com.jackattackk246.files.MainActivityLightFrost", "Core Baselines", 0xFFF0F9FF, 0xFF0284C7))
            add(LauncherIconVariant("aero_classic", "Aero Classic", "Refined deep navy blue with sapphire aero gloss", "com.jackattackk246.files.MainActivityAeroClassic", "Core Baselines", 0xFF1E3A8A, 0xFF60A5FA))
            add(LauncherIconVariant("retro_purple", "Retro Purple", "Signature indigo retro console layout", "com.jackattackk246.files.MainActivityBlurpleTwilight", "Core Baselines", 0xFF1E1B4B, 0xFF818CF8))
            add(LauncherIconVariant("retro_crt", "Retro CRT", "Vintage green phosphor monitor scanline emulator", "com.jackattackk246.files.MainActivityRetroCRT", "Cyberpunk & Retro", 0xFF0A1A0A, 0xFF39FF14))
            add(LauncherIconVariant("glitch_overdrive", "Glitch Overdrive", "Dark cyber glitch matrix with electric cyan highlights", "com.jackattackk246.files.MainActivityGlitchOverdrive", "Cyberpunk & Retro", 0xFF120024, 0xFF00FFFF))
            add(LauncherIconVariant("tokyo_drift", "Tokyo Drift", "Midnight neon magenta with high-octane racing pink", "com.jackattackk246.files.MainActivityTokyoDrift", "Cyberpunk & Retro", 0xFF1A001A, 0xFFFF007F))
            add(LauncherIconVariant("rad_obsidian", "Rad Obsidian", "Obsidian velvet with intense ultraviolet radiant crest", "com.jackattackk246.files.MainActivityRadObsidian", "Cyberpunk & Retro", 0xFF111115, 0xFFA855F7))
            add(LauncherIconVariant("cyberpunk_2077", "Cyberpunk 2077", "Stark asphalt graphite with electric neon yellow", "com.jackattackk246.files.MainActivityCyberpunk2077", "Cyberpunk & Retro", 0xFF0D0D11, 0xFFFCEE0A))

            // 31–50: Industrial & Developer Palettes
            add(LauncherIconVariant("carbon_fiber", "Carbon Fiber", "Woven composite charcoal weave with titanium slate", "com.jackattackk246.files.MainActivityCarbonFiber", "Industrial & Dev", 0xFF1F1F1F, 0xFF9CA3AF))
            add(LauncherIconVariant("nordic_blizzard", "Nordic Blizzard", "Deep Arctic ocean slate with glacial frost cyan", "com.jackattackk246.files.MainActivityNordicBlizzard", "Industrial & Dev", 0xFF2E3440, 0xFF88C0D0))
            add(LauncherIconVariant("monochrome_minimal", "Monochrome Minimal", "Clean balanced grayscale with stark contrast edge", "com.jackattackk246.files.MainActivityMonochromeMinimal", "Industrial & Dev", 0xFF121212, 0xFFE0E0E0))
            add(LauncherIconVariant("ubuntu_orange", "Ubuntu Orange", "Canonical aubergine backing with iconic warm orange", "com.jackattackk246.files.MainActivityUbuntuOrange", "Industrial & Dev", 0xFF300A24, 0xFFE95420))
            add(LauncherIconVariant("dracula_core", "Dracula Core", "Dark gothic slate with soft pastel purple & pink", "com.jackattackk246.files.MainActivityDraculaCore", "Industrial & Dev", 0xFF282A36, 0xFFFF79C6))
            add(LauncherIconVariant("gruvbox_hard", "Gruvbox Hard", "Retro warm dark earth with golden yellow accents", "com.jackattackk246.files.MainActivityGruvboxHard", "Industrial & Dev", 0xFF1D2021, 0xFFFABD2F))
            add(LauncherIconVariant("steel_foundry", "Steel Foundry", "Heavy industrial steel plate with brushed graphite", "com.jackattackk246.files.MainActivitySteelFoundry", "Industrial & Dev", 0xFF27272A, 0xFF71717A))
            add(LauncherIconVariant("hydra_cyan", "Hydra Cyan", "Abyssal trench navy with bioluminescent aqua cyan", "com.jackattackk246.files.MainActivityHydraCyan", "Industrial & Dev", 0xFF082F49, 0xFF06B6D4))
            add(LauncherIconVariant("rust_oxide", "Rust Oxide", "Deep weathered copper oxide with burnt amber rust", "com.jackattackk246.files.MainActivityRustOxide", "Industrial & Dev", 0xFF3B180A, 0xFFD97706))
            add(LauncherIconVariant("solarized_abyss", "Solarized Abyss", "Precision solarized cyan-slate for terminal clarity", "com.jackattackk246.files.MainActivitySolarizedAbyss", "Industrial & Dev", 0xFF002B36, 0xFF268BD2))
            add(LauncherIconVariant("sunset_glow_alt", "Sunset Glow Alt", "Radiant sunset horizon with vivid ruby ember tint", "com.jackattackk246.files.MainActivitySunsetGlowAlt", "Premium Materials", 0xFF4A0E17, 0xFFFF6B6B))
            add(LauncherIconVariant("gold_leaf", "Gold Leaf", "Gilded 24K imperial gold sheen over onyx stone", "com.jackattackk246.files.MainActivityGoldLeaf", "Premium Materials", 0xFF1C1917, 0xFFEAB308))
            add(LauncherIconVariant("royal_amethyst", "Royal Amethyst", "Imperial monarch purple with radiant crystal gem glow", "com.jackattackk246.files.MainActivityRoyalAmethyst", "Premium Materials", 0xFF2E0854, 0xFFC084FC))
            add(LauncherIconVariant("emerald_vault", "Emerald Vault", "Deep vault green with brilliant cut emerald facets", "com.jackattackk246.files.MainActivityEmeraldVault", "Premium Materials", 0xFF022C22, 0xFF10B981))
            add(LauncherIconVariant("copper_circuit", "Copper Circuit", "Raw conductive copper traces with warm metallic bronze", "com.jackattackk246.files.MainActivityCopperCircuit", "Premium Materials", 0xFF2B1810, 0xFFB45309))
            add(LauncherIconVariant("platinum_silk", "Platinum Silk", "Brushed lustrous platinum silver with cool chrome edge", "com.jackattackk246.files.MainActivityPlatinumSilk", "Premium Materials", 0xFF1E293B, 0xFFCBD5E1))
            add(LauncherIconVariant("crimson_shadow", "Crimson Shadow", "Vampiric dark bloodwood with vibrant scarlet borders", "com.jackattackk246.files.MainActivityCrimsonShadow", "Premium Materials", 0xFF1A0A0E, 0xFFDC2626))
            add(LauncherIconVariant("frozen_tundra", "Frozen Tundra", "Sub-zero permafrost frost with crystalline ice hue", "com.jackattackk246.files.MainActivityFrozenTundra", "Premium Materials", 0xFF0C2333, 0xFF7DD3FC))
            add(LauncherIconVariant("gunmetal_heavy", "Gunmetal Heavy", "Tactical matte gunmetal alloy with stealth edges", "com.jackattackk246.files.MainActivityGunmetalHeavy", "Premium Materials", 0xFF18181B, 0xFF52525B))
            add(LauncherIconVariant("nvidia_shield", "NVIDIA Shield", "Team green tactical gaming emblem with lime pulse", "com.jackattackk246.files.MainActivityNvidiaShield", "Pop-Culture & Special", 0xFF0A1F0A, 0xFF76B900))

            // 51–75: Pop-Culture, Gaming & Tactical Concepts
            add(LauncherIconVariant("playstation_classic", "PlayStation Classic", "Heritage PlayStation royal blue console aesthetic", "com.jackattackk246.files.MainActivityPlayStationClassic", "Pop-Culture & Special", 0xFF001E50, 0xFF003791))
            add(LauncherIconVariant("xbox_command", "Xbox Command", "Dark gaming command deck with Xbox emerald green", "com.jackattackk246.files.MainActivityXboxCommand", "Pop-Culture & Special", 0xFF0D2810, 0xFF107C10))
            add(LauncherIconVariant("gameboy_pocket", "GameBoy Pocket", "Nostalgic 90s monochrome LCD matrix olive-gray", "com.jackattackk246.files.MainActivityGameBoyPocket", "Pop-Culture & Special", 0xFF263238, 0xFF8BC34A))
            add(LauncherIconVariant("nuka_quantum", "Nuka Quantum", "Post-apocalyptic glowing strontium cyan beverage vibe", "com.jackattackk246.files.MainActivityNukaQuantum", "Pop-Culture & Special", 0xFF05233B, 0xFF00E5FF))
            add(LauncherIconVariant("deep_space", "Deep Space", "Interstellar dark void with cosmic nebula indigo", "com.jackattackk246.files.MainActivityDeepSpace", "Pop-Culture & Special", 0xFF050510, 0xFF6366F1))
            add(LauncherIconVariant("volcanic_ash", "Volcanic Ash", "Molten magma rock fissures with incandescent ember", "com.jackattackk246.files.MainActivityVolcanicAsh", "Pop-Culture & Special", 0xFF1C1311, 0xFFFF5722))
            add(LauncherIconVariant("ghost_protocol", "Ghost Protocol", "Stealth black-ops reconnaissance slate with icy HUD", "com.jackattackk246.files.MainActivityGhostProtocol", "Pop-Culture & Special", 0xFF0A0E17, 0xFF64748B))
            add(LauncherIconVariant("subzero_frost", "Subzero Frost", "Cryogenic deep freeze atmosphere with ice flare", "com.jackattackk246.files.MainActivitySubzeroFrost", "Pop-Culture & Special", 0xFF051B2C, 0xFF38BDF8))
            add(LauncherIconVariant("redline_racing", "Redline Racing", "High-rev track champion scarlet with apex checkered edge", "com.jackattackk246.files.MainActivityRedlineRacing", "Pop-Culture & Special", 0xFF2B0000, 0xFFFF0033))
            add(LauncherIconVariant("vintage_parchment", "Vintage Parchment", "Ancient archival scroll parchment with sepia ink", "com.jackattackk246.files.MainActivityVintageParchment", "Pop-Culture & Special", 0xFF2B2117, 0xFFD4A373))
            add(LauncherIconVariant("biohazard", "BioHazard", "Containment hazard yellow with cautionary radioactive trim", "com.jackattackk246.files.MainActivityBioHazard", "Pop-Culture & Special", 0xFF1A1800, 0xFFE2E600))
            add(LauncherIconVariant("neon_mirage", "Neon Mirage", "Synthwave cyber dusk with electric hot magenta flare", "com.jackattackk246.files.MainActivityNeonMirage", "Pop-Culture & Special", 0xFF1F002B, 0xFFFF00AA))
            add(LauncherIconVariant("chroma_eclipse", "Chroma Eclipse", "Total solar eclipse corona with ultraviolet spectrum", "com.jackattackk246.files.MainActivityChromaEclipse", "Pop-Culture & Special", 0xFF08080C, 0xFF8B5CF6))
            add(LauncherIconVariant("jack_overlord", "Jack Overlord", "Master Crimson & Jet Obsidian executive command profile", "com.jackattackk246.files.MainActivityJackOverlord", "Pop-Culture & Special", 0xFF1A0000, 0xFFD32F2F))
            add(LauncherIconVariant("nebula_nova", "Nebula Nova", "Stellar supernova violet with blazing starlight pink", "com.jackattackk246.files.MainActivityDeepSpace", "Specialty Grid", 0xFF1A0826, 0xFFEC4899))
            add(LauncherIconVariant("matrix_sentinel", "Matrix Sentinel", "Autonomous cyber sentinel dark metal with glowing optical lens", "com.jackattackk246.files.MainActivityGothMatrix", "Specialty Grid", 0xFF0B1410, 0xFF10B981))
            add(LauncherIconVariant("titanium_alloy", "Titanium Alloy", "Aerospace-grade titanium shell with refined brushed luster", "com.jackattackk246.files.MainActivityPlatinumSilk", "Specialty Grid", 0xFF24272C, 0xFFE2E8F0))
            add(LauncherIconVariant("quantum_flux", "Quantum Flux", "High-frequency particle beam cyan over deep space vacuum", "com.jackattackk246.files.MainActivityMidnightPrism", "Specialty Grid", 0xFF091224, 0xFF06B6D4))
            add(LauncherIconVariant("amber_phosphor", "Amber Phosphor", "Vintage monochrome amber terminal with warmth tube scanlines", "com.jackattackk246.files.MainActivityCyberpunk2077", "Specialty Grid", 0xFF1A1408, 0xFFF59E0B))
            add(LauncherIconVariant("glacier_fjord", "Glacier Fjord", "Nordic glacier ice shelf with crystal aqua reflection", "com.jackattackk246.files.MainActivityNordicBlizzard", "Specialty Grid", 0xFF0D1E28, 0xFF38BDF8))
            add(LauncherIconVariant("ruby_monolith", "Ruby Monolith", "Dark crystalline garnet stone with laser scarlet facet cuts", "com.jackattackk246.files.MainActivityCrimsonGlow", "Specialty Grid", 0xFF240A10, 0xFFF43F5E))
            add(LauncherIconVariant("emerald_matrix", "Emerald Matrix", "Encrypted mainframe data flow with luminous emerald green", "com.jackattackk246.files.MainActivityGothMatrix", "Specialty Grid", 0xFF04180A, 0xFF22C55E))
            add(LauncherIconVariant("sapphire_pulse", "Sapphire Pulse", "Deep naval sonar pulse with radiant sapphire electric aura", "com.jackattackk246.files.MainActivityAeroClassic", "Specialty Grid", 0xFF0A1832, 0xFF3B82F6))
            add(LauncherIconVariant("obsidian_gold", "Obsidian Gold", "Volcanic obsidian glass polished with 24K gold filament inlay", "com.jackattackk246.files.MainActivityGoldLeaf", "Specialty Grid", 0xFF14120E, 0xFFFBBF24))
            add(LauncherIconVariant("hyper_violet", "Hyper Violet", "Ultraviolet frequency spectrum with high-energy purple flare", "com.jackattackk246.files.MainActivityRoyalAmethyst", "Specialty Grid", 0xFF1C0830, 0xFFA855F7))

            // 76–100: Master Series & Expanded 100 Designs
            add(LauncherIconVariant("desert_dune", "Desert Dune", "Golden Saharan sand dune with warm sun-drenched terracotta", "com.jackattackk246.files.MainActivitySunsetGlow", "Master Series", 0xFF281C10, 0xFFFB923C))
            add(LauncherIconVariant("midnight_aurora", "Midnight Aurora", "Polar night sky with shimmering atmospheric emerald ribbons", "com.jackattackk246.files.MainActivityDefault", "Master Series", 0xFF061A18, 0xFF34D399))
            add(LauncherIconVariant("carbon_stealth", "Carbon Stealth", "Low-radar cross section composite matte graphite armor", "com.jackattackk246.files.MainActivityCarbonFiber", "Master Series", 0xFF141416, 0xFF64748B))
            add(LauncherIconVariant("neon_cyber_grid", "Neon Cyber Grid", "Vector wireframe landscape with vivid cyan & magenta sunset", "com.jackattackk246.files.MainActivityNeonSynthwave", "Master Series", 0xFF180A26, 0xFFE879F9))
            add(LauncherIconVariant("plasma_arc", "Plasma Arc", "High-voltage ionized gas channel with electric blue lightning", "com.jackattackk246.files.MainActivityAeroClassic", "Master Series", 0xFF08162E, 0xFF60A5FA))
            add(LauncherIconVariant("forge_ember", "Forge Ember", "Blacksmith anvil hearth with incandescent glowing coals", "com.jackattackk246.files.MainActivityVolcanicAsh", "Master Series", 0xFF2A0C06, 0xFFFF6B6B))
            add(LauncherIconVariant("zenith_white", "Zenith White", "Pure alpine snow peak with crisp sky blue reflections", "com.jackattackk246.files.MainActivityLightFrost", "Master Series", 0xFFF8FAFC, 0xFF0284C7))
            add(LauncherIconVariant("abyssal_trench", "Abyssal Trench", "Bioluminescent deep ocean floor with eerie cyan glow", "com.jackattackk246.files.MainActivityHydraCyan", "Master Series", 0xFF021422, 0xFF22D3EE))
            add(LauncherIconVariant("crimson_core", "Crimson Core", "Thermonuclear core shielding with emergency scarlet beacons", "com.jackattackk246.files.MainActivityCrimsonGlow", "Master Series", 0xFF260408, 0xFFEF4444))
            add(LauncherIconVariant("solar_corona", "Solar Corona", "Total eclipse chromosphere with brilliant golden solar flares", "com.jackattackk246.files.MainActivitySolarFlare", "Master Series", 0xFF221200, 0xFFF59E0B))
            add(LauncherIconVariant("prism_spectrum", "Prism Spectrum", "Optical refraction crystal dispersing pure full-gamut beams", "com.jackattackk246.files.MainActivityMidnightPrism", "Master Series", 0xFF101420, 0xFF818CF8))
            add(LauncherIconVariant("matrix_hex", "Matrix Hex Code", "Raw machine instruction hex dumps in terminal phosphor green", "com.jackattackk246.files.MainActivityGothMatrix", "Master Series", 0xFF061408, 0xFF4ADE80))
            add(LauncherIconVariant("tactical_camo", "Tactical Camo", "Military woodland field camouflage with muted olive drab", "com.jackattackk246.files.MainActivityDesertSage", "Master Series", 0xFF1C2218, 0xFFA3E635))
            add(LauncherIconVariant("royal_velvet", "Royal Velvet", "Coronation purple velvet drapery with polished gold embroidery", "com.jackattackk246.files.MainActivityRoyalAmethyst", "Master Series", 0xFF240638, 0xFFFDE047))
            add(LauncherIconVariant("silver_stream", "Silver Stream", "Liquid mercury stream flowing over polished slate bed", "com.jackattackk246.files.MainActivityPlatinumSilk", "Master Series", 0xFF1E222A, 0xFFCBD5E1))
            add(LauncherIconVariant("cosmic_dust", "Cosmic Dust", "Interstellar dust pillar with glowing newborn star clusters", "com.jackattackk246.files.MainActivityDeepSpace", "Master Series", 0xFF120A20, 0xFFC084FC))
            add(LauncherIconVariant("vaporwave_dusk", "Vaporwave Dusk", "Nostalgic 1984 aesthetic with pastel teal and flamingo pink", "com.jackattackk246.files.MainActivityNeonSynthwave", "Master Series", 0xFF1C1028, 0xFFF472B6))
            add(LauncherIconVariant("cyber_samurai", "Cyber Samurai", "Neo-Tokyo ronin lacquer armor with neon crimson katana edge", "com.jackattackk246.files.MainActivityJackOverlord", "Master Series", 0xFF20060A, 0xFFFF0055))
            add(LauncherIconVariant("arctic_expedition", "Arctic Expedition", "Glacial survival expedition station with hazard orange beacons", "com.jackattackk246.files.MainActivityFrozenTundra", "Master Series", 0xFF0A1E2C, 0xFFFB923C))
            add(LauncherIconVariant("onyx_marble", "Onyx Marble", "Fine black marble slab with delicate white quartz veins", "com.jackattackk246.files.MainActivityCeramicMatte", "Master Series", 0xFF101012, 0xFFE4E4E7))
            add(LauncherIconVariant("copper_rust", "Copper Verdigris", "Aged architectural copper with natural turquoise patina", "com.jackattackk246.files.MainActivityRustOxide", "Master Series", 0xFF142422, 0xFF2DD4BF))
            add(LauncherIconVariant("electric_lime", "Electric Lime", "Ultra high-visibility racing lime over asphalt composite", "com.jackattackk246.files.MainActivityNvidiaShield", "Master Series", 0xFF121A08, 0xFF84CC16))
            add(LauncherIconVariant("amethyst_geode", "Amethyst Geode", "Broken crystalline geode interior with deep violet facets", "com.jackattackk246.files.MainActivityRoyalAmethyst", "Master Series", 0xFF1E0A30, 0xFFD8B4FE))
            add(LauncherIconVariant("hazard_stripe", "Hazard Stripe", "Industrial safety barricade with bold black & safety yellow stripes", "com.jackattackk246.files.MainActivityBioHazard", "Master Series", 0xFF1A1600, 0xFFEAB308))
            add(LauncherIconVariant("ultimate_master_100", "Ultimate Master 100", "The definitive 100th catalog masterpiece profile with chromatic aura", "com.jackattackk246.files.MainActivityJackOverlord", "Master Series", 0xFF160818, 0xFFE11D48))
        }

        private const val PREFS_NAME = "launcher_icon_prefs"
        private const val KEY_ACTIVE_ICON = "active_launcher_icon_id"

        fun getActiveIconId(context: Context): String {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ACTIVE_ICON, "default") ?: "default"
        }

        fun setLauncherIcon(context: Context, variantId: String): Boolean {
            val target = ICON_VARIANTS.find { it.id == variantId } ?: return false
            val engine = IconChangerEngine(context)
            engine.safelyDeployTargetTheme(target.aliasClass)

            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ACTIVE_ICON, variantId)
                .apply()

            return true
        }
    }
}
