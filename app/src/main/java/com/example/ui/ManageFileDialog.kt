package com.jackattackk246.files.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jackattackk246.files.model.FileItem
import com.jackattackk246.files.util.FileManager

@Composable
fun ManageFileDialog(
  fileItem: FileItem,
  isFromRecentsTab: Boolean,
  onDismiss: () -> Unit,
  onShowInFolder: (FileItem) -> Unit,
  onRenameRequest: (FileItem) -> Unit,
  onCopyRequest: (FileItem) -> Unit,
  onMoveRequest: (FileItem) -> Unit,
  onDeleteRequest: (FileItem) -> Unit,
  onZipRequest: (FileItem) -> Unit,
  onUnzipRequest: (FileItem) -> Unit,
  onAnalyticsRequest: (FileItem) -> Unit
) {
  val context = LocalContext.current

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("manage_file_dialog"),
    shape = RoundedCornerShape(24.dp),
    containerColor = MaterialTheme.colorScheme.surface,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .background(
              color = MaterialTheme.colorScheme.primaryContainer,
              shape = RoundedCornerShape(12.dp)
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = when {
              fileItem.isDirectory -> Icons.Default.Folder
              fileItem.isMinecraftFile -> Icons.Default.SportsEsports
              fileItem.extension == "zip" -> Icons.Default.FolderZip
              else -> Icons.Default.InsertDriveFile
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = fileItem.name,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = if (fileItem.isDirectory) "Directory" else "${fileItem.formattedSize} • ${fileItem.extension.uppercase()}",
            style = MaterialTheme.typography.bodySmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // CONDITIONAL 'Show in Folder' Shortcut (STRICTLY for Recents View)
        if (isFromRecentsTab) {
          Button(
            onClick = {
              onDismiss()
              onShowInFolder(fileItem)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("show_in_folder_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Show in Folder", fontWeight = FontWeight.Bold)
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }

        // Open with System Default
        ManageMenuRowButton(
          title = "Open with System Default",
          subtitle = if (fileItem.isMinecraftFile) "Targets Mojang Minecraft (com.mojang.minecraftpe)" else "System Intent Chooser",
          icon = Icons.Default.Launch,
          onClick = {
            onDismiss()
            FileManager.openWithSystemDefault(context, fileItem.file)
          }
        )

        // Open Path SAF System Backdoor
        ManageMenuRowButton(
          title = "Open Path in System Files",
          subtitle = "Launch DocumentsUI (com.google.android.documentsui) in parent path",
          icon = Icons.Default.OpenInNew,
          onClick = {
            onDismiss()
            val parentPath = fileItem.file.parent ?: "/sdcard"
            FileManager.openPathSAFBackdoor(context, parentPath)
          }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Core Operations (Rename, Copy, Move, Delete)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          ActionChipButton(
            text = "Rename",
            icon = Icons.Default.Edit,
            modifier = Modifier.weight(1f),
            onClick = {
              onDismiss()
              onRenameRequest(fileItem)
            }
          )
          ActionChipButton(
            text = "Copy",
            icon = Icons.Default.ContentCopy,
            modifier = Modifier.weight(1f),
            onClick = {
              onDismiss()
              onCopyRequest(fileItem)
            }
          )
          ActionChipButton(
            text = "Move",
            icon = Icons.Default.DriveFileMove,
            modifier = Modifier.weight(1f),
            onClick = {
              onDismiss()
              onMoveRequest(fileItem)
            }
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          ActionChipButton(
            text = "Delete",
            icon = Icons.Default.Delete,
            isDanger = true,
            modifier = Modifier.weight(1f),
            onClick = {
              onDismiss()
              onDeleteRequest(fileItem)
            }
          )

          if (fileItem.extension == "zip") {
            ActionChipButton(
              text = "Extract Zip",
              icon = Icons.Default.FolderZip,
              modifier = Modifier.weight(1f),
              onClick = {
                onDismiss()
                onUnzipRequest(fileItem)
              }
            )
          } else {
            ActionChipButton(
              text = "Compress",
              icon = Icons.Default.Archive,
              modifier = Modifier.weight(1f),
              onClick = {
                onDismiss()
                onZipRequest(fileItem)
              }
            )
          }

          ActionChipButton(
            text = "Details",
            icon = Icons.Default.Analytics,
            modifier = Modifier.weight(1f),
            onClick = {
              onDismiss()
              onAnalyticsRequest(fileItem)
            }
          )
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}

@Composable
private fun ManageMenuRowButton(
  title: String,
  subtitle: String,
  icon: ImageVector,
  onClick: () -> Unit
) {
  OutlinedButton(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
        Text(subtitle, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
      }
    }
  }
}

@Composable
private fun ActionChipButton(
  text: String,
  icon: ImageVector,
  modifier: Modifier = Modifier,
  isDanger: Boolean = false,
  onClick: () -> Unit
) {
  Button(
    onClick = onClick,
    modifier = modifier,
    shape = RoundedCornerShape(10.dp),
    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
    colors = if (isDanger) {
      ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
      )
    } else {
      ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
      Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
  }
}
