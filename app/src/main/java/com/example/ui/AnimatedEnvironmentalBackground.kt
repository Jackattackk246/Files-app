package com.jackattackk246.files.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import com.jackattackk246.files.model.EnvironmentalBackdropConfig
import com.jackattackk246.files.model.EnvironmentalSeason
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Animated Environmental Background Engine v2.4.6 Production.
 *
 * Implements:
 * 1. 180-Degree Vertical Linear Gradients anchored strictly from top (0.0) to bottom (1.0).
 * 2. Preset 01 (Midnight Workspace): Deep Cobalt Blue (#0D2040) -> Slate Indigo (#1A2B4C) with shooting star.
 * 3. Preset 02 (Daylight Workspace): High-saturation sky-blue (#0070E0) -> warm cerulean (#0288D1).
 * 4. Preset 03 (Tails' Mechanical Sandbox): Cell-shaded industrial gray & metallic bronze robot suit matrix.
 * 5. Low-overhead particle lifecycles with single-particle shooting star engagement.
 */
@Composable
fun AnimatedEnvironmentalBackground(
  config: EnvironmentalBackdropConfig,
  modifier: Modifier = Modifier
) {
  // Resolve effective season
  val effectiveSeason = remember(config.selectedSeason) {
    if (config.selectedSeason == EnvironmentalSeason.AUTO) {
      EnvironmentalSeason.resolveCurrentSystemSeason(Calendar.getInstance())
    } else {
      config.selectedSeason
    }
  }

  // Continuous loop animation ticker
  val infiniteTransition = rememberInfiniteTransition(label = "environmental_engine_v246")

  // Primary cyclic progress (16-second loop)
  val animProgress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 16000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "anim_progress"
  )

  // Solar pulse / ambient glow expansion
  val sunPulse by infiniteTransition.animateFloat(
    initialValue = 0.90f,
    targetValue = 1.10f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "sun_pulse"
  )

  // Amber alert pulse
  val amberAlertPulse by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 0.85f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "amber_alert_pulse"
  )

  // Shooting star 10-second lifecycle
  val shootingStarProgress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 10000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "shooting_star_ticker"
  )

  // Mechanical rotation ticker (for Preset 03 Sandbox)
  val mechanicalGearRotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 24000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "mechanical_gear_rotation"
  )

  // Rain fall ticker
  val rainProgress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 800, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rain_ticker"
  )

  // Deterministic Star Dot Matrix (120 lightweight nodes)
  val starMatrix = remember {
    List(120) { index ->
      StarDotState(
        id = index,
        xPercent = Random.nextFloat(),
        yPercent = Random.nextFloat(),
        radius = Random.nextFloat() * 1.8f + 0.8f,
        baseAlpha = Random.nextFloat() * 0.6f + 0.35f,
        twinkleSpeed = Random.nextFloat() * 2f + 1f,
        isCrossStar = index % 12 == 0
      )
    }
  }

  // Rain drop particle seeds
  val rainDropSeeds = remember {
    List(60) { index ->
      RainDropState(
        id = index,
        xPercent = Random.nextFloat(),
        yPercent = Random.nextFloat(),
        length = Random.nextFloat() * 24f + 16f,
        speed = Random.nextFloat() * 0.6f + 0.7f,
        alpha = Random.nextFloat() * 0.45f + 0.35f
      )
    }
  }

  // General seasonal particle seeds
  val particles = remember(effectiveSeason) {
    List(36) { index ->
      ParticleState(
        id = index,
        xPercent = Random.nextFloat(),
        yPercent = Random.nextFloat(),
        size = Random.nextFloat() * 12f + 6f,
        speed = Random.nextFloat() * 0.35f + 0.15f,
        swaySpeed = Random.nextFloat() * 1.4f + 0.7f,
        swayAmplitude = Random.nextFloat() * 28f + 10f,
        rotationSpeed = (Random.nextFloat() - 0.5f) * 100f,
        alpha = Random.nextFloat() * 0.5f + 0.25f,
        variant = index % 3
      )
    }
  }

  // Mechanical bronze particle sparks for Preset 03
  val bronzeSparks = remember {
    List(24) { index ->
      MechanicalSparkState(
        id = index,
        xPercent = Random.nextFloat(),
        yPercent = Random.nextFloat(),
        size = Random.nextFloat() * 3f + 1.5f,
        speed = Random.nextFloat() * 0.25f + 0.1f,
        colorHex = if (index % 2 == 0) 0xFFC68B59 else 0xFFD97706
      )
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .testTag("animated_environmental_background_container")
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val canvasWidth = size.width
      val canvasHeight = size.height

      if (canvasWidth <= 0 || canvasHeight <= 0) return@Canvas

      when (effectiveSeason) {
        // PRESET 01 (Midnight Workspace) & CLEAR SKY NIGHT PROFILE
        EnvironmentalSeason.MIDNIGHT_WORKSPACE, EnvironmentalSeason.CLEAR_NIGHT -> {
          drawMidnightWorkspaceBackdrop(canvasWidth, canvasHeight)
          if (config.enableParticles) {
            drawStarDotMatrix(starMatrix, animProgress, canvasWidth, canvasHeight)
            drawSingleShootingStarEngagement(shootingStarProgress, canvasWidth, canvasHeight)
          }
        }

        // PRESET 02 (Daylight Workspace) & SUNNY DAY / SUMMER PROFILE
        EnvironmentalSeason.DAYLIGHT_WORKSPACE, EnvironmentalSeason.SUNNY_DAY, EnvironmentalSeason.SUMMER -> {
          drawDaylightWorkspaceBackdrop(
            width = canvasWidth,
            height = canvasHeight,
            sunPulse = sunPulse * config.weatherReport.sunburstExpansion,
            cloudOpacity = config.weatherReport.cloudOpacity,
            animProgress = animProgress
          )
          if (config.enableParticles) {
            drawSummerGlimmer(particles, animProgress, canvasWidth, canvasHeight)
          }
        }

        // PRESET 03 (Tails' Mechanical Sandbox)
        EnvironmentalSeason.TAILS_MECHANICAL_SANDBOX -> {
          drawTailsMechanicalSandboxBackdrop(
            width = canvasWidth,
            height = canvasHeight,
            gearRotation = mechanicalGearRotation,
            animProgress = animProgress,
            sparks = bronzeSparks,
            enableParticles = config.enableParticles
          )
        }

        // OVERCAST / CLOUDY PROFILE
        EnvironmentalSeason.OVERCAST -> {
          drawOvercastCloudyBackdrop(canvasWidth, canvasHeight, config.weatherReport.cloudOpacity, animProgress)
        }

        // RAIN / THUNDERSTORM PROFILE
        EnvironmentalSeason.RAIN_THUNDERSTORM -> {
          drawRainThunderstormBackdrop(canvasWidth, canvasHeight)
          if (config.enableParticles) {
            drawRainfallVectorStrokes(rainDropSeeds, rainProgress, canvasWidth, canvasHeight)
          }
        }

        // AMBER ALERT PROFILE
        EnvironmentalSeason.AMBER_ALERT -> {
          drawAmberAlertBackdrop(canvasWidth, canvasHeight, amberAlertPulse)
        }

        // WINTER STATE
        EnvironmentalSeason.WINTER -> {
          drawWinterIceBackdrop(canvasWidth, canvasHeight)
          if (config.enableParticles) {
            drawStarDotMatrix(starMatrix, animProgress, canvasWidth, canvasHeight)
            drawWinterSnowflakes(particles, animProgress, canvasWidth, canvasHeight)
          }
        }

        // AUTUMN STATE (Autumn Copper #4A1525 to #A84B24)
        EnvironmentalSeason.AUTUMN -> {
          drawAutumnBackdrop(canvasWidth, canvasHeight)
          if (config.enableParticles) {
            drawStarDotMatrix(starMatrix.take(60), animProgress, canvasWidth, canvasHeight)
            drawAutumnLeaves(particles, animProgress, canvasWidth, canvasHeight)
          }
        }

        // SPRING STATE
        EnvironmentalSeason.SPRING -> {
          drawSpringBackdrop(canvasWidth, canvasHeight)
          if (config.enableParticles) {
            drawSpringPetalsAndDew(particles, animProgress, canvasWidth, canvasHeight)
          }
        }

        // AUTO Fallback
        EnvironmentalSeason.AUTO -> {
          drawMidnightWorkspaceBackdrop(canvasWidth, canvasHeight)
          if (config.enableParticles) {
            drawStarDotMatrix(starMatrix, animProgress, canvasWidth, canvasHeight)
            drawSingleShootingStarEngagement(shootingStarProgress, canvasWidth, canvasHeight)
          }
        }
      }
    }
  }
}

