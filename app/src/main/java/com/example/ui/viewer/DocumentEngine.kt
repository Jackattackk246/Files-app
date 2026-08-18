package com.jackattackk246.files.ui.viewer

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jackattackk246.files.model.FileItem
import com.jackattackk246.files.ui.theme.AppThemeMode
import com.jackattackk246.files.ui.theme.ThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun DocumentEngineDialog(
  fileItem: FileItem,
  themeMode: AppThemeMode,
  customAccentColor: Color?,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val accentColor = ThemeManager.getThemeAccentColor(themeMode, customAccentColor)
  val isLight = ThemeManager.isLightBackgroundProfile(themeMode)
  val primaryTextColor = ThemeManager.getAdaptivePrimaryTextColor(themeMode)
  val secondaryTextColor = ThemeManager.getAdaptiveSecondaryTextColor(themeMode)
  val cardContainer = if (isLight) Color(0xFFF8FAFC) else Color(0xFF0F172A)
  val cardBorder = if (isLight) Color(0x33000000) else Color(0x3338BDF8)

  var textContent by remember { mutableStateOf("") }
  var isLoading by remember { mutableStateOf(true) }
  var isEditing by remember { mutableStateOf(false) }
  var lineCount by remember { mutableStateOf(0) }
  var isSaved by remember { mutableStateOf(false) }

  LaunchedEffect(fileItem) {
    isLoading = true
    withContext(Dispatchers.IO) {
      try {
        val file = fileItem.file
        if (file.exists() && file.isFile) {
          if (file.length() > 5 * 1024 * 1024) {
            textContent = "[File is too large to preview (>5MB): ${fileItem.formattedSize}]\nPath: ${file.absolutePath}"
          } else {
            textContent = file.readText()
            lineCount = textContent.lines().size
          }
        }
      } catch (e: Exception) {
        textContent = "Error reading file content:\n${e.localizedMessage}"
      }
    }
    isLoading = false
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp)
        .testTag("document_engine_dialog"),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = cardContainer),
      border = BorderStroke(1.dp, cardBorder)
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Top Toolbar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B))
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Article, contentDescription = null, tint = accentColor)
            Column {
              Text(
                text = fileItem.name,
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = primaryTextColor
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "${fileItem.formattedSize} • $lineCount lines • ${if (isEditing) "Edit Mode" else "Read Only"}",
                style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
              onClick = { isEditing = !isEditing }
            ) {
              Icon(
                imageVector = if (isEditing) Icons.Default.Visibility else Icons.Default.Edit,
                contentDescription = "Toggle Edit",
                tint = accentColor
              )
            }

            if (isEditing) {
              IconButton(
                onClick = {
                  scope.launch(Dispatchers.IO) {
                    try {
                      fileItem.file.writeText(textContent)
                      withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Saved changes!", Toast.LENGTH_SHORT).show()
                        isEditing = false
                      }
                    } catch (e: Exception) {
                      withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
                      }
                    }
                  }
                }
              ) {
                Icon(Icons.Default.Save, contentDescription = "Save", tint = accentColor)
              }
            }

            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = primaryTextColor)
            }
          }
        }

        // Content Area
        if (isLoading) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = accentColor)
          }
        } else {
          if (isEditing) {
            OutlinedTextField(
              value = textContent,
              onValueChange = {
                textContent = it
                lineCount = it.lines().size
              },
              modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("document_editor_field"),
              textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = primaryTextColor
              ),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = cardBorder,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
              )
            )
          } else {
            val scrollState = rememberScrollState()
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
            ) {
              Text(
                text = textContent,
                style = TextStyle(
                  fontFamily = FontFamily.Monospace,
                  fontSize = 13.sp,
                  color = primaryTextColor,
                  lineHeight = 18.sp
                )
              )
            }
          }
        }
      }
    }
  }
}
