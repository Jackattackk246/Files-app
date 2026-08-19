package com.jackattackk246.files.ui

import android.content.Intent
import android.os.Environment
import android.os.StatFs
import android.text.format.Formatter
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.jackattackk246.files.util.FileManager
import com.jackattackk246.files.util.RecycleBinEngine
import com.jackattackk246.files.util.UserProfilePreferences
import java.io.File

@Composable
fun WearDashboardScreen() {
    val context = LocalContext.current
    var totalSpace by remember { mutableStateOf(0L) }
    var freeSpace by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        try {
            val stat = StatFs(Environment.getExternalStorageDirectory().path)
            totalSpace = stat.totalBytes
            freeSpace = stat.availableBytes
        } catch (e: Exception) {
            totalSpace = 1L
            freeSpace = 1L
        }
    }

    val dynamicGreeting = remember {
        UserProfilePreferences.getDynamicTimeGreeting(context)
    }

    val totalStr = Formatter.formatFileSize(context, totalSpace)
    val freeStr = Formatter.formatFileSize(context, freeSpace)
    val usedBytes = (totalSpace - freeSpace).coerceAtLeast(0L)
    val usedPercent = if (totalSpace > 0L) (usedBytes.toFloat() / totalSpace.toFloat() * 100).toInt() else 0

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 28.dp, bottom = 28.dp, start = 10.dp, end = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. TOP PROFILE & STORAGE PROGRESS
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = dynamicGreeting,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Storage: $freeStr free ($usedPercent% used)",
                    fontSize = 11.sp,
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 2. LOCAL STORAGE HUBS (Download / Main Storage / Recycle Bin)
        item {
            Chip(
                onClick = {
                    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    Toast.makeText(context, "Downloads: ${dir.listFiles()?.size ?: 0} files", Toast.LENGTH_SHORT).show()
                },
                label = { Text("Download Folder", fontSize = 12.sp) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Downloads",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(20.dp)
                    )
                },
                colors = ChipDefaults.secondaryChipColors(),
                modifier = Modifier.fillMaxWidth(0.92f)
            )
        }

        item {
            Chip(
                onClick = {
                    val dir = FileManager.getRootDirectory()
                    Toast.makeText(context, "Main Storage: ${dir.listFiles()?.size ?: 0} files", Toast.LENGTH_SHORT).show()
                },
                label = { Text("Main Storage", fontSize = 12.sp) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Main Storage",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                },
                colors = ChipDefaults.secondaryChipColors(),
                modifier = Modifier.fillMaxWidth(0.92f)
            )
        }

        item {
            Chip(
                onClick = {
                    val trashCount = RecycleBinEngine.getItemCount()
                    Toast.makeText(context, "Recycle Bin: $trashCount items", Toast.LENGTH_SHORT).show()
                },
                label = { Text("Recycle Bin", fontSize = 12.sp) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Recycle Bin",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                },
                colors = ChipDefaults.secondaryChipColors(),
                modifier = Modifier.fillMaxWidth(0.92f)
            )
        }

        // 3. MEDIA HUBS SECTION
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Media Hubs",
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Bold
            )
        }

        val mediaItems = listOf(
            Triple("Images", Icons.Default.Image, Color(0xFF10B981)),
            Triple("Audio", Icons.Default.MusicNote, Color(0xFF3B82F6)),
            Triple("Videos", Icons.Default.Videocam, Color(0xFFF59E0B)),
            Triple("APK Packages", Icons.Default.Android, Color(0xFF8B5CF6)),
            Triple("Documents", Icons.Default.Description, Color(0xFFEC4899))
        )

        items(mediaItems.size) { i ->
            val media = mediaItems[i]
            Chip(
                onClick = {
                    Toast.makeText(context, "${media.first} Category", Toast.LENGTH_SHORT).show()
                },
                label = { Text(media.first, fontSize = 12.sp) },
                icon = {
                    Icon(
                        imageVector = media.second,
                        contentDescription = media.first,
                        tint = media.third,
                        modifier = Modifier.size(20.dp)
                    )
                },
                colors = ChipDefaults.secondaryChipColors(),
                modifier = Modifier.fillMaxWidth(0.92f)
            )
        }

        // 4. QUICK FILE ACTIONS
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Quick File Actions",
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Chip(
                onClick = {
                    FileManager.openPathSAFBackdoor(context, Environment.getExternalStorageDirectory().absolutePath)
                },
                label = { Text("Open Document Picker", fontSize = 11.sp) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.FileOpen,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                },
                colors = ChipDefaults.primaryChipColors(),
                modifier = Modifier.fillMaxWidth(0.92f)
            )
        }

        item {
            Chip(
                onClick = {
                    Toast.makeText(context, "Root: /storage/emulated/0", Toast.LENGTH_SHORT).show()
                },
                label = { Text("Open Root Directory", fontSize = 11.sp) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                },
                colors = ChipDefaults.primaryChipColors(),
                modifier = Modifier.fillMaxWidth(0.92f)
            )
        }
    }
}