private data class StarDotState(
  val id: Int,
  val xPercent: Float,
  val yPercent: Float,
  val radius: Float,
  val baseAlpha: Float,
  val twinkleSpeed: Float,
  val isCrossStar: Boolean
)

private data class RainDropState(
  val id: Int,
  val xPercent: Float,
  val yPercent: Float,
  val length: Float,
  val speed: Float,
  val alpha: Float
)

private data class ParticleState(
  val id: Int,
  val xPercent: Float,
  val yPercent: Float,
  val size: Float,
  val speed: Float,
  val swaySpeed: Float,
  val swayAmplitude: Float,
  val rotationSpeed: Float,
  val alpha: Float,
  val variant: Int
)

private data class MechanicalSparkState(
  val id: Int,
  val xPercent: Float,
  val yPercent: Float,
  val size: Float,
  val speed: Float,
  val colorHex: Long
)

// =========================================================================
// 1. PRESET 01: MIDNIGHT WORKSPACE (True 180-Degree Vertical Linear Gradient)
// =========================================================================
private fun DrawScope.drawMidnightWorkspaceBackdrop(width: Float, height: Float) {
  // True 180-degree vertical linear gradient anchored strictly across 0.0 to 1.0
  val midnightGradient = Brush.verticalGradient(
    colors = listOf(
      Color(0xFF0D2040), // Deep Cobalt Blue Zenith (Top 0.0)
      Color(0xFF102446), // Upper Cobalt
      Color(0xFF142749), // Mid Cobalt-Indigo Transition
      Color(0xFF17294B), // Lower Indigo
      Color(0xFF1A2B4C)  // Slate Indigo Horizon Base (Bottom 1.0)
    ),
    startY = 0f,
    endY = height
  )
  drawRect(brush = midnightGradient, size = Size(width, height))

  // Soft ambient cosmic glow
  val cosmicGlow = Brush.radialGradient(
    colors = listOf(
      Color(0x286366F1),
      Color(0x108B5CF6),
      Color.Transparent
    ),
    center = Offset(width * 0.72f, height * 0.20f),
    radius = width * 0.85f
  )
  drawCircle(brush = cosmicGlow, radius = width * 0.85f, center = Offset(width * 0.72f, height * 0.20f))
}

