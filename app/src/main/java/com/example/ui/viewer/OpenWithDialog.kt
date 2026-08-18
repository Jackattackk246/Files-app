package com.jackattackk246.files.ui.viewer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.jackattackk246.files.model.FileItem
import com.jackattackk246.files.ui.theme.AppThemeMode
import com.jackattackk246.files.ui.theme.ThemeManager
import java.io.File
import java.util.Locale

object ViewerPersistenceManager {
  private const val PREFS_NAME = "viewer_handler_preferences"

  fun getPreferredAction(context: Context, extension: String): String? {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString("pref_${extension.lowercase(Locale.getDefault())}", null)
  }

  fun setPreferredAction(context: Context, extension: String, actionId: String) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .putString("pref_${extension.lowercase(Locale.getDefault())}", actionId)
      .apply()
  }

  fun clearPreferredAction(context: Context, extension: String) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .remove("pref_${extension.lowercase(Locale.getDefault())}")
      .apply()
  }
}

@Composable
fun OpenWithChooserDialog(
  fileItem: FileItem,
  themeMode: AppThemeMode,
  customAccentColor: Color?,
  onOpenInternal: (String) -> Unit, // "audio", "image", "doc"
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val accentColor = ThemeManager.getThemeAccentColor(themeMode, customAccentColor)
  val isLight = ThemeManager.isLightBackgroundProfile(themeMode)
  val primaryTextColor = ThemeManager.getAdaptivePrimaryTextColor(themeMode)
  val secondaryTextColor = ThemeManager.getAdaptiveSecondaryTextColor(themeMode)
  val cardContainer = if (isLight) Color(0xFFFFFFFF) else Color(0xFF14171F)
  val cardBorder = if (isLight) Color(0x33000000) else Color(0x3338BDF8)

  var rememberAlways by remember { mutableStateOf(false) }

  fun launchExternalSystemChooser() {
    try {
      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        fileItem.file
      )
      val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileItem.extension) ?: "*/*"
      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mime)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      val chooser = Intent.createChooser(intent, "Open ${fileItem.name} with...")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
      if (rememberAlways) {
        ViewerPersistenceManager.setPreferredAction(context, fileItem.extension, "external")
      }
      onDismiss()
    } catch (e: Exception) {
      Toast.makeText(context, "No app available to open this file", Toast.LENGTH_SHORT).show()
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("open_with_chooser_dialog"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = cardContainer),
      border = BorderStroke(1.dp, cardBorder)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            "Open File As...",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = primaryTextColor
            )
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = secondaryTextColor)
          }
        }

        Text(
          fileItem.name,
          style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor)
        )

        // Options List
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Option 1: Built-in Player / Viewer depending on type
          if (fileItem.isAudio) {
            ChooserRow(
              icon = Icons.Default.MusicNote,
              title = "Native Media Player",
              subtitle = "Built-in audio player with waveform controls",
              tint = accentColor,
              textColor = primaryTextColor
            ) {
              if (rememberAlways) {
                ViewerPersistenceManager.setPreferredAction(context, fileItem.extension, "native_audio")
              }
              onOpenInternal("audio")
            }
          }

          if (fileItem.isImage) {
            ChooserRow(
              icon = Icons.Default.Image,
              title = "Native Image Viewer",
              subtitle = "High-speed bitmap canvas with pan and zoom",
              tint = accentColor,
              textColor = primaryTextColor
            ) {
              if (rememberAlways) {
                ViewerPersistenceManager.setPreferredAction(context, fileItem.extension, "native_image")
              }
              onOpenInternal("image")
            }
          }

          ChooserRow(
            icon = Icons.Default.Article,
            title = "Document & Code Engine",
            subtitle = "Built-in text / code editor and raw inspector",
            tint = accentColor,
            textColor = primaryTextColor
          ) {
            if (rememberAlways) {
              ViewerPersistenceManager.setPreferredAction(context, fileItem.extension, "native_doc")
            }
            onOpenInternal("doc")
          }

          ChooserRow(
            icon = Icons.Default.OpenInNew,
            title = "External Application",
            subtitle = "Choose from installed apps on this device",
            tint = secondaryTextColor,
            textColor = primaryTextColor
          ) {
            launchExternalSystemChooser()
          }
        }

        // "Always" checkbox memory
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { rememberAlways = !rememberAlways }
            .padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Checkbox(
            checked = rememberAlways,
            onCheckedChange = { rememberAlways = it },
            colors = CheckboxDefaults.colors(
              checkedColor = accentColor,
              checkmarkColor = Color.Black
            )
          )
          Text(
            "Always use this choice for .${fileItem.extension.uppercase()} files",
            style = MaterialTheme.typography.bodySmall.copy(color = primaryTextColor)
          )
        }
      }
    }
  }
}

@Composable
private fun ChooserRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  tint: Color,
  textColor: Color,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    border = BorderStroke(1.dp, Color(0x22888888)),
    shape = RoundedCornerShape(12.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = textColor))
        Text(subtitle, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
      }
      Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
  }
}

@Composable
fun ProtectedPathDialog(
  path: String,
  themeMode: AppThemeMode,
  customAccentColor: Color?,
  onOpenOtherApp: () -> Unit,
  onOpenAnyway: () -> Unit,
  onDismiss: () -> Unit
) {
  val accentColor = ThemeManager.getThemeAccentColor(themeMode, customAccentColor)
  val isLight = ThemeManager.isLightBackgroundProfile(themeMode)
  val primaryTextColor = ThemeManager.getAdaptivePrimaryTextColor(themeMode)
  val secondaryTextColor = ThemeManager.getAdaptiveSecondaryTextColor(themeMode)
  val cardContainer = if (isLight) Color(0xFFFFFFFF) else Color(0xFF14171F)
  val cardBorder = if (isLight) Color(0x33000000) else Color(0x3338BDF8)

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("protected_path_modal_popup"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = cardContainer),
      border = BorderStroke(1.dp, cardBorder)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(28.dp))
          Column {
            Text(
              "Protected System Path",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
              )
            )
            Text(
              "Access to Android/data & Android/obb is restricted by OS policy",
              style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
            )
          }
        }

        Text(
          path,
          style = MaterialTheme.typography.bodySmall.copy(
            color = accentColor,
            fontWeight = FontWeight.SemiBold
          ),
          maxLines = 2,
          overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )

        HorizontalDivider()

        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // 1. Open other app
          Button(
            onClick = {
              onOpenOtherApp()
              onDismiss()
            },
            modifier = Modifier.fillMaxWidth().testTag("protected_path_open_other_app"),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open other app", color = if (isLight) Color.White else Color.Black)
          }

          // 2. Open anyway
          OutlinedButton(
            onClick = {
              onOpenAnyway()
              onDismiss()
            },
            modifier = Modifier.fillMaxWidth().testTag("protected_path_open_anyway"),
            border = BorderStroke(1.dp, accentColor),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open anyway", color = accentColor)
          }

          // 3. Cancel
          TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().testTag("protected_path_cancel"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Cancel", color = secondaryTextColor)
          }
        }
      }
    }
  }
}

