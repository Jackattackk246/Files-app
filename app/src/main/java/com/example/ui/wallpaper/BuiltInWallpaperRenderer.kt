package com.jackattackk246.files.ui.wallpaper

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.jackattackk246.files.model.BuiltInWallpaper
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BuiltInWallpaperBackdrop(
  pattern: BuiltInWallpaper,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier.fillMaxSize()) {
    when (pattern) {
      BuiltInWallpaper.CYBER_GRID -> drawCyberGrid()
      BuiltInWallpaper.SYNTH_WAVEFRONT -> drawSynthWavefront()
      BuiltInWallpaper.ORBIT_VOID -> drawOrbitVoid()
      BuiltInWallpaper.ECHO_LINES -> drawEchoLines()
      BuiltInWallpaper.MATRIX_STREAM -> drawMatrixStream()
      BuiltInWallpaper.SAGE_GEOMETRY -> drawSageGeometry()
      BuiltInWallpaper.OBSIDIAN_SHARDS -> drawObsidianShards()
      BuiltInWallpaper.COPPER_FUSE -> drawCopperFuse()
      BuiltInWallpaper.SOLAR_CORONA -> drawSolarCorona()
      BuiltInWallpaper.FROST_POLYGON -> drawFrostPolygon()
    }
  }
}

// 01. Cyber Grid — Monochromatic grid lines over deep slate
private fun DrawScope.drawCyberGrid() {
  drawRect(
    brush = Brush.verticalGradient(
      listOf(Color(0xFF080C14), Color(0xFF0F172A), Color(0xFF05080E))
    )
  )

  val step = 36f
  val gridColor = Color(0x3338BDF8)
  val majorGridColor = Color(0x6638BDF8)
  val dotColor = Color(0xFF38BDF8)

  var x = 0f
  var col = 0
  while (x < size.width) {
    val strokeColor = if (col % 4 == 0) majorGridColor else gridColor
    val strokeW = if (col % 4 == 0) 1.5f else 0.8f
    drawLine(
      color = strokeColor,
      start = Offset(x, 0f),
      end = Offset(x, size.height),
      strokeWidth = strokeW
    )
    x += step
    col++
  }

  var y = 0f
  var row = 0
  while (y < size.height) {
    val strokeColor = if (row % 4 == 0) majorGridColor else gridColor
    val strokeW = if (row % 4 == 0) 1.5f else 0.8f
    drawLine(
      color = strokeColor,
      start = Offset(0f, y),
      end = Offset(size.width, y),
      strokeWidth = strokeW
    )
    y += step
    row++
  }

  // Accent Intersection Crossbars and Dots
  var ix = 0f
  while (ix < size.width) {
    var iy = 0f
    while (iy < size.height) {
      if (((ix.toInt() / step.toInt()) + (iy.toInt() / step.toInt())) % 6 == 0) {
        drawCircle(color = dotColor, radius = 2.5f, center = Offset(ix, iy))
      }
      iy += step * 2
    }
    ix += step * 2
  }
}

