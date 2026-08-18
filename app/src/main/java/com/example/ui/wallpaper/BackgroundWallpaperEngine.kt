package com.jackattackk246.files.ui.wallpaper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.jackattackk246.files.model.BuiltInWallpaper
import com.jackattackk246.files.model.EnvironmentalBackdropConfig
import com.jackattackk246.files.model.EnvironmentalSeason
import com.jackattackk246.files.model.WallpaperConfig
import com.jackattackk246.files.ui.AnimatedEnvironmentalBackground
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Background Wallpaper & Parallax Tilt Physics Engine
 */
@Composable
fun DynamicWallpaperHost(
  environmentalConfig: EnvironmentalBackdropConfig,
  wallpaperConfig: WallpaperConfig,
  isDarkTheme: Boolean,
  enableGyroscope: Boolean = true,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // 1. Context BroadcastReceiver for Time Tick & Timezone changes (Instant Invalidation)
  var timeInvalidationToken by remember { mutableStateOf(System.currentTimeMillis()) }

  DisposableEffect(context) {
    val receiver = object : BroadcastReceiver() {
      override fun onReceive(ctx: Context?, intent: Intent?) {
        timeInvalidationToken = System.currentTimeMillis()
      }
    }
    val filter = IntentFilter().apply {
      addAction(Intent.ACTION_TIME_TICK)
      addAction(Intent.ACTION_TIME_CHANGED)
      addAction(Intent.ACTION_TIMEZONE_CHANGED)
    }
    context.registerReceiver(receiver, filter)

    onDispose {
      try {
        context.unregisterReceiver(receiver)
      } catch (e: Exception) {
        // Ignored
      }
    }
  }

  // 2. Hardware Gyroscope / Accelerometer Sensor Parallax Tracker
  var tiltOffsetX by remember { mutableStateOf(0f) }
  var tiltOffsetY by remember { mutableStateOf(0f) }

  val animatedTiltX by animateFloatAsState(
    targetValue = tiltOffsetX,
    animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
    label = "parallax_tilt_x"
  )
  val animatedTiltY by animateFloatAsState(
    targetValue = tiltOffsetY,
    animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
    label = "parallax_tilt_y"
  )

  DisposableEffect(context, enableGyroscope) {
    if (!enableGyroscope) return@DisposableEffect onDispose {}

    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    val gyroSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
      ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val listener = object : SensorEventListener {
      override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val vx = event.values.getOrNull(0) ?: 0f
        val vy = event.values.getOrNull(1) ?: 0f
        // Smooth translation coordinate filter (bounded to +/- 24dp)
        tiltOffsetX = (-vx * 6f).coerceIn(-24f, 24f)
        tiltOffsetY = (vy * 6f).coerceIn(-24f, 24f)
      }

      override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    sensorManager?.registerListener(listener, gyroSensor, SensorManager.SENSOR_DELAY_GAME)

    onDispose {
      sensorManager?.unregisterListener(listener)
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .graphicsLayer {
        translationX = animatedTiltX
        translationY = animatedTiltY
      }
      .testTag("dynamic_wallpaper_host")
  ) {
    // Render Built-in Wallpaper pattern if set
    if (wallpaperConfig.builtInPattern != null) {
      BuiltInWallpaperBackdrop(pattern = wallpaperConfig.builtInPattern)
    } else {
      // Dynamic Environmental Theme / Season Backdrop with Time Invalidation Token
      key(timeInvalidationToken) {
        AnimatedEnvironmentalBackground(config = environmentalConfig)
      }
    }

    // Celestial Vector Layer: Glowing Wireframe Moon in Dark Mode, Flat-art Sun with Flare Rings in Light Mode
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      if (w <= 0f || h <= 0f) return@Canvas

      if (isDarkTheme) {
        drawGlowingWireframeMoon(w, h)
      } else {
        drawFlatVectorSunWithRings(w, h)
      }
    }
  }
}