// =========================================================================
// 2. PRESET 02: DAYLIGHT WORKSPACE (True 180-Degree Vertical Linear Gradient)
// =========================================================================
private fun DrawScope.drawDaylightWorkspaceBackdrop(
  width: Float,
  height: Float,
  sunPulse: Float,
  cloudOpacity: Float,
  animProgress: Float
) {
  // True 180-degree vertical linear gradient anchored strictly across 0.0 to 1.0 (#4FACFE top to #00F2FE bottom)
  val daylightGradient = Brush.verticalGradient(
    colors = listOf(
      Color(0xFF4FACFE), // Vivid Sky Blue (Top 0.0)
      Color(0xFF3CA8FE), // Upper Horizon
      Color(0xFF28BDFD), // Mid Sky Transition
      Color(0xFF13D7FB), // Lower Cyan
      Color(0xFF00F2FE)  // Deep Horizon Cobalt (Bottom 1.0)
    ),
    startY = 0f,
    endY = height
  )
  drawRect(brush = daylightGradient, size = Size(width, height))

  // Solar Core Glare Container
  val sunCenterX = width * 0.82f
  val sunCenterY = height * 0.16f
  val baseRadius = width * 0.24f * sunPulse

  // Outer solar flare
  val outerGlare = Brush.radialGradient(
    colors = listOf(
      Color(0x55FFF3B0),
      Color(0x25FFD54F),
      Color(0x08FFE082),
      Color.Transparent
    ),
    center = Offset(sunCenterX, sunCenterY),
    radius = baseRadius * 2.4f
  )
  drawCircle(brush = outerGlare, radius = baseRadius * 2.4f, center = Offset(sunCenterX, sunCenterY))

  // Inner radiant disc
  val innerSun = Brush.radialGradient(
    colors = listOf(
      Color(0xFFFFFDE7),
      Color(0xFFFFEE58),
      Color(0xFFFDD835),
      Color.Transparent
    ),
    center = Offset(sunCenterX, sunCenterY),
    radius = baseRadius * 0.95f
  )
  drawCircle(brush = innerSun, radius = baseRadius * 0.95f, center = Offset(sunCenterX, sunCenterY))

  // 12 Radiating Vector Glare Beams
  val beamLength = baseRadius * 1.8f
  for (i in 0 until 12) {
    val angle = (i * (2 * PI / 12) + (animProgress * 2 * PI * 0.15f)).toFloat()
    val bx = sunCenterX + cos(angle) * beamLength
    val by = sunCenterY + sin(angle) * beamLength

    val beamBrush = Brush.linearGradient(
      colors = listOf(
        Color(0x40FFF59D),
        Color.Transparent
      ),
      start = Offset(sunCenterX, sunCenterY),
      end = Offset(bx, by)
    )
    drawLine(
      brush = beamBrush,
      start = Offset(sunCenterX, sunCenterY),
      end = Offset(bx, by),
      strokeWidth = 2.2f,
      cap = StrokeCap.Round
    )
  }

  // Drifting Soft Cloud Opacity Layers
  drawDriftingClouds(width, height, cloudOpacity, animProgress)
}

