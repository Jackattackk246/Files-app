package com.jackattackk246.files.ui.dialog

import android.text.format.Formatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jackattackk246.files.model.FileItem
import com.jackattackk246.files.ui.theme.AppThemeMode
import com.jackattackk246.files.ui.theme.ThemeManager
import com.jackattackk246.files.util.FileManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QuickPreviewDialog(
  fileItem: FileItem,
  themeMode: AppThemeMode,
  customAccentColor: Color? = null,
  onDismiss: () -> Unit,
  onOpenWithSystemDefault: () -> Unit,
  onOpenManageDialog: () -> Unit
) {
  val context = LocalContext.current
  val accentColor = ThemeManager.getThemeAccentColor(themeMode, customAccentColor)
  val primaryTextColor = ThemeManager.getAdaptivePrimaryTextColor(themeMode)
  val secondaryTextColor = ThemeManager.getAdaptiveSecondaryTextColor(themeMode)
  val cardContainer = ThemeManager.getAdaptiveCardContainerColor(themeMode)
  val cardBorder = ThemeManager.getAdaptiveCardBorderColor(themeMode)

  val file = fileItem.file

  val lastModifiedStr = remember(fileItem) {
    try {
      SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(fileItem.lastModified))
    } catch (_: Exception) {
      "Unknown"
    }
  }

  val sizeFormatted = remember(fileItem) {
    try {
      if (file.isDirectory) "Directory" else Formatter.formatFileSize(context, fileItem.size)
    } catch (_: Exception) {
      fileItem.formattedSize
    }
  }

  // Permissions check
  val isReadable = file.canRead()
  val isWritable = file.canWrite()
  val isExecutable = file.canExecute()
  val permissionsString = buildString {
    append(if (isReadable) "Read (r)" else "-")
    append(" • ")
    append(if (isWritable) "Write (w)" else "-")
    append(" • ")
    append(if (isExecutable) "Execute (x)" else "-")
  }

  // Reading a preview snippet for standard offline text documents
  val textSnippet = remember(fileItem) {
    try {
      if (file.isFile && isReadable && fileItem.extension in listOf("txt", "md", "json", "xml", "csv", "log", "html", "css", "js", "kt", "java")) {
        file.bufferedReader().use { reader ->
          val buffer = CharArray(250)
          val readChars = reader.read(buffer)
          if (readChars > 0) {
            val content = String(buffer, 0, readChars).trim()
            if (file.length() > 250) "$content\n..." else content
          } else {
            "[Empty file]"
          }
        }
      } else {
        null
      }
    } catch (e: Exception) {
      "Error loading preview: ${e.message}"
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("quick_preview_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = cardContainer),
      border = BorderStroke(1.dp, cardBorder)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Header row with Icon and Name
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(accentColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = when {
                fileItem.isDirectory -> Icons.Default.Folder
                fileItem.isImage -> Icons.Default.Image
                fileItem.isDocument -> Icons.Default.Description
                fileItem.isAudio -> Icons.Default.Audiotrack
                fileItem.isVideo -> Icons.Default.VideoLibrary
                fileItem.extension == "zip" -> Icons.Default.FolderZip
                else -> Icons.Default.InsertDriveFile
              },
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(24.dp)
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = fileItem.name,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
              ),
              color = primaryTextColor,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = if (fileItem.isDirectory) "System Directory" else "File Extension: .${fileItem.extension.uppercase()}",
              style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor)
            )
          }
        }

        Divider(color = cardBorder, thickness = 1.dp)

        // Thumbnail / Content Preview block
        when {
          fileItem.isImage -> {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black.copy(alpha = 0.1f))
                .border(1.dp, cardBorder, RoundedCornerShape(14.dp)),
              contentAlignment = Alignment.Center
            ) {
              AsyncImage(
                model = ImageRequest.Builder(context)
                  .data(file)
                  .crossfade(true)
                  .build(),
                contentDescription = fileItem.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                  .fillMaxSize()
                  .clip(RoundedCornerShape(14.dp))
                  .testTag("quick_preview_image")
              )
            }
          }

          textSnippet != null -> {
            Column(
              verticalArrangement = Arrangement.spacedBy(4.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "File Snippet Preview",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = accentColor
                )
              )
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(min = 60.dp, max = 150.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color.Black.copy(alpha = 0.2f))
                  .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                  .padding(10.dp)
              ) {
                Text(
                  text = textSnippet,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = primaryTextColor,
                    fontSize = 12.sp
                  ),
                  maxLines = 6,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }

          fileItem.isDocument && !fileItem.isImage -> {
            // Document non-text thumbnail
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accentColor.copy(alpha = 0.05f))
                .border(1.dp, cardBorder, RoundedCornerShape(14.dp)),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Description,
                  contentDescription = null,
                  tint = accentColor,
                  modifier = Modifier.size(32.dp)
                )
                Text(
                  text = "Document File (No Snippet)",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = primaryTextColor
                )
              }
            }
          }
        }

        // Metadata grid / information
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          MetadataItemRow(label = "File Size", value = sizeFormatted, secondaryColor = secondaryTextColor, primaryColor = primaryTextColor)
          MetadataItemRow(label = "Last Modified", value = lastModifiedStr, secondaryColor = secondaryTextColor, primaryColor = primaryTextColor)
          MetadataItemRow(label = "Permissions", value = permissionsString, secondaryColor = secondaryTextColor, primaryColor = primaryTextColor)
          MetadataItemRow(
            label = "Path Location",
            value = fileItem.path,
            secondaryColor = secondaryTextColor,
            primaryColor = primaryTextColor,
            isPath = true
          )
        }

        Divider(color = cardBorder, thickness = 1.dp)

        // Action controls
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = {
              onDismiss()
              onOpenManageDialog()
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, cardBorder)
          ) {
            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp), tint = primaryTextColor)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Manage", color = primaryTextColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }

          Button(
            onClick = {
              onDismiss()
              onOpenWithSystemDefault()
            },
            modifier = Modifier.weight(1.2f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
          ) {
            Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Open File", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun MetadataItemRow(
  label: String,
  value: String,
  primaryColor: Color,
  secondaryColor: Color,
  isPath: Boolean = false
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
      color = secondaryColor,
      modifier = Modifier.weight(1f)
    )
    Text(
      text = value,
      style = if (isPath) MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp) else MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
      color = primaryColor,
      modifier = Modifier.weight(2f),
      maxLines = if (isPath) 3 else 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}
