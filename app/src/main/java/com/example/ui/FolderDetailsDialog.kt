package com.jackattackk246.files.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jackattackk246.files.model.FileItem
import com.jackattackk246.files.model.FolderAnalytics
import com.jackattackk246.files.util.FileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FolderDetailsDialog(
  fileItem: FileItem,
  onDismiss: () -> Unit
) {
  var analytics by remember { mutableStateOf<FolderAnalytics?>(null) }
  var isLoading by remember { mutableStateOf(true) }

  LaunchedEffect(fileItem) {
    isLoading = true
    val result = withContext(Dispatchers.IO) {
      FileManager.analyzeFolder(fileItem.file)
    }
    analytics = result
    isLoading = false
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(20.dp),
    icon = {
      Icon(
        imageVector = Icons.Default.Analytics,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary
      )
    },
    title = {
      Text(
        text = if (fileItem.isDirectory) "Folder Analytics" else "File Properties",
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text("Name: ${fileItem.name}", fontWeight = FontWeight.SemiBold)
        Text("Path: ${fileItem.path}", style = MaterialTheme.typography.bodySmall)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        if (isLoading) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
            Text("Calculating total byte weight and item count...", style = MaterialTheme.typography.bodyMedium)
          }
        } else if (analytics != null) {
          val data = analytics!!
          DetailRow("Total Size:", data.formattedTotalSize)
          DetailRow("Nested Files:", "${data.totalFilesCount} files")
          if (fileItem.isDirectory) {
            DetailRow("Nested Folders:", "${data.totalFoldersCount} subfolders")
          }
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
private fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
  }
}