// =========================================================================
// 3. PRESET 03: TAILS' MECHANICAL SANDBOX
// Cell-shaded industrial gray & metallic bronze robot suit matrix overlay
// =========================================================================
private fun DrawScope.drawTailsMechanicalSandboxBackdrop(
  width: Float,
  height: Float,
  gearRotation: Float,
  animProgress: Float,
  sparks: List<MechanicalSparkState>,
  enableParticles: Boolean
) {
  // 1. True 180-degree vertical linear gradient (Industrial Dark Slate to Carbon Gray)
  val industrialGradient = Brush.verticalGradient(
    colors = listOf(
      Color(0xFF181C22), // Top Industrial Dark Slate (0.0)
      Color(0xFF1D222A), // Upper Carbon
      Color(0xFF222832), // Mid Industrial Steel
      Color(0xFF29303C), // Lower Mechanized Slate
      Color(0xFF15191F)  // Bottom Carbon Black Base (1.0)
    ),
    startY = 0f,
    endY = height
  )
  drawRect(brush = industrialGradient, size = Size(width, height))

  // 2. Cell-shaded Industrial Hex / Isometric Blueprint Grid Matrix
  val gridSpacing = 44f
  val gridCols = (width / gridSpacing).toInt() + 1
  val gridRows = (height / gridSpacing).toInt() + 1

  // Draw technical grid mesh lines
  for (c in 0..gridCols) {
    val x = c * gridSpacing
    drawLine(
      color = Color(0x14C68B59), // Faint Metallic Bronze Gridline
      start = Offset(x, 0f),
      end = Offset(x, height),
      strokeWidth = 1f
    )
  }
  for (r in 0..gridRows) {
    val y = r * gridSpacing
    drawLine(
      color = Color(0x14C68B59),
      start = Offset(0f, y),
      end = Offset(width, y),
      strokeWidth = 1f
    )
  }

  // 3. Hydraulic Circuit Traces & Metallic Bronze Accent Vectors (#C68B59, #8C6239, #D97706)
  val bronzeTraceColor = Color(0x35C68B59)
  val bronzeNodeColor = Color(0x80D97706)

  // Circuit Bus Line 1 (Upper diagonal telemetry trace)
  val path1 = Path().apply {
    moveTo(width * 0.05f, height * 0.12f)
    lineTo(width * 0.40f, height * 0.12f)
    lineTo(width * 0.55f, height * 0.22f)
    lineTo(width * 0.95f, height * 0.22f)
  }
  drawPath(path = path1, color = bronzeTraceColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))

  // Circuit Bus Line 2 (Mid-lower telemetry trace)
  val path2 = Path().apply {
    moveTo(width * 0.10f, height * 0.78f)
    lineTo(width * 0.45f, height * 0.78f)
    lineTo(width * 0.60f, height * 0.68f)
    lineTo(width * 0.90f, height * 0.68f)
  }
  drawPath(path = path2, color = bronzeTraceColor, style = Stroke(width = 2.0f, cap = StrokeCap.Round))

  // Circuit telemetry nodes
  drawCircle(color = bronzeNodeColor, radius = 4f, center = Offset(width * 0.40f, height * 0.12f))
  drawCircle(color = bronzeNodeColor, radius = 4f, center = Offset(width * 0.55f, height * 0.22f))
  drawCircle(color = bronzeNodeColor, radius = 4f, center = Offset(width * 0.45f, height * 0.78f))
  drawCircle(color = bronzeNodeColor, radius = 4f, center = Offset(width * 0.60f, height * 0.68f))

  // 4. Mechanical Robot Gear Vectors (Cell-shaded rotating telemetry glyphs)
  val gear1Center = Offset(width * 0.85f, height * 0.32f)
  val gear1Radius = width * 0.28f
  drawMechanicalGear(center = gear1Center, radius = gear1Radius, rotationDegrees = gearRotation, toothCount = 14)

  val gear2Center = Offset(width * 0.15f, height * 0.55f)
  val gear2Radius = width * 0.20f
  drawMechanicalGear(center = gear2Center, radius = gear2Radius, rotationDegrees = -gearRotation * 1.4f, toothCount = 10)

  // 5. Metallic Bronze Particle Sparks
  if (enableParticles) {
    sparks.forEach { spark ->
      val sparkY = (spark.yPercent + animProgress * spark.speed * 2f) % 1.0f
      val sparkX = spark.xPercent * width
      val y = sparkY * height
      val pulse = (sin((animProgress * 6f + spark.id).toDouble()) * 0.4f + 0.6f).toFloat()

      drawCircle(
        color = Color(spark.colorHex).copy(alpha = 0.45f * pulse),
        radius = spark.size,
        center = Offset(sparkX, y)
      )
    }
  }

  // 6. Translucent 60% Dark Charcoal Glass Mask (#121212 @ 60% opacity)
  drawRect(
    color = Color(0x99121212),
    size = Size(width, height)
  )
}

