package com.jackattackk246.files.ui.theme

import androidx.compose.ui.graphics.Color

// 0. Dynamic Weather Canvas (Vivid Sky Blue #4FACFE to Deep Horizon Cobalt #00F2FE)
val WeatherGradientTop = Color(0xFF4FACFE)     // Vivid Sky Blue (Top 0.0)
val WeatherGradientBottom = Color(0xFF00F2FE)  // Deep Horizon Cobalt (Bottom 1.0)
val WeatherAccentPrimary = Color(0xFF0288D1)

// Translucent Masks & Card Containers
val TranslucentMidnightCharcoalMask = Color(0xA6121214) // 65% Translucent Midnight Charcoal Mask (#121214 with 0.65 opacity)
val TranslucentSlateCharcoalCard = Color(0xCC1E1E22)    // 80% Translucent Slate Charcoal (#1E1E22 with 0.80 opacity)

// Typography Standards
val TextCrispWhite = Color(0xFFFFFFFF)
val TextSilverGray = Color(0xFFE5E5EA)

// PRESET 01: OLED Pitch Black Palette (Pure black #000000)
val OledPitchBlackBg = Color(0xFF000000)
val OledPitchBlackSurface = Color(0xFF080808)
val OledPitchBlackCard = Color(0xCC1E1E22)
val OledPitchBlackBorder = Color(0xFF2E2E34)
val OledPitchBlackPrimary = Color(0xFF38BDF8)

// PRESET 02: Midnight Matte Black Palette (Dark charcoal #141414 to Jet Black #000000)
val MidnightGradientTop = Color(0xFF141414)    // Charcoal
val MidnightGradientBottom = Color(0xFF000000) // Jet Black
val MidnightBlackBg = Color(0xFF141414)
val MidnightBlackSurface = Color(0xFF1A1A1E)
val MidnightBlackCard = Color(0xCC1E1E22)
val MidnightBlackBorder = Color(0xFF2C2C3C)
val MidnightPrimary = Color(0xFF38BDF8)
val MidnightLavenderPrimary = Color(0xFFD0BCFF)
val MidnightSecondary = Color(0xFF38BDF8)
val MidnightOutline = Color(0xFF433B58)

// PRESET 03: Spring Emerald Palette (Forest green #0A1F0D, Neon Green #00FF66)
val SpringEmeraldGradientTop = Color(0xFF0A1F0D)      // Midnight Forest
val SpringEmeraldGradientBottom = Color(0xFF000000)   // Pitch Black
val SpringEmeraldBg = Color(0xFF0A1F0D)
val SpringEmeraldSurface = Color(0xFF0F2913)
val SpringEmeraldCard = Color(0xCC1E1E22)
val SpringEmeraldBorder = Color(0xFF00FF66)
val SpringEmeraldPrimary = Color(0xFF00FF66)          // Bright Neon Green (#00FF66)
val SpringEmeraldOnPrimary = Color(0xFF02260B)
val SpringEmeraldContainer = Color(0xFF0E3819)
val SpringEmeraldSecondary = Color(0xFF00FF66)

// Matrix Green (Alias for Spring Emerald / Cyber Green)
val MatrixGradientTop = Color(0xFF0A1F0D)
val MatrixGradientBottom = Color(0xFF000000)
val MatrixGreenBg = Color(0xFF0A1F0D)
val MatrixGreenSurface = Color(0xFF0F2913)
val MatrixGreenCard = Color(0xCC1E1E22)
val MatrixGreenBorder = Color(0xFF00FF66)
val MatrixGreenPrimary = Color(0xFF00FF66)
val MatrixGreenOnPrimary = Color(0xFF02260B)
val MatrixGreenContainer = Color(0xFF0E3819)
val MatrixSecondary = Color(0xFF00FF66)
val MatrixOutline = Color(0xFF1F5C2D)

// PRESET 04: Cyberpunk Amber Palette (Deep graphite backing #141418 with amber #FFB000 borders)
val CyberpunkAmberGradientTop = Color(0xFF1A1A22)
val CyberpunkAmberGradientBottom = Color(0xFF0D0D12)
val CyberpunkAmberBg = Color(0xFF141418)
val CyberpunkAmberSurface = Color(0xFF1C1C24)
val CyberpunkAmberCard = Color(0xCC1E1E22)
val CyberpunkAmberBorder = Color(0xFFFFB000)          // Sharp high-contrast amber yellow (#FFB000)
val CyberpunkAmberPrimary = Color(0xFFFFB000)
val CyberpunkAmberOnPrimary = Color(0xFF2A1B00)
val CyberpunkAmberContainer = Color(0xFF3D2700)
val CyberpunkAmberSecondary = Color(0xFFFFB000)

// 3. Deep Purple Palette (Royal Amethyst to Void Black)
val DeepPurpleGradientTop = Color(0xFF2E0854)    // Royal Amethyst
val DeepPurpleGradientBottom = Color(0xFF000000) // Void Black
val DeepPurpleBg = Color(0xFF0B0716)
val DeepPurpleSurface = Color(0xFF140E26)
val DeepPurpleCard = Color(0xCC1E1E22)
val DeepPurpleBorder = Color(0xFFC084FC)
val DeepPurplePrimary = Color(0xFFC084FC)        // Vivid Purple (#C084FC)
val DeepPurpleOnPrimary = Color(0xFF2E0061)
val DeepPurpleContainer = Color(0xFF451978)
val DeepPurpleSecondary = Color(0xFFC084FC)
val DeepPurpleOutline = Color(0xFF562E8F)

// 4. Ocean Blue Palette (Deep Sapphire to Abyssal Black)
val OceanBlueGradientTop = Color(0xFF0D2B45)     // Deep Sapphire
val OceanBlueGradientBottom = Color(0xFF000000)  // Abyssal Black
val OceanBlueBg = Color(0xFF060E1A)
val OceanBlueSurface = Color(0xFF0B1728)
val OceanBlueCard = Color(0xCC1E1E22)
val OceanBlueBorder = Color(0xFF38BDF8)
val OceanBluePrimary = Color(0xFF38BDF8)         // Sky Blue (#38BDF8)
val OceanBlueOnPrimary = Color(0xFF032840)
val OceanBlueContainer = Color(0xFF0C426D)
val OceanBlueSecondary = Color(0xFF38BDF8)
val OceanBlueOutline = Color(0xFF224E82)

// Legacy alias definitions
val PitchBlackBg = Color(0xFF000000)
val PitchBlackSurface = Color(0xFF0C0C0C)
val PitchBlackCard = Color(0xCC1E1E22)
val PitchBlackBorder = Color(0xFF252525)

// Universal High-Contrast Typography Anchors
val HighContrastWhite = Color(0xFFFFFFFF)
val HighContrastBlack = Color(0xFF000000)

// Light Palette Fallback Constants
val LightBg = Color(0xFFF6F4F8)
val LightSurface = Color(0xFFFFFFFF)
val LightCard = Color(0xFFEDE8F2)
val LightBorder = Color(0xFFD3CBE0)
