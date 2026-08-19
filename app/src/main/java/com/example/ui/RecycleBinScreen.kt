package com.jackattackk246.files.ui

import android.text.format.Formatter
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jackattackk246.files.model.EnvironmentalSeason
import com.jackattackk246.files.ui.theme.AppThemeMode
import com.jackattackk246.files.ui.theme.ThemeManager
import com.jackattackk246.files.util.HapticFeedbackHelper
import com.jackattackk246.files.util.HapticManager
import com.jackattackk246.files.util.RecycleBinEngine
import com.jackattackk246.files.util.RecycledFileRecord
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecycleBinScreen(
  themeMode: AppThemeMode = AppThemeMode.MIDNIGHT_MATTE_BLACK,
  customAccentColor: Color? = null,
  season: EnvironmentalSeason = EnvironmentalSeason.AUTO,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var refreshCounter by remember { mutableStateOf(0) }

  val recycledRecords = remember(refreshCounter) {
    RecycleBinEngine.getRecycledItems()
  }

  val totalWastedBytes = remember(recycledRecords) {
    recycledRecords.sumOf { it.fileSize }
  }

  val itemCount = recycledRecords.size

  val isLight = ThemeManager.isLightBackgroundProfile(themeMode, season)
  val primaryTextColor = ThemeManager.getAdaptivePrimaryTextColor(themeMode, season)
  val secondaryTextColor = ThemeManager.getAdaptiveSecondaryTextColor(themeMode, season)
  val accentColor = ThemeManager.getThemeAccentColor(themeMode, customAccentColor)
  val cardContainer = ThemeManager.getAdaptiveCardContainerColor(themeMode, season)
  val cardBorder = ThemeManager.getAdaptiveCardBorderColor(themeMode, season)

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("recycle_bin_screen")
  ) {
    // 1. Top Utility Metrics Banner Row (Isolated System Utility Hub - No File Folder Breadcrumb)
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .testTag("recycle_bin_metrics_banner"),
      color = cardContainer,
      shape = RoundedCornerShape(16.dp),
      border = BorderStroke(1.dp, cardBorder)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Total Deleted Items",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Medium,
              color = secondaryTextColor
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          val spaceText = Formatter.formatFileSize(context, totalWastedBytes)
          Text(
            text = "$itemCount Items | $spaceText Wasted",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = primaryTextColor
            )
          )
        }

        // HIDE Empty Bin Button when bin is empty (count == 0)
        // Enforce active theme primary/accent color token when visible
        if (itemCount > 0) {
          Spacer(modifier = Modifier.width(12.dp))

          Button(
            onClick = {
              HapticManager.errorPulse(context)
              val success = RecycleBinEngine.emptyRecycleBin()
              if (success) {
                Toast.makeText(context, "Recycle Bin Emptied", Toast.LENGTH_SHORT).show()
                refreshCounter++
              } else {
                Toast.makeText(context, "Recycle Bin is already empty", Toast.LENGTH_SHORT).show()
              }
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = accentColor,
              contentColor = if (isLight) Color.White else Color.Black
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            modifier = Modifier
              .defaultMinSize(minHeight = 40.dp)
              .testTag("empty_recycle_bin_button")
          ) {
            Icon(
              imageVector = Icons.Default.DeleteSweep,
              contentDescription = "Empty Recycle Bin",
              tint = if (isLight) Color.White else Color.Black,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Empty Bin",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = if (isLight) Color.White else Color.Black
            )
          }
        }
      }
    }

    // 2. Main Content: Stylized Empty State OR Deleted Items List
    if (itemCount == 0) {
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(32.dp)
          .testTag("recycle_bin_empty_state"),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Box(
            modifier = Modifier
              .size(80.dp)
              .clip(CircleShape)
              .background(accentColor.copy(alpha = 0.12f))
              .border(1.dp, accentColor.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Empty Trash",
              tint = accentColor,
              modifier = Modifier.size(42.dp)
            )
          }
          Text(
            text = "Recycle Bin is empty",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = primaryTextColor
            )
          )
          Text(
            text = "Files and folders moved to the Recycle Bin will safely appear here and can be restored or erased permanently.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = secondaryTextColor,
              textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 28.dp)
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .testTag("recycle_bin_items_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(
          items = recycledRecords,
          key = { it.id }
        ) { record ->
          val formattedDate = remember(record.deletedTimestamp) {
            SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(record.deletedTimestamp))
          }
          val formattedSize = remember(record.fileSize) {
            Formatter.formatFileSize(context, record.fileSize)
          }

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .testTag("recycled_card_${record.id}"),
            colors = CardDefaults.cardColors(containerColor = cardContainer),
            border = BorderStroke(1.dp, cardBorder)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              // Left: Adaptive File Type Thumbnail Icon
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(accentColor.copy(alpha = 0.16f))
                  .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (record.isDirectory) {
                    Icons.Default.Folder
                  } else {
                    when (File(record.fileName).extension.lowercase()) {
                      "jpg", "jpeg", "png", "webp", "gif" -> Icons.Default.Image
                      "mp4", "mkv", "mov", "webm" -> Icons.Default.Videocam
                      "mp3", "wav", "flac", "ogg" -> Icons.Default.Audiotrack
                      "pdf", "doc", "docx", "txt" -> Icons.Default.Description
                      "zip", "rar", "tar", "gz" -> Icons.Default.FolderZip
                      "apk" -> Icons.Default.Android
                      else -> Icons.Default.InsertDriveFile
                    }
                  },
                  contentDescription = null,
                  tint = accentColor,
                  modifier = Modifier.size(24.dp)
                )
              }

              // Center: Original Name, Original Location Path, and Deleted Timestamp
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = record.fileName,
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "From: ${record.originalPath}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = secondaryTextColor
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "Deleted: $formattedDate • $formattedSize",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = secondaryTextColor.copy(alpha = 0.75f)
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }

              // Right: Dedicated Restore & Delete Permanently Action Buttons
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                // Restore Button
                IconButton(
                  onClick = {
                    val success = RecycleBinEngine.restoreItem(record.id)
                    if (success) {
                      HapticFeedbackHelper.performTransferSuccessFeedback(context)
                      Toast.makeText(context, "Restored: ${record.fileName}", Toast.LENGTH_SHORT).show()
                      refreshCounter++
                    } else {
                      Toast.makeText(context, "Failed to restore item", Toast.LENGTH_SHORT).show()
                    }
                  },
                  modifier = Modifier
                    .size(40.dp)
                    .testTag("restore_button_${record.id}")
                ) {
                  Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = "Restore File",
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                  )
                }

                // Delete Permanently Button
                IconButton(
                  onClick = {
                    val success = RecycleBinEngine.deletePermanently(record.id)
                    if (success) {
                      HapticManager.errorPulse(context)
                      Toast.makeText(context, "Permanently deleted: ${record.fileName}", Toast.LENGTH_SHORT).show()
                      refreshCounter++
                    } else {
                      Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show()
                    }
                  },
                  modifier = Modifier
                    .size(40.dp)
                    .testTag("delete_permanently_button_${record.id}")
                ) {
                  Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Delete Permanently",
                    tint = secondaryTextColor,
                    modifier = Modifier.size(22.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