// 02. Synth Wavefront — Muted geometric mountain paths
private fun DrawScope.drawSynthWavefront() {
  drawRect(
    brush = Brush.verticalGradient(
      listOf(Color(0xFF0E041A), Color(0xFF1E0B36), Color(0xFF0A0214))
    )
  )

  val w = size.width
  val h = size.height

  // Background neon wireframe sun arc
  val sunCenter = Offset(w * 0.5f, h * 0.42f)
  val sunRadius = w * 0.28f
  for (r in 3..6) {
    drawCircle(
      color = Color(0x33F43F5E),
      radius = sunRadius * (r / 6f),
      center = sunCenter,
      style = Stroke(width = 1.2f)
    )
  }

  // Geometric low-poly mountain silhouettes (Layer 1 - background)
  val path1 = Path().apply {
    moveTo(0f, h * 0.55f)
    lineTo(w * 0.2f, h * 0.46f)
    lineTo(w * 0.38f, h * 0.52f)
    lineTo(w * 0.55f, h * 0.42f)
    lineTo(w * 0.75f, h * 0.50f)
    lineTo(w * 0.88f, h * 0.44f)
    lineTo(w, h * 0.52f)
    lineTo(w, h)
    lineTo(0f, h)
    close()
  }
  drawPath(path1, Color(0x772A084E))
  drawPath(path1, Color(0xFFA855F7), style = Stroke(width = 1.5f))

  // Mountain Layer 2 - foreground
  val path2 = Path().apply {
    moveTo(0f, h * 0.65f)
    lineTo(w * 0.15f, h * 0.58f)
    lineTo(w * 0.32f, h * 0.68f)
    lineTo(w * 0.5f, h * 0.54f)
    lineTo(w * 0.7f, h * 0.66f)
    lineTo(w * 0.85f, h * 0.57f)
    lineTo(w, h * 0.64f)
    lineTo(w, h)
    lineTo(0f, h)
    close()
  }
  drawPath(path2, Color(0xAA16052B))
  drawPath(path2, Color(0xFFF43F5E), style = Stroke(width = 2.0f))

  // Perspective Horizon Lines
  val groundY = h * 0.65f
  for (i in 1..10) {
    val curY = groundY + (h - groundY) * (i * i / 100f)
    drawLine(
      color = Color(0x44F43F5E),
      start = Offset(0f, curY),
      end = Offset(w, curY),
      strokeWidth = 1f
    )
  }

  // Converging perspective diagonals
  for (i in -4..14) {
    val startX = w * 0.5f + (i - 5) * 15f
    val endX = (i - 5) * (w * 0.15f) + w * 0.5f
    drawLine(
      color = Color(0x33A855F7),
      start = Offset(startX, groundY),
      end = Offset(endX, h),
      strokeWidth = 1f
    )
  }
}

// 03. Orbit Void — Dark concentric wireframe planetary orbits
private fun DrawScope.drawOrbitVoid() {
  drawRect(
    brush = Brush.radialGradient(
      colors = listOf(Color(0xFF0E121E), Color(0xFF05070C), Color(0xFF000000)),
      center = Offset(size.width * 0.5f, size.height * 0.45f),
      radius = size.width * 0.9f
    )
  )

  val center = Offset(size.width * 0.5f, size.height * 0.45f)
  val radii = listOf(
    size.width * 0.18f,
    size.width * 0.32f,
    size.width * 0.46f,
    size.width * 0.60f,
    size.width * 0.74f
  )

  radii.forEachIndexed { idx, r ->
    // Draw wireframe elliptical orbit
    drawOval(
      color = Color(0x4438BDF8),
      topLeft = Offset(center.x - r, center.y - r * 0.65f),
      size = Size(r * 2f, r * 1.3f),
      style = Stroke(width = if (idx % 2 == 0) 1.5f else 0.8f)
    )

    // Orbital Nodes (Satellites/Planets)
    val angle = (idx * 65.0 + 30.0) * (PI / 180.0)
    val nodeX = center.x + (r * cos(angle)).toFloat()
    val nodeY = center.y + (r * 0.65f * sin(angle)).toFloat()

    drawCircle(
      color = Color(0xFF38BDF8),
      radius = if (idx % 2 == 0) 5f else 3.5f,
      center = Offset(nodeX, nodeY)
    )
    drawCircle(
      color = Color.White,
      radius = 2f,
      center = Offset(nodeX, nodeY)
    )
  }

  // Central Star / Primary Nucleus
  drawCircle(color = Color(0x3300F2FE), radius = 24f, center = center)
  drawCircle(color = Color(0xFF00F2FE), radius = 10f, center = center)
  drawCircle(color = Color.White, radius = 4f, center = center)
}