/**
 * Minimalist Glowing Wireframe Moon in the top-right quadrant for dark mode
 */
private fun DrawScope.drawGlowingWireframeMoon(width: Float, height: Float) {
  val moonCenter = Offset(width * 0.82f, height * 0.12f)
  val moonRadius = width * 0.09f

  // Outer ambient lunar glow
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(Color(0x3338BDF8), Color(0x10818CF8), Color.Transparent),
      center = moonCenter,
      radius = moonRadius * 2.5f
    ),
    radius = moonRadius * 2.5f,
    center = moonCenter
  )

  // Crescent / Wireframe Orbit Rings
  drawCircle(
    color = Color(0x4438BDF8),
    radius = moonRadius,
    center = moonCenter,
    style = Stroke(width = 1.8f)
  )

  drawCircle(
    color = Color(0x22818CF8),
    radius = moonRadius * 1.35f,
    center = moonCenter,
    style = Stroke(width = 1.2f)
  )

  // Inner Crescent Path
  val crescentPath = Path().apply {
    addOval(androidx.compose.ui.geometry.Rect(moonCenter.x - moonRadius, moonCenter.y - moonRadius, moonCenter.x + moonRadius, moonCenter.y + moonRadius))
  }
  drawCircle(
    color = Color(0x1538BDF8),
    radius = moonRadius,
    center = moonCenter
  )

  // Geometric crater nodes
  drawCircle(Color(0x447DD3FC), radius = moonRadius * 0.22f, center = Offset(moonCenter.x - moonRadius * 0.25f, moonCenter.y - moonRadius * 0.2f), style = Stroke(width = 1.2f))
  drawCircle(Color(0x337DD3FC), radius = moonRadius * 0.15f, center = Offset(moonCenter.x + moonRadius * 0.2f, moonCenter.y + moonRadius * 0.25f), style = Stroke(width = 1f))
  drawCircle(Color(0x44FFFFFF), radius = 2f, center = Offset(moonCenter.x - moonRadius * 0.25f, moonCenter.y - moonRadius * 0.2f))
}

/**
 * Clean Flat-Art Vector Sun with Geometric Ring Flares for light mode
 */
private fun DrawScope.drawFlatVectorSunWithRings(width: Float, height: Float) {
  val sunCenter = Offset(width * 0.82f, height * 0.12f)
  val sunRadius = width * 0.10f

  // Geometric Ring Flares
  val ringColors = listOf(Color(0x33FBBF24), Color(0x22F59E0B), Color(0x15F97316))
  val radii = listOf(sunRadius * 1.4f, sunRadius * 1.9f, sunRadius * 2.5f)

  radii.forEachIndexed { idx, r ->
    drawCircle(
      color = ringColors[idx % ringColors.size],
      radius = r,
      center = sunCenter,
      style = Stroke(width = 1.5f)
    )
  }

  // Flat Art Sun Core
  drawCircle(
    color = Color(0xFFFDE047),
    radius = sunRadius,
    center = sunCenter
  )
  drawCircle(
    color = Color(0xFFF59E0B),
    radius = sunRadius,
    center = sunCenter,
    style = Stroke(width = 2.5f)
  )

  // 8 Minimal Geometric Solar Rays
  for (i in 0 until 8) {
    val angle = (i * (360.0 / 8)) * (PI / 180.0)
    val startR = sunRadius * 1.12f
    val endR = sunRadius * 1.55f
    val p1 = Offset(sunCenter.x + (startR * cos(angle)).toFloat(), sunCenter.y + (startR * sin(angle)).toFloat())
    val p2 = Offset(sunCenter.x + (endR * cos(angle)).toFloat(), sunCenter.y + (endR * sin(angle)).toFloat())

    drawLine(
      color = Color(0xFFF59E0B),
      start = p1,
      end = p2,
      strokeWidth = 2.2f,
      cap = StrokeCap.Round
    )
  }
}
