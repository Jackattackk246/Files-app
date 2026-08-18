package com.jackattackk246.files.ui.viewer

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.jackattackk246.files.model.FileItem
import com.jackattackk246.files.ui.theme.AppThemeMode
import com.jackattackk246.files.ui.theme.ThemeManager
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun NativeMediaPlayerDialog(
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
  val cardContainer = if (isLight) Color(0xFFFFFFFF) else Color(0xFF14171F)
  val cardBorder = if (isLight) Color(0x33000000) else Color(0x3338BDF8)

  var isPlaying by remember { mutableStateOf(false) }
  var currentPositionMs by remember { mutableStateOf(0) }
  var durationMs by remember { mutableStateOf(1) }
  var isLooping by remember { mutableStateOf(false) }
  var isMuted by remember { mutableStateOf(false) }

  val mediaPlayer = remember {
    MediaPlayer().apply {
      try {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        )
        setDataSource(fileItem.path)
        prepare()
        durationMs = duration.coerceAtLeast(1)
        start()
        isPlaying = true
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      try {
        if (mediaPlayer.isPlaying) {
          mediaPlayer.stop()
        }
        mediaPlayer.release()
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  LaunchedEffect(isPlaying) {
    while (isPlaying) {
      try {
        currentPositionMs = mediaPlayer.currentPosition
        if (currentPositionMs >= durationMs && !isLooping) {
          isPlaying = false
        }
      } catch (e: Exception) {
        // Ignored
      }
      delay(300)
    }
  }

  fun formatTime(ms: Int): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%02d:%02d".format(min, sec)
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("native_media_player_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = cardContainer),
      border = BorderStroke(1.dp, cardBorder)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Header Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = accentColor)
            Text(
              "Built-In Audio Player",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
              )
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = secondaryTextColor)
          }
        }

        // Album Art Box (Geometric Stylized Vinyl / Waveform)
        Box(
          modifier = Modifier
            .size(160.dp)
            .clip(CircleShape)
            .background(
              Brush.radialGradient(
                colors = listOf(accentColor.copy(alpha = 0.4f), Color(0xFF0F172A))
              )
            )
            .border(2.dp, accentColor, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(64.dp)
          )
        }

        // Track Info
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = fileItem.name,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = primaryTextColor
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
          )
          Text(
            text = "${fileItem.formattedSize} • ${fileItem.extension.uppercase()}",
            style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor)
          )
        }

        // Timeline Slider
        Column(modifier = Modifier.fillMaxWidth()) {
          Slider(
            value = currentPositionMs.toFloat().coerceIn(0f, durationMs.toFloat()),
            onValueChange = { newPos ->
              currentPositionMs = newPos.toInt()
              mediaPlayer.seekTo(currentPositionMs)
            },
            valueRange = 0f..durationMs.toFloat(),
            colors = SliderDefaults.colors(
              thumbColor = accentColor,
              activeTrackColor = accentColor,
              inactiveTrackColor = accentColor.copy(alpha = 0.25f)
            ),
            modifier = Modifier.fillMaxWidth().testTag("audio_timeline_slider")
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(formatTime(currentPositionMs), style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor))
            Text(formatTime(durationMs), style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor))
          }
        }

        // Playback Controls
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = {
              isLooping = !isLooping
              mediaPlayer.isLooping = isLooping
            }
          ) {
            Icon(
              Icons.Default.Repeat,
              contentDescription = "Loop",
              tint = if (isLooping) accentColor else secondaryTextColor
            )
          }

          IconButton(
            onClick = {
              val newPos = (mediaPlayer.currentPosition - 10000).coerceAtLeast(0)
              mediaPlayer.seekTo(newPos)
              currentPositionMs = newPos
            }
          ) {
            Icon(Icons.Default.Replay10, contentDescription = "Rewind 10s", tint = primaryTextColor)
          }

          // Main Play / Pause Button
          IconButton(
            onClick = {
              if (isPlaying) {
                mediaPlayer.pause()
                isPlaying = false
              } else {
                mediaPlayer.start()
                isPlaying = true
              }
            },
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(accentColor)
              .testTag("audio_play_pause_button")
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (isPlaying) "Pause" else "Play",
              tint = Color.Black,
              modifier = Modifier.size(32.dp)
            )
          }

          IconButton(
            onClick = {
              val newPos = (mediaPlayer.currentPosition + 10000).coerceAtMost(durationMs)
              mediaPlayer.seekTo(newPos)
              currentPositionMs = newPos
            }
          ) {
            Icon(Icons.Default.Forward10, contentDescription = "Forward 10s", tint = primaryTextColor)
          }

          IconButton(
            onClick = {
              isMuted = !isMuted
              val vol = if (isMuted) 0f else 1f
              mediaPlayer.setVolume(vol, vol)
            }
          ) {
            Icon(
              imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
              contentDescription = "Mute",
              tint = if (isMuted) accentColor else secondaryTextColor
            )
          }
        }
      }
    }
  }
}