// 04. Echo Lines — Low-poly audio waveform patterns
private fun DrawScope.drawEchoLines() {
  drawRect(
    brush = Brush.verticalGradient(
      listOf(Color(0xFF070E17), Color(0xFF0B1728), Color(0xFF04080F))
    )
  )

  val w = size.width
  val h = size.height
  val baseColors = listOf(
    Color(0xFF06B6D4),
    Color(0xFF10B981),
    Color(0xFF38BDF8),
    Color(0xFF818CF8),
    Color(0xFFF59E0B)
  )

  val lineCount = 9
  for (i in 0 until lineCount) {
    val centerY = h * 0.2f + (h * 0.6f) * (i.toFloat() / lineCount)
    val path = Path()
    val points = 16
    val amp = (28f + (i % 3) * 18f)

    for (p in 0..points) {
      val px = w * (p.toFloat() / points)
      val freq = (p * 0.8f + i * 1.3f)
      val offsetVal = sin(freq) * amp * if (p in 3..13) 1f else 0.3f
      val py = centerY + offsetVal

      if (p == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }

    val col = baseColors[i % baseColors.size]
    drawPath(
      path = path,
      color = col.copy(alpha = 0.65f),
      style = Stroke(width = if (i % 2 == 0) 2.2f else 1.2f)
    )
  }
}

// 05. Matrix Stream — Vertical technical code block strings
private fun DrawScope.drawMatrixStream() {
  drawRect(color = Color(0xFF020904))

  val colWidth = 24f
  val cols = (size.width / colWidth).toInt() + 1

  for (c in 0 until cols) {
    val x = c * colWidth
    val startY = ((c * 47) % size.height.toInt()).toFloat()
    val streamLength = 180f + ((c * 31) % 240)

    val streamBrush = Brush.verticalGradient(
      colors = listOf(
        Color(0x0000FF66),
        Color(0x5500FF66),
        Color(0xCC00FF66),
        Color(0xFFFFFFFF)
      ),
      startY = startY,
      endY = startY + streamLength
    )

    // Vertical dashed code stream
    var curY = startY
    while (curY < startY + streamLength && curY < size.height) {
      val blockH = 8f + ((c + curY.toInt()) % 10)
      drawLine(
        brush = streamBrush,
        start = Offset(x, curY),
        end = Offset(x, curY + blockH),
        strokeWidth = 2f
      )
      curY += blockH + 6f
    }

    // Glowing head glyph
    if (startY + streamLength < size.height) {
      drawCircle(
        color = Color(0xFFFFFFFF),
        radius = 2.5f,
        center = Offset(x, startY + streamLength)
      )
    }
  }
}

// 06. Sage Geometry — Earthy pale olive overlapping triangles
private fun DrawScope.drawSageGeometry() {
  drawRect(
    brush = Brush.verticalGradient(
      listOf(Color(0xFF131D16), Color(0xFF1C2B21), Color(0xFF0F1712))
    )
  )

  val w = size.width
  val h = size.height
  val triColors = listOf(
    Color(0x2284CC16),
    Color(0x334D7C5D),
    Color(0x22A3E635),
    Color(0x2865A30D),
    Color(0x1F22C55E)
  )

  for (i in 0..12) {
    val cx = (w * ((i * 37) % 100) / 100f)
    val cy = (h * ((i * 53) % 100) / 100f)
    val sz = w * 0.45f

    val path = Path().apply {
      moveTo(cx, cy - sz * 0.5f)
      lineTo(cx + sz * 0.5f, cy + sz * 0.5f)
      lineTo(cx - sz * 0.5f, cy + sz * 0.5f)
      close()
    }

    drawPath(path, triColors[i % triColors.size])
    drawPath(path, Color(0x6684CC16), style = Stroke(width = 1.2f))
  }
}

// 07. Obsidian Shards — Angular dark charcoal crystal planes
private fun DrawScope.drawObsidianShards() {
  drawRect(color = Color(0xFF0A0A0D))

  val w = size.width
  val h = size.height

  val shardPolygons = listOf(
    listOf(Offset(0f, 0f), Offset(w * 0.55f, 0f), Offset(w * 0.35f, h * 0.35f), Offset(0f, h * 0.25f)),
    listOf(Offset(w * 0.55f, 0f), Offset(w, 0f), Offset(w, h * 0.4f), Offset(w * 0.35f, h * 0.35f)),
    listOf(Offset(0f, h * 0.25f), Offset(w * 0.35f, h * 0.35f), Offset(w * 0.5f, h * 0.7f), Offset(0f, h * 0.65f)),
    listOf(Offset(w * 0.35f, h * 0.35f), Offset(w, h * 0.4f), Offset(w * 0.75f, h * 0.75f), Offset(w * 0.5f, h * 0.7f)),
    listOf(Offset(0f, h * 0.65f), Offset(w * 0.5f, h * 0.7f), Offset(w * 0.3f, h), Offset(0f, h)),
    listOf(Offset(w * 0.5f, h * 0.7f), Offset(w * 0.75f, h * 0.75f), Offset(w, h * 0.65f), Offset(w, h), Offset(w * 0.3f, h))
  )

  val fills = listOf(
    Color(0xFF14141A),
    Color(0xFF1C1C24),
    Color(0xFF111116),
    Color(0xFF22222E),
    Color(0xFF171720),
    Color(0xFF1E1E28)
  )

  shardPolygons.forEachIndexed { idx, points ->
    val path = Path().apply {
      moveTo(points[0].x, points[0].y)
      for (p in 1 until points.size) {
        lineTo(points[p].x, points[p].y)
      }
      close()
    }
    drawPath(path, fills[idx % fills.size])
    drawPath(path, Color(0x6671717A), style = Stroke(width = 1.2f))
  }
}

// 08. Copper Fuse — Industrial metallic circuit traces
private fun DrawScope.drawCopperFuse() {
  drawRect(
    brush = Brush.verticalGradient(
      listOf(Color(0xFF0F0B06), Color(0xFF17110A), Color(0xFF080503))
    )
  )

  val traceColor = Color(0xFFD97706)
  val padColor = Color(0xFFEA580C)
  val w = size.width
  val h = size.height

  val busLines = listOf(
    listOf(Offset(w * 0.1f, 0f), Offset(w * 0.1f, h * 0.25f), Offset(w * 0.35f, h * 0.4f), Offset(w * 0.35f, h)),
    listOf(Offset(w * 0.3f, 0f), Offset(w * 0.3f, h * 0.15f), Offset(w * 0.6f, h * 0.35f), Offset(w * 0.6f, h * 0.7f), Offset(w * 0.85f, h * 0.85f), Offset(w * 0.85f, h)),
    listOf(Offset(0f, h * 0.3f), Offset(w * 0.25f, h * 0.3f), Offset(w * 0.45f, h * 0.5f), Offset(w * 0.8f, h * 0.5f), Offset(w * 0.95f, h * 0.65f), Offset(w, h * 0.65f)),
    listOf(Offset(0f, h * 0.75f), Offset(w * 0.2f, h * 0.75f), Offset(w * 0.4f, h * 0.85f), Offset(w * 0.7f, h * 0.85f), Offset(w, h * 0.95f)),
    listOf(Offset(w * 0.7f, 0f), Offset(w * 0.7f, h * 0.2f), Offset(w * 0.9f, h * 0.35f), Offset(w * 0.9f, h))
  )

  busLines.forEach { linePoints ->
    val path = Path().apply {
      moveTo(linePoints[0].x, linePoints[0].y)
      for (p in 1 until linePoints.size) {
        lineTo(linePoints[p].x, linePoints[p].y)
      }
    }
    drawPath(path, traceColor.copy(alpha = 0.7f), style = Stroke(width = 2.5f))

    linePoints.forEach { pt ->
      drawCircle(padColor, radius = 4.5f, center = pt)
      drawCircle(Color(0xFFFDE68A), radius = 2f, center = pt)
    }
  }
}

// 09. Solar Corona — Minimalist abstract orange vector arches
private fun DrawScope.drawSolarCorona() {
  drawRect(
    brush = Brush.radialGradient(
      colors = listOf(Color(0xFF261002), Color(0xFF120600), Color(0xFF050200)),
      center = Offset(size.width * 0.5f, size.height * 0.35f),
      radius = size.width * 0.85f
    )
  )

  val center = Offset(size.width * 0.5f, size.height * 0.35f)
  val arches = 8

  for (i in 1..arches) {
    val r = size.width * (0.12f + i * 0.09f)
    val strokeColor = if (i % 2 == 0) Color(0xFFF97316) else Color(0xFFEA580C)
    drawCircle(
      color = strokeColor.copy(alpha = 0.25f + i * 0.06f),
      radius = r,
      center = center,
      style = Stroke(width = if (i == 3 || i == 6) 2.5f else 1.2f)
    )
  }

  // Radiating solar ray spokes
  val rays = 16
  for (i in 0 until rays) {
    val angle = (i * (360.0 / rays)) * (PI / 180.0)
    val startR = size.width * 0.15f
    val endR = size.width * 0.8f
    val p1 = Offset(center.x + (startR * cos(angle)).toFloat(), center.y + (startR * sin(angle)).toFloat())
    val p2 = Offset(center.x + (endR * cos(angle)).toFloat(), center.y + (endR * sin(angle)).toFloat())

    drawLine(
      color = Color(0x33FB923C),
      start = p1,
      end = p2,
      strokeWidth = 1f
    )
  }

  // Glowing solar nucleus
  drawCircle(Color(0xFFF97316), radius = size.width * 0.12f, center = center)
  drawCircle(Color(0xFFFDBA74), radius = size.width * 0.08f, center = center)
  drawCircle(Color.White, radius = size.width * 0.03f, center = center)
}

// 10. Frost Polygon — Crisp low-poly icy crystal mesh patterns
private fun DrawScope.drawFrostPolygon() {
  drawRect(
    brush = Brush.verticalGradient(
      listOf(Color(0xFF061524), Color(0xFF0D253A), Color(0xFF040D17))
    )
  )

  val w = size.width
  val h = size.height

  val cols = 4
  val rows = 8
  val grid = Array(rows + 1) { r ->
    Array(cols + 1) { c ->
      val jitterX = if (c in 1 until cols) ((r * 17 + c * 23) % 25 - 12f) else 0f
      val jitterY = if (r in 1 until rows) ((r * 31 + c * 13) % 25 - 12f) else 0f
      Offset(
        x = (c.toFloat() / cols) * w + jitterX,
        y = (r.toFloat() / rows) * h + jitterY
      )
    }
  }

  val iceFills = listOf(
    Color(0x3338BDF8),
    Color(0x220284C7),
    Color(0x447DD3FC),
    Color(0x1F0EA5E9),
    Color(0x2BBAE6FD)
  )

  for (r in 0 until rows) {
    for (c in 0 until cols) {
      val pTL = grid[r][c]
      val pTR = grid[r][c + 1]
      val pBL = grid[r + 1][c]
      val pBR = grid[r + 1][c + 1]

      // Triangle 1
      val t1 = Path().apply {
        moveTo(pTL.x, pTL.y)
        lineTo(pTR.x, pTR.y)
        lineTo(pBL.x, pBL.y)
        close()
      }
      drawPath(t1, iceFills[(r * cols + c) % iceFills.size])
      drawPath(t1, Color(0x6638BDF8), style = Stroke(width = 1f))

      // Triangle 2
      val t2 = Path().apply {
        moveTo(pTR.x, pTR.y)
        lineTo(pBR.x, pBR.y)
        lineTo(pBL.x, pBL.y)
        close()
      }
      drawPath(t2, iceFills[(r * cols + c + 2) % iceFills.size])
      drawPath(t2, Color(0x6638BDF8), style = Stroke(width = 1f))
    }
  }
}