/**
 * Draws a cell-shaded mechanical robot suit gear vector.
 */
private fun DrawScope.drawMechanicalGear(
  center: Offset,
  radius: Float,
  rotationDegrees: Float,
  toothCount: Int
) {
  val bronzeLine = Color(0x28C68B59)
  val bronzeFill = Color(0x0C8C6239)

  // Outer gear pitch circle
  drawCircle(
    color = bronzeFill,
    radius = radius,
    center = center
  )
  drawCircle(
    color = bronzeLine,
    radius = radius,
    center = center,
    style = Stroke(width = 2f)
  )

  // Inner concentric ring
  drawCircle(
    color = bronzeLine,
    radius = radius * 0.55f,
    center = center,
    style = Stroke(width = 1.5f)
  )

  // Central axle node
  drawCircle(
    color = Color(0x40C68B59),
    radius = radius * 0.22f,
    center = center
  )

  // Gear Teeth Spokes
  val rotationRad = (rotationDegrees * PI / 180f).toFloat()
  for (i in 0 until toothCount) {
    val angle = rotationRad + (i * (2 * PI / toothCount)).toFloat()
    val innerX = center.x + cos(angle) * (radius * 0.55f)
    val innerY = center.y + sin(angle) * (radius * 0.55f)
    val outerX = center.x + cos(angle) * (radius * 1.15f)
    val outerY = center.y + sin(angle) * (radius * 1.15f)

    drawLine(
      color = bronzeLine,
      start = Offset(innerX, innerY),
      end = Offset(outerX, outerY),
      strokeWidth = 3f,
      cap = StrokeCap.Square
    )
  }
}

