package com.jackattackk246.files.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jackattackk246.files.model.FileItem
import com.jackattackk246.files.ui.theme.AppThemeMode
import com.jackattackk246.files.ui.theme.ThemeManager
import com.jackattackk246.files.util.RecentFilesTracker
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecentFilesScreen(
  onFileSelected: (FileItem) -> Unit,
  themeMode: AppThemeMode = AppThemeMode.MIDNIGHT_MATTE_BLACK,
  customAccentColor: Color? = null
) {
  val recentFiles = RecentFilesTracker.getRecents()
  val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }

  val isLight = ThemeManager.isLightBackgroundProfile(themeMode)
  val primaryTextColor = ThemeManager.getAdaptivePrimaryTextColor(themeMode)
  val secondaryTextColor = ThemeManager.getAdaptiveSecondaryTextColor(themeMode)
  val accentColor = ThemeManager.getThemeAccentColor(themeMode, customAccentColor)
  val cardContainer = ThemeManager.getAdaptiveCardContainerColor(themeMode)
  val cardBorder = ThemeManager.getAdaptiveCardBorderColor(themeMode)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("recent_files_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.20f))
            .border(1.dp, accentColor.copy(alpha = 0.40f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.History, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = "Recent Files",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp,
              color = primaryTextColor
            )
          )
          Text(
            text = "${recentFiles.size} Recent Activity Records",
            style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor)
          )
        }
      }

      if (recentFiles.isNotEmpty()) {
        TextButton(
          onClick = {
            RecentFilesTracker.clear()
          },
          modifier = Modifier.testTag("clear_recent_history_button")
        ) {
          Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Clear", color = accentColor, fontWeight = FontWeight.Bold)
        }
      }
    }

    if (recentFiles.isEmpty()) {
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = secondaryTextColor.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
          )
          Text(
            text = "No Recent Activity Yet",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = primaryTextColor
            )
          )
          Text(
            text = "Files opened, edited, or modified will automatically appear here chronologically.",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = secondaryTextColor
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
          )
        }
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
      ) {
        items(
          items = recentFiles,
          key = { it.path + "_" + it.timestamp }
        ) { recent ->
          val file = File(recent.path)
          val fileItem = FileItem(file)

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .clickable { onFileSelected(fileItem) }
              .testTag("recent_file_row_${recent.name}"),
            colors = CardDefaults.cardColors(containerColor = cardContainer),
            border = BorderStroke(1.dp, cardBorder)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(accentColor.copy(alpha = 0.20f))
                  .border(1.dp, accentColor.copy(alpha = 0.40f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = when {
                    recent.isDirectory -> Icons.Default.Folder
                    fileItem.isMinecraftFile -> Icons.Default.SportsEsports
                    recent.path.endsWith(".zip") -> Icons.Default.FolderZip
                    else -> Icons.Default.InsertDriveFile
                  },
                  contentDescription = null,
                  tint = accentColor,
                  modifier = Modifier.size(22.dp)
                )
              }

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = recent.name,
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = recent.path,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = secondaryTextColor
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = dateFormat.format(Date(recent.timestamp)),
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = accentColor,
                    fontWeight = FontWeight.Medium
                  )
                )
              }

              Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = "Show Options",
                tint = secondaryTextColor,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }
    }
  }
}
