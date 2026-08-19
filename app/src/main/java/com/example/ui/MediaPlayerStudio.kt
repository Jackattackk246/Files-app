package com.example.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun MediaPlayerStudio(
    mediaFile: com.jackattackk246.files.model.FileItem,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val isAudioOnly = mediaFile.extension.lowercase() in listOf("mp3", "wav", "flac", "ogg", "m4a", "aac")

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }

    DisposableEffect(mediaFile) {
        val player = ExoPlayer.Builder(context).build().apply {
            val uri = if (mediaFile.customStreamUrl != null) {
                Uri.parse(mediaFile.customStreamUrl)
            } else {
                Uri.fromFile(mediaFile.file)
            }
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlayingChanged: Boolean) {
                    isPlaying = isPlayingChanged
                }
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        duration = this@apply.duration.coerceAtLeast(0L)
                    }
                }
            })
        }
        exoPlayer = player

        onDispose {
            player.release()
            exoPlayer = null
        }
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            exoPlayer?.let {
                if (it.isPlaying) {
                    currentPosition = it.currentPosition
                }
            }
            delay(100L)
        }
    }

    val formatTime = { timeMs: Long ->
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        String.format("%02d:%02d", minutes, seconds)
    }

    // Colors according to prompt
    val bgCharcoal = Color(0xFF1C1D22)
    val borderCharcoal = Color(0xFF2C2D35)
    val accentCyan = Color(0xFF00E5FF)
    val translucentBg = Color.Black.copy(alpha = 0.4f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(translucentBg)
            
            .clickable(enabled = false) {} // block clicks
    ) {
        // We use another Box on top of the blur without the blur applied to its children
        // Wait, Modifier.blur blurs the content OF the composable, not just the background.
        // To blur the background behind it, we'd need to use a RenderEffect or similar on Android 12+,
        // or just blur a background layer. We will blur a background layer.
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(16.dp)
                    .background(Color(0xFF1C1D22).copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                    .border(1.dp, borderCharcoal, RoundedCornerShape(24.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Top Layer: Filename and Close Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mediaFile.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    maxLines = 1,
                    modifier = Modifier.weight(1f).padding(end = 16.dp)
                )
                
                Surface(
                    shape = CircleShape,
                    color = borderCharcoal,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable {
                            com.jackattackk246.files.util.HapticFeedbackHelper.performToggleFeedback(context)
                            onClose()
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = accentCyan,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            // Center Layer: ExoPlayer Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (isAudioOnly) {
                    // Spinning electric-cyan disc vector asset for audio
                    Icon(
                        imageVector = Icons.Default.PlayArrow, // placeholder for a disc if needed, or just a stylized note
                        contentDescription = "Audio Playback",
                        tint = accentCyan,
                        modifier = Modifier.size(64.dp)
                    )
                } else {
                    exoPlayer?.let { player ->
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    this.player = player
                                    useController = false
                                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Bottom Layer: Custom Playback Mechanics
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(currentPosition),
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = formatTime(duration),
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Medium)
                    )
                }

                Slider(
                    value = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()) else 0f,
                    onValueChange = { percent ->
                        val target = (percent * duration).toLong()
                        currentPosition = target
                        exoPlayer?.seekTo(target)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = accentCyan,
                        activeTrackColor = accentCyan,
                        inactiveTrackColor = borderCharcoal
                    ),
                    modifier = Modifier.fillMaxWidth().height(24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            com.jackattackk246.files.util.HapticFeedbackHelper.performToggleFeedback(context)
                            exoPlayer?.seekTo((currentPosition - 10000).coerceAtLeast(0))
                        }
                    ) {
                        Icon(Icons.Default.FastRewind, contentDescription = "Skip Backward 10s", tint = accentCyan, modifier = Modifier.size(32.dp))
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    Surface(
                        shape = CircleShape,
                        color = accentCyan.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, accentCyan),
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .clickable {
                                com.jackattackk246.files.util.HapticFeedbackHelper.performToggleFeedback(context)
                                exoPlayer?.let {
                                    if (it.isPlaying) it.pause() else it.play()
                                }
                            }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = accentCyan,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    IconButton(
                        onClick = {
                            com.jackattackk246.files.util.HapticFeedbackHelper.performToggleFeedback(context)
                            exoPlayer?.seekTo((currentPosition + 10000).coerceAtMost(duration))
                        }
                    ) {
                        Icon(Icons.Default.FastForward, contentDescription = "Skip Forward 10s", tint = accentCyan, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}
}