// =========================================================================
// 4. OVERCAST / CLOUDY PROFILE (True 180-Degree Vertical Linear Gradient)
// =========================================================================
private fun DrawScope.drawOvercastCloudyBackdrop(
  width: Float,
  height: Float,
  cloudOpacity: Float,
  animProgress: Float
) {
  val overcastGradient = Brush.verticalGradient(
    colors = listOf(
      Color(0xFF2A323D), // Top Slate Gray (0.0)
      Color(0xFF252D37),
      Color(0xFF212832),
      Color(0xFF1D232C),
      Color(0xFF191E26)  // Base Dark Charcoal (1.0)
    ),
    startY = 0f,
    endY = height
  )
  drawRect(brush = overcastGradient, size = Size(width, height))
  drawDriftingClouds(width, height, cloudOpacity * 1.25f, animProgress)
}

// =========================================================================
// 5. RAIN / THUNDERSTORM PROFILE (True 180-Degree Vertical Linear Gradient)
// =========================================================================
private fun DrawScope.drawRainThunderstormBackdrop(width: Float, height: Float) {
  val rainGradient = Brush.verticalGradient(
    colors = listOf(
      Color(0xFF08171B), // Top Deep Charcoal Teal (0.0)
      Color(0xFF0B1F25),
      Color(0xFF0E282F),
      Color(0xFF11323B),
      Color(0xFF143B45)  // Bottom Base Teal (1.0)
    ),
    startY = 0f,
    endY = height
  )
  drawRect(brush = rainGradient, size = Size(width, height))
}

// =========================================================================
// 6. AMBER ALERT PROFILE (True 180-Degree Vertical Linear Gradient)
// =========================================================================
private fun DrawScope.drawAmberAlertBackdrop(width: Float, height: Float, pulse: Float) {
  val amberGradient = Brush.verticalGradient(
    colors = listOf(
      Color(0xFF1F1206), // Top Dark Amber (0.0)
      Color(0xFF2B1908),
      Color(0xFF38210A),
      Color(0xFF241507),
      Color(0xFF150C04)  // Bottom Black Amber (1.0)
    ),
    startY = 0f,
    endY = height
  )
  drawRect(brush = amberGradient, size = Size(width, height))

  // Emergency Pulsing Amber Border Aura
  drawRect(
    color = Color(0xFFD97706).copy(alpha = pulse * 0.35f),
    size = Size(width, height),
    style = Stroke(width = 16f)
  )
}

// =========================================================================
// 7. WINTER ICE STATE (True 180-Degree Vertical Linear Gradient)
// =========================================================================
private fun DrawScope.drawWinterIceBackdrop(width: Float, height: Float) {
  val iceGradient = Brush.verticalGradient(
    colors = listOf(
      Color(0xFF0A1E2C), // Top Deep Frost Blue (0.0)
      Color(0xFF0E283A),
      Color(0xFF123249),
      Color(0xFF163D58),
      Color(0xFF1B4867)  // Bottom Crystalline Cyan (1.0)
    ),
    startY = 0f,
    endY = height
  )
  drawRect(brush = iceGradient, size = Size(width, height))
}

// =========================================================================
// 8. AUTUMN COPPER STATE (True 180-Degree Vertical Linear Gradient)
// =========================================================================
private fun DrawScope.drawAutumnBackdrop(width: Float, height: Float) {
  // Shifting from Deep Burgundy (#4A1525) down to Muted Burnt Orange (#A84B24)
  val autumnGradient = Brush.verticalGradient(
    colors = listOf(
      Color(0xFF4A1525), // Top Deep Burgundy (0.0)
      Color(0xFF622225),
      Color(0xFF7A2F25),
      Color(0xFF913D24),
      Color(0xFFA84B24)  // Bottom Muted Burnt Orange (1.0)
    ),
    startY = 0f,
    endY = height
  )
  drawRect(brush = autumnGradient, size = Size(width, height))
}

