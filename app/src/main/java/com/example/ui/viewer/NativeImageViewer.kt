package com.jackattackk246.files.ui.viewer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jackattackk246.files.model.FileItem
import com.jackattackk246.files.ui.theme.AppThemeMode
import com.jackattackk246.files.ui.theme.ThemeManager
import java.io.File

@Composable
fun NativeImageViewerDialog(
  fileItem: FileItem,
  themeMode: AppThemeMode,
  customAccentColor: Color?,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val accentColor = ThemeManager.getThemeAccentColor(themeMode, customAccentColor)
  val isLight = ThemeManager.isLightBackgroundProfile(themeMode)
  val primaryTextColor = ThemeManager.getAdaptivePrimaryTextColor(themeMode)
  val secondaryTextColor = ThemeManager.getAdaptiveSecondaryTextColor(themeMode)

  var scale by remember { mutableStateOf(1f) }
  var offsetX by remember { mutableStateOf(0f) }
  var offsetY by remember { mutableStateOf(0f) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.95f))
        .testTag("native_image_viewer_dialog")
    ) {
      // Image Content with Pan / Zoom
      Box(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
              scale = (scale * zoom).coerceIn(0.8f, 5.0f)
              offsetX += pan.x
              offsetY += pan.y
            }
          },
        contentAlignment = Alignment.Center
      ) {
        AsyncImage(
          model = ImageRequest.Builder(context)
            .data(fileItem.file)
            .crossfade(true)
            .build(),
          contentDescription = fileItem.name,
          contentScale = ContentScale.Fit,
          modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
              scaleX = scale,
              scaleY = scale,
              translationX = offsetX,
              translationY = offsetY
            )
        )
      }

      // Top Header Overlay Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.TopCenter)
          .background(Color(0x99000000))
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = fileItem.name,
            style = MaterialTheme.typography.titleMedium.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "${fileItem.formattedSize} • ${fileItem.extension.uppercase()}",
            style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray)
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          IconButton(
            onClick = {
              scale = 1f
              offsetX = 0f
              offsetY = 0f
            }
          ) {
            Icon(Icons.Default.FitScreen, contentDescription = "Reset Zoom", tint = accentColor)
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
          }
        }
      }
    }
  }
}