// =========================================================================
// 9. SPRING STATE (True 180-Degree Vertical Linear Gradient)
// =========================================================================
private fun DrawScope.drawSpringBackdrop(width: Float, height: Float) {
  val springGradient = Brush.verticalGradient(
    colors = listOf(
      Color(0xFF0A241C), // Top Dark Emerald (0.0)
      Color(0xFF0E3025),
      Color(0xFF133D30),
      Color(0xFF184B3B),
      Color(0xFF1D5845)  // Bottom Spring Mint (1.0)
    ),
    startY = 0f,
    endY = height
  )
  drawRect(brush = springGradient, size = Size(width, height))
}

// =========================================================================
// VECTOR SHADER & PARTICLE DRAWING HELPERS
// =========================================================================

/**
 * SHOOTING STAR ENGAGEMENT:
 * Low-overhead particle system spawning a single white streak line every 8-12 seconds
 * drifting at a 45-degree downward diagonal.
 * RESTRICTS active particles to a MAXIMUM of one at any single millisecond.
 */
private fun DrawScope.drawSingleShootingStarEngagement(
  progress: Float,
  width: Float,
  height: Float
) {
  val startWindow = 0.05f
  val endWindow = 0.20f

  if (progress in startWindow..endWindow) {
    val localProgress = (progress - startWindow) / (endWindow - startWindow)
    val startX = width * 0.80f - (localProgress * width * 0.65f)
    val startY = height * 0.08f + (localProgress * height * 0.45f)
    val streakLength = 70f + (sin((localProgress * PI).toFloat()) * 40f)

    // 45-degree diagonal vector components
    val endX = startX - (streakLength * 0.7071f)
    val endY = startY + (streakLength * 0.7071f)

    val streakAlpha = sin((localProgress * PI).toFloat()).coerceIn(0f, 1f)

    // Head glow
    drawCircle(
      color = Color.White.copy(alpha = streakAlpha * 0.95f),
      radius = 2.8f,
      center = Offset(endX, endY)
    )

    // Streak Tail
    val streakBrush = Brush.linearGradient(
      colors = listOf(
        Color.Transparent,
        Color(0x80E0F2FE).copy(alpha = streakAlpha * 0.5f),
        Color.White.copy(alpha = streakAlpha * 0.95f)
      ),
      start = Offset(startX, startY),
      end = Offset(endX, endY)
    )

    drawLine(
      brush = streakBrush,
      start = Offset(startX, startY),
      end = Offset(endX, endY),
      strokeWidth = 2.4f,
      cap = StrokeCap.Round
    )
  }
}

/**
 * Deterministic twinkling Star Dot Matrix.
 */
private fun DrawScope.drawStarDotMatrix(
  stars: List<StarDotState>,
  progress: Float,
  width: Float,
  height: Float
) {
  stars.forEach { star ->
    val x = star.xPercent * width
    val y = star.yPercent * height
    val twinkle = (sin((progress * 2 * PI * star.twinkleSpeed + star.id).toDouble()) * 0.5f + 0.5f).toFloat()
    val alpha = (star.baseAlpha * twinkle).coerceIn(0.1f, 1.0f)

    drawCircle(
      color = Color.White.copy(alpha = alpha),
      radius = star.radius,
      center = Offset(x, y)
    )
  }
}

/**
 * Drifting Soft Cloud Opacity Layers.
 */
private fun DrawScope.drawDriftingClouds(
  width: Float,
  height: Float,
  opacity: Float,
  progress: Float
) {
  val cloudColor = Color.White.copy(alpha = (opacity * 0.35f).coerceIn(0.05f, 0.65f))
  val drift1 = (progress * width * 0.25f) % width
  val drift2 = (progress * width * 0.15f + width * 0.5f) % width

  // Cloud layer 1
  drawCircle(
    color = cloudColor,
    radius = width * 0.35f,
    center = Offset(drift1 - width * 0.1f, height * 0.14f)
  )
  drawCircle(
    color = cloudColor,
    radius = width * 0.28f,
    center = Offset(drift1 + width * 0.2f, height * 0.12f)
  )

  // Cloud layer 2
  drawCircle(
    color = cloudColor.copy(alpha = cloudColor.alpha * 0.8f),
    radius = width * 0.40f,
    center = Offset(drift2, height * 0.22f)
  )
}

/**
 * Summer particle shimmer.
 */
private fun DrawScope.drawSummerGlimmer(
  particles: List<ParticleState>,
  progress: Float,
  width: Float,
  height: Float
) {
  particles.forEach { p ->
    val yProgress = (p.yPercent - progress * p.speed + 1.0f) % 1.0f
    val x = p.xPercent * width + sin((progress * 2 * PI * p.swaySpeed + p.id).toDouble()).toFloat() * p.swayAmplitude
    val y = yProgress * height

    drawCircle(
      color = Color(0xFFFFF9C4).copy(alpha = p.alpha * 0.5f),
      radius = p.size * 0.4f,
      center = Offset(x, y)
    )
  }
}

/**
 * Vertical rainfall strokes.
 */
private fun DrawScope.drawRainfallVectorStrokes(
  raindrops: List<RainDropState>,
  progress: Float,
  width: Float,
  height: Float
) {
  val rainColor = Color(0xFF90CAF9)
  raindrops.forEach { drop ->
    val yProgress = (drop.yPercent + progress * drop.speed * 2.5f) % 1.0f
    val startX = drop.xPercent * width
    val startY = yProgress * height
    val endY = startY + drop.length

    drawLine(
      color = rainColor.copy(alpha = drop.alpha),
      start = Offset(startX, startY),
      end = Offset(startX, endY),
      strokeWidth = 1.5f,
      cap = StrokeCap.Round
    )
  }
}

/**
 * Winter snowflakes.
 */
private fun DrawScope.drawWinterSnowflakes(
  particles: List<ParticleState>,
  progress: Float,
  width: Float,
  height: Float
) {
  particles.forEach { p ->
    val yProgress = (p.yPercent + progress * p.speed) % 1.0f
    val x = p.xPercent * width + sin((progress * 2 * PI * p.swaySpeed + p.id).toDouble()).toFloat() * p.swayAmplitude
    val y = yProgress * height

    drawCircle(
      color = Color.White.copy(alpha = p.alpha * 0.8f),
      radius = p.size * 0.35f,
      center = Offset(x, y)
    )
  }
}

/**
 * Autumn drifting leaves.
 */
private fun DrawScope.drawAutumnLeaves(
  particles: List<ParticleState>,
  progress: Float,
  width: Float,
  height: Float
) {
  val leafColors = listOf(Color(0xFFD97706), Color(0xFFEA580C), Color(0xFFB45309))
  particles.forEach { p ->
    val yProgress = (p.yPercent + progress * p.speed) % 1.0f
    val x = p.xPercent * width + sin((progress * 2 * PI * p.swaySpeed + p.id).toDouble()).toFloat() * p.swayAmplitude
    val y = yProgress * height
    val color = leafColors[p.variant % leafColors.size]

    drawCircle(
      color = color.copy(alpha = p.alpha * 0.75f),
      radius = p.size * 0.45f,
      center = Offset(x, y)
    )
  }
}

/**
 * Spring petals and dew shimmer.
 */
private fun DrawScope.drawSpringPetalsAndDew(
  particles: List<ParticleState>,
  progress: Float,
  width: Float,
  height: Float
) {
  val petalColors = listOf(Color(0xFFF472B6), Color(0xFF6EE7B7), Color(0xFFFDE047))
  particles.forEach { p ->
    val yProgress = (p.yPercent + progress * p.speed * 0.7f) % 1.0f
    val x = p.xPercent * width + sin((progress * 2 * PI * p.swaySpeed + p.id).toDouble()).toFloat() * p.swayAmplitude
    val y = yProgress * height
    val color = petalColors[p.variant % petalColors.size]

    drawCircle(
      color = color.copy(alpha = p.alpha * 0.6f),
      radius = p.size * 0.35f,
      center = Offset(x, y)
    )
  }
}
