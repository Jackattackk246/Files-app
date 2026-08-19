package com.jackattackk246.files.ui.section

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Environment
import android.text.format.Formatter
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.jackattackk246.files.model.EnvironmentalSeason
import com.jackattackk246.files.ui.theme.AppThemeMode
import com.jackattackk246.files.ui.theme.ThemeManager
import com.jackattackk246.files.util.FileManager
import com.jackattackk246.files.util.RecycleBinEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

enum class ActiveUtilityDialog {
  STORAGE_ANALYZER,
  DUPLICATE_DETECTOR,
  DOCUMENT_VAULT,
  ZIP_COMPRESSION_HUB,
  APK_EXTRACTOR
}

@Composable
fun SystemUtilitiesCardSection(
  themeMode: AppThemeMode,
  season: EnvironmentalSeason,
  customAccentColor: Color?,
  onTrashUpdated: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val primaryTextColor = ThemeManager.getAdaptivePrimaryTextColor(themeMode, season)
  val secondaryTextColor = ThemeManager.getAdaptiveSecondaryTextColor(themeMode, season)
  val accentColor = ThemeManager.getThemeAccentColor(themeMode, customAccentColor)
  val cardContainer = ThemeManager.getAdaptiveCardContainerColor(themeMode, season)
  val cardBorder = ThemeManager.getAdaptiveCardBorderColor(themeMode, season)

  var activeDialog by remember { mutableStateOf<ActiveUtilityDialog?>(null) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("system_utilities_main_card"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = cardContainer),
    border = BorderStroke(1.dp, cardBorder)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(accentColor.copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Handyman,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(20.dp)
            )
          }
          Column {
            Text(
              text = "System Utilities",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
              )
            )
            Text(
              text = "100% Offline Disk Tools & Locker",
              style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor)
            )
          }
        }
      }

      // 5 Utilities Quick Action Items
      UtilityActionItem(
        title = "Storage Analyzer & Large File Finder",
        subtitle = "Category distribution & Top 10 largest files",
        icon = Icons.Default.PieChart,
        accentColor = Color(0xFF00E5FF),
        primaryTextColor = primaryTextColor,
        secondaryTextColor = secondaryTextColor,
        onClick = { activeDialog = ActiveUtilityDialog.STORAGE_ANALYZER }
      )

      UtilityActionItem(
        title = "Duplicate File Detector",
        subtitle = "Scan & wipe redundant identical copies",
        icon = Icons.Default.CopyAll,
        accentColor = Color(0xFFFF9100),
        primaryTextColor = primaryTextColor,
        secondaryTextColor = secondaryTextColor,
        onClick = { activeDialog = ActiveUtilityDialog.DUPLICATE_DETECTOR }
      )

      UtilityActionItem(
        title = "Offline Document Vault",
        subtitle = "Secure passcode protected file locker",
        icon = Icons.Default.Lock,
        accentColor = Color(0xFF00E676),
        primaryTextColor = primaryTextColor,
        secondaryTextColor = secondaryTextColor,
        onClick = { activeDialog = ActiveUtilityDialog.DOCUMENT_VAULT }
      )

      UtilityActionItem(
        title = "Native ZIP / Unzip Hub",
        subtitle = "Compress files & extract archives offline",
        icon = Icons.Default.FolderZip,
        accentColor = Color(0xFFD500F9),
        primaryTextColor = primaryTextColor,
        secondaryTextColor = secondaryTextColor,
        onClick = { activeDialog = ActiveUtilityDialog.ZIP_COMPRESSION_HUB }
      )

      UtilityActionItem(
        title = "User Compliant APK Extractor",
        subtitle = "Dump installed apps to offline APK packages",
        icon = Icons.Default.Android,
        accentColor = Color(0xFF00B0FF),
        primaryTextColor = primaryTextColor,
        secondaryTextColor = secondaryTextColor,
        onClick = { activeDialog = ActiveUtilityDialog.APK_EXTRACTOR }
      )
    }
  }

  // Active Dialog Handler
  when (activeDialog) {
    ActiveUtilityDialog.STORAGE_ANALYZER -> {
      StorageAnalyzerDialog(
        accentColor = accentColor,
        primaryTextColor = primaryTextColor,
        secondaryTextColor = secondaryTextColor,
        onTrashUpdated = onTrashUpdated,
        onDismiss = { activeDialog = null }
      )
    }
    ActiveUtilityDialog.DUPLICATE_DETECTOR -> {
      DuplicateDetectorDialog(
        accentColor = accentColor,
        primaryTextColor = primaryTextColor,
        secondaryTextColor = secondaryTextColor,
        onTrashUpdated = onTrashUpdated,
        onDismiss = { activeDialog = null }
      )
    }
    ActiveUtilityDialog.DOCUMENT_VAULT -> {
      DocumentVaultDialog(
        accentColor = accentColor,
        primaryTextColor = primaryTextColor,
        secondaryTextColor = secondaryTextColor,
        onDismiss = { activeDialog = null }
      )
    }
    ActiveUtilityDialog.ZIP_COMPRESSION_HUB -> {
      ZipHubDialog(
        accentColor = accentColor,
        primaryTextColor = primaryTextColor,
        secondaryTextColor = secondaryTextColor,
        onDismiss = { activeDialog = null }
      )
    }
    ActiveUtilityDialog.APK_EXTRACTOR -> {
      ApkExtractorDialog(
        accentColor = accentColor,
        primaryTextColor = primaryTextColor,
        secondaryTextColor = secondaryTextColor,
        onTrashUpdated = onTrashUpdated,
        onDismiss = { activeDialog = null }
      )
    }
    null -> {}
  }
}

@Composable
private fun UtilityActionItem(
  title: String,
  subtitle: String,
  icon: ImageVector,
  accentColor: Color,
  primaryTextColor: Color,
  secondaryTextColor: Color,
  onClick: () -> Unit
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFF22232A),
    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.30f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(accentColor.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(22.dp)
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            color = primaryTextColor
          )
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor)
        )
      }

      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = null,
        tint = secondaryTextColor,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

// ============================================================================
// A. STORAGE ANALYZER & LARGE FILE FINDER DIALOG
// ============================================================================
@Composable
private fun StorageAnalyzerDialog(
  accentColor: Color,
  primaryTextColor: Color,
  secondaryTextColor: Color,
  onTrashUpdated: () -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var isScanning by remember { mutableStateOf(true) }
  var top10Files by remember { mutableStateOf<List<File>>(emptyList()) }
  var imageBytes by remember { mutableLongStateOf(0L) }
  var videoBytes by remember { mutableLongStateOf(0L) }
  var audioBytes by remember { mutableLongStateOf(0L) }
  var otherBytes by remember { mutableLongStateOf(0L) }

  LaunchedEffect(Unit) {
    withContext(Dispatchers.IO) {
      val root = Environment.getExternalStorageDirectory()
      val fileList = mutableListOf<File>()
      var img = 0L
      var vid = 0L
      var aud = 0L
      var oth = 0L

      root.walkTopDown()
        .onEnter { dir ->
          dir.name != ".recycle_bin" && dir.name != ".secure_vault" && !dir.name.startsWith(".")
        }
        .forEach { file ->
          if (file.isFile && !file.name.startsWith(".")) {
            val size = file.length()
            fileList.add(file)

            when (file.extension.lowercase()) {
              "jpg", "jpeg", "png", "webp", "gif", "heic" -> img += size
              "mp4", "mkv", "avi", "mov", "webm", "3gp" -> vid += size
              "mp3", "wav", "flac", "aac", "ogg", "m4a" -> aud += size
              else -> oth += size
            }
          }
        }

      top10Files = fileList.sortedByDescending { it.length() }.take(10)
      imageBytes = img
      videoBytes = vid
      audioBytes = aud
      otherBytes = oth
      isScanning = false
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(Icons.Default.PieChart, contentDescription = null, tint = accentColor)
        Text("Storage Analyzer", color = primaryTextColor, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      if (isScanning) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          CircularProgressIndicator(color = accentColor)
          Text("Scanning disk structure...", color = secondaryTextColor, fontSize = 13.sp)
        }
      } else {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Distribution summary
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14151A))
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("Category Breakdown", fontWeight = FontWeight.Bold, color = primaryTextColor, fontSize = 13.sp)
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Images: ${Formatter.formatFileSize(context, imageBytes)}", color = Color(0xFF00E5FF), fontSize = 12.sp)
                Text("Videos: ${Formatter.formatFileSize(context, videoBytes)}", color = Color(0xFFFF9100), fontSize = 12.sp)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Audio: ${Formatter.formatFileSize(context, audioBytes)}", color = Color(0xFF00E676), fontSize = 12.sp)
                Text("System/Other: ${Formatter.formatFileSize(context, otherBytes)}", color = secondaryTextColor, fontSize = 12.sp)
              }
            }
          }

          Text("Top 10 Largest Files", fontWeight = FontWeight.Bold, color = primaryTextColor, fontSize = 14.sp)

          LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(top10Files) { file ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF22232A),
                border = BorderStroke(1.dp, Color(0xFF33343D))
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(file.name, fontWeight = FontWeight.Bold, color = primaryTextColor, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(Formatter.formatFileSize(context, file.length()), color = accentColor, fontSize = 11.sp)
                  }

                  IconButton(
                    onClick = {
                      val moved = RecycleBinEngine.moveToRecycleBin(file)
                      if (moved) {
                        Toast.makeText(context, "${file.name} moved to Recycle Bin", Toast.LENGTH_SHORT).show()
                        top10Files = top10Files.filter { it.absolutePath != file.absolutePath }
                        onTrashUpdated()
                      }
                    }
                  ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Recycle", tint = Color(0xFFFF5252))
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black)) {
        Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    containerColor = Color(0xFF1C1D22),
    shape = RoundedCornerShape(16.dp)
  )
}

// ============================================================================
// B. DUPLICATE FILE DETECTOR DIALOG
// ============================================================================
private data class DuplicatePair(
  val original: File,
  val duplicate: File
)

@Composable
private fun DuplicateDetectorDialog(
  accentColor: Color,
  primaryTextColor: Color,
  secondaryTextColor: Color,
  onTrashUpdated: () -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var isScanning by remember { mutableStateOf(true) }
  var duplicatesList by remember { mutableStateOf<List<DuplicatePair>>(emptyList()) }

  LaunchedEffect(Unit) {
    withContext(Dispatchers.IO) {
      val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
      val documents = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

      val filesToScan = mutableListOf<File>()
      listOf(downloads, documents).forEach { dir ->
        if (dir.exists()) {
          dir.walkTopDown().filter { it.isFile && it.length() > 1024L }.forEach { filesToScan.add(it) }
        }
      }

      // Group by length first for speed
      val lengthGroups = filesToScan.groupBy { it.length() }.filter { it.value.size > 1 }
      val dups = mutableListOf<DuplicatePair>()

      for ((_, list) in lengthGroups) {
        // Hash check
        val hashMap = mutableMapOf<String, File>()
        for (f in list) {
          try {
            val hash = getFileMd5Quick(f)
            if (hashMap.containsKey(hash)) {
              dups.add(DuplicatePair(original = hashMap[hash]!!, duplicate = f))
            } else {
              hashMap[hash] = f
            }
          } catch (_: Exception) {}
        }
      }

      duplicatesList = dups
      isScanning = false
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(Icons.Default.CopyAll, contentDescription = null, tint = accentColor)
        Text("Duplicate Detector", color = primaryTextColor, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      if (isScanning) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          CircularProgressIndicator(color = accentColor)
          Text("Scanning for identical duplicates...", color = secondaryTextColor, fontSize = 13.sp)
        }
      } else if (duplicatesList.isEmpty()) {
        Text("No duplicate files found across Downloads or Documents directories.", color = secondaryTextColor)
      } else {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Found ${duplicatesList.size} duplicates", color = primaryTextColor, fontWeight = FontWeight.Bold)
            TextButton(
              onClick = {
                var wipedCount = 0
                duplicatesList.forEach { pair ->
                  if (RecycleBinEngine.moveToRecycleBin(pair.duplicate)) {
                    wipedCount++
                  }
                }
                Toast.makeText(context, "Wiped $wipedCount duplicate files to Recycle Bin", Toast.LENGTH_SHORT).show()
                duplicatesList = emptyList()
                onTrashUpdated()
              }
            ) {
              Text("Wipe All Duplicates", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
            }
          }

          LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(duplicatesList) { pair ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF22232A),
                border = BorderStroke(1.dp, Color(0xFF33343D))
              ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Text("Original: ${pair.original.name}", fontSize = 11.sp, color = Color(0xFF00E676), maxLines = 1, overflow = TextOverflow.Ellipsis)
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text("Duplicate: ${pair.duplicate.name}", fontSize = 11.sp, color = primaryTextColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                      Text(Formatter.formatFileSize(context, pair.duplicate.length()), fontSize = 10.sp, color = secondaryTextColor)
                    }
                    Button(
                      onClick = {
                        if (RecycleBinEngine.moveToRecycleBin(pair.duplicate)) {
                          Toast.makeText(context, "Duplicate moved to Recycle Bin", Toast.LENGTH_SHORT).show()
                          duplicatesList = duplicatesList.filter { it.duplicate.absolutePath != pair.duplicate.absolutePath }
                          onTrashUpdated()
                        }
                      },
                      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252), contentColor = Color.White),
                      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                      Text("Wipe Copy", fontSize = 10.sp)
                    }
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black)) {
        Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    containerColor = Color(0xFF1C1D22),
    shape = RoundedCornerShape(16.dp)
  )
}

private fun getFileMd5Quick(file: File): String {
  val digest = MessageDigest.getInstance("MD5")
  FileInputStream(file).use { fis ->
    val buffer = ByteArray(8192)
    var read = fis.read(buffer)
    if (read > 0) {
      digest.update(buffer, 0, read)
    }
  }
  return digest.digest().joinToString("") { "%02x".format(it) }
}

// ============================================================================
// C. OFFLINE DOCUMENT VAULT (SECURE LOCKER)
// ============================================================================
@Composable
private fun DocumentVaultDialog(
  accentColor: Color,
  primaryTextColor: Color,
  secondaryTextColor: Color,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val prefs = remember { context.getSharedPreferences("secure_vault_prefs", Context.MODE_PRIVATE) }
  var savedPin by remember { mutableStateOf(prefs.getString("vault_pin", null)) }
  var pinInput by remember { mutableStateOf("") }
  var isUnlocked by remember { mutableStateOf(savedPin == null) }
  var vaultFiles by remember { mutableStateOf<List<File>>(emptyList()) }

  val vaultDir = remember {
    File(context.filesDir, ".secure_vault").apply { if (!exists()) mkdirs() }
  }

  fun refreshVaultFiles() {
    vaultFiles = vaultDir.listFiles()?.filter { it.isFile } ?: emptyList()
  }

  LaunchedEffect(isUnlocked) {
    if (isUnlocked) refreshVaultFiles()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = accentColor)
        Text("Document Vault", color = primaryTextColor, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      if (!isUnlocked) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text("Enter 4-Digit Passcode to unlock Vault:", color = secondaryTextColor, fontSize = 13.sp)
          OutlinedTextField(
            value = pinInput,
            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.width(180.dp)
          )
          Button(
            onClick = {
              if (pinInput == savedPin) {
                isUnlocked = true
              } else {
                Toast.makeText(context, "Incorrect Passcode", Toast.LENGTH_SHORT).show()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black)
          ) {
            Text("Unlock Vault", fontWeight = FontWeight.Bold)
          }
        }
      } else if (savedPin == null) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text("Set a 4-Digit Security Passcode:", color = secondaryTextColor, fontSize = 13.sp)
          OutlinedTextField(
            value = pinInput,
            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.width(180.dp)
          )
          Button(
            onClick = {
              if (pinInput.length == 4) {
                prefs.edit().putString("vault_pin", pinInput).apply()
                savedPin = pinInput
                Toast.makeText(context, "Passcode Configured!", Toast.LENGTH_SHORT).show()
              } else {
                Toast.makeText(context, "PIN must be 4 digits", Toast.LENGTH_SHORT).show()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black)
          ) {
            Text("Save Passcode", fontWeight = FontWeight.Bold)
          }
        }
      } else {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Vault Contents (${vaultFiles.size} items)", fontWeight = FontWeight.Bold, color = primaryTextColor)
            Button(
              onClick = {
                // Quick import demo from Downloads
                val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val candidate = downloads.listFiles()?.firstOrNull { it.isFile }
                if (candidate != null) {
                  val target = File(vaultDir, candidate.name)
                  candidate.copyTo(target, overwrite = true)
                  candidate.delete()
                  Toast.makeText(context, "Moved ${candidate.name} into Secure Vault", Toast.LENGTH_SHORT).show()
                  refreshVaultFiles()
                } else {
                  Toast.makeText(context, "No files found in Downloads to import", Toast.LENGTH_SHORT).show()
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text("Import File", fontSize = 11.sp, color = Color.Black)
            }
          }

          if (vaultFiles.isEmpty()) {
            Text("Vault is empty. Imported files are isolated from file search.", color = secondaryTextColor, fontSize = 12.sp)
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              items(vaultFiles) { file ->
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color(0xFF22232A),
                  border = BorderStroke(1.dp, Color(0xFF33343D))
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(file.name, fontWeight = FontWeight.Bold, color = primaryTextColor, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                      Text(Formatter.formatFileSize(context, file.length()), color = accentColor, fontSize = 11.sp)
                    }

                    TextButton(
                      onClick = {
                        val target = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), file.name)
                        file.copyTo(target, overwrite = true)
                        file.delete()
                        Toast.makeText(context, "Restored to Downloads", Toast.LENGTH_SHORT).show()
                        refreshVaultFiles()
                      }
                    ) {
                      Text("Export", color = accentColor, fontSize = 11.sp)
                    }
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black)) {
        Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    containerColor = Color(0xFF1C1D22),
    shape = RoundedCornerShape(16.dp)
  )
}

// ============================================================================
// D. NATIVE ZIP / UNZIP COMPRESSION HUB DIALOG
// ============================================================================
@Composable
private fun ZipHubDialog(
  accentColor: Color,
  primaryTextColor: Color,
  secondaryTextColor: Color,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  var isProcessing by remember { mutableStateOf(false) }
  var progressRatio by remember { mutableFloatStateOf(0f) }
  var statusMsg by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(Icons.Default.FolderZip, contentDescription = null, tint = accentColor)
        Text("ZIP Compression Hub", color = primaryTextColor, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        if (isProcessing) {
          LinearProgressIndicator(progress = { progressRatio }, modifier = Modifier.fillMaxWidth(), color = accentColor)
          Text(statusMsg, color = secondaryTextColor, fontSize = 12.sp)
        } else {
          Surface(
            onClick = {
              coroutineScope.launch {
                isProcessing = true
                val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val sources = downloads.listFiles()?.filter { it.isFile }?.take(3) ?: emptyList()
                if (sources.isNotEmpty()) {
                  val outZip = File(downloads, "Archive_${System.currentTimeMillis() % 10000}.zip")
                  val res = FileManager.compressToZip(sources, outZip) { ratio, msg ->
                    progressRatio = ratio
                    statusMsg = msg
                  }
                  isProcessing = false
                  if (res.isSuccess) {
                    Toast.makeText(context, "Archive created: ${outZip.name}", Toast.LENGTH_SHORT).show()
                  } else {
                    Toast.makeText(context, "Compression failed", Toast.LENGTH_SHORT).show()
                  }
                } else {
                  isProcessing = false
                  Toast.makeText(context, "No files found in Downloads to compress", Toast.LENGTH_SHORT).show()
                }
              }
            },
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF22232A),
            border = BorderStroke(1.dp, accentColor)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Icon(Icons.Default.Compress, contentDescription = null, tint = accentColor)
              Column {
                Text("Compress Downloads Sample to .ZIP", fontWeight = FontWeight.Bold, color = primaryTextColor, fontSize = 12.sp)
                Text("Create standard ZIP archive offline", color = secondaryTextColor, fontSize = 11.sp)
              }
            }
          }

          Surface(
            onClick = {
              coroutineScope.launch {
                isProcessing = true
                val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetZip = downloads.listFiles()?.firstOrNull { it.extension.equals("zip", ignoreCase = true) }
                if (targetZip != null) {
                  val extractDir = File(downloads, "Extracted_${targetZip.nameWithoutExtension}")
                  val res = FileManager.extractZip(targetZip, extractDir) { ratio, msg ->
                    progressRatio = ratio
                    statusMsg = msg
                  }
                  isProcessing = false
                  if (res.isSuccess) {
                    Toast.makeText(context, "Extracted to ${extractDir.name}", Toast.LENGTH_SHORT).show()
                  } else {
                    Toast.makeText(context, "Extraction failed", Toast.LENGTH_SHORT).show()
                  }
                } else {
                  isProcessing = false
                  Toast.makeText(context, "No .ZIP archive found in Downloads", Toast.LENGTH_SHORT).show()
                }
              }
            },
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF22232A),
            border = BorderStroke(1.dp, Color(0xFF33343D))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color(0xFF00E676))
              Column {
                Text("Extract Recent .ZIP Archive", fontWeight = FontWeight.Bold, color = primaryTextColor, fontSize = 12.sp)
                Text("Decompress zip archive into directory", color = secondaryTextColor, fontSize = 11.sp)
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black)) {
        Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    containerColor = Color(0xFF1C1D22),
    shape = RoundedCornerShape(16.dp)
  )
}

// ============================================================================
// E. USER COMPLIANT APK EXTRACTOR DIALOG
// ============================================================================
private data class InstalledAppItem(
  val label: String,
  val packageName: String,
  val sourceApk: File,
  val iconBitmap: androidx.compose.ui.graphics.ImageBitmap?
)

@Composable
private fun ApkExtractorDialog(
  accentColor: Color,
  primaryTextColor: Color,
  secondaryTextColor: Color,
  onTrashUpdated: () -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var isScanning by remember { mutableStateOf(true) }
  var installedApps by remember { mutableStateOf<List<InstalledAppItem>>(emptyList()) }

  LaunchedEffect(Unit) {
    withContext(Dispatchers.IO) {
      val pm = context.packageManager
      val packages = pm.getInstalledPackages(0)
      val appList = mutableListOf<InstalledAppItem>()

      for (pkg in packages) {
        val appInfo = pkg.applicationInfo ?: continue
        if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
          val label = pm.getApplicationLabel(appInfo).toString()
          val source = File(appInfo.sourceDir)
          val iconDrawable = pm.getApplicationIcon(appInfo)
          val bmp = try { iconDrawable.toBitmap(64, 64).asImageBitmap() } catch (_: Exception) { null }

          appList.add(
            InstalledAppItem(
              label = label,
              packageName = pkg.packageName,
              sourceApk = source,
              iconBitmap = bmp
            )
          )
        }
      }

      installedApps = appList.sortedBy { it.label }
      isScanning = false
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(Icons.Default.Android, contentDescription = null, tint = accentColor)
        Text("APK Extractor", color = primaryTextColor, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      if (isScanning) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          CircularProgressIndicator(color = accentColor)
          Text("Querying installed applications...", color = secondaryTextColor, fontSize = 13.sp)
        }
      } else if (installedApps.isEmpty()) {
        Text("No user-installed applications found on device.", color = secondaryTextColor)
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(installedApps) { app ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF22232A),
              border = BorderStroke(1.dp, Color(0xFF33343D))
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                if (app.iconBitmap != null) {
                  Image(
                    bitmap = app.iconBitmap,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                  )
                } else {
                  Icon(Icons.Default.Android, contentDescription = null, tint = accentColor, modifier = Modifier.size(36.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                  Text(app.label, fontWeight = FontWeight.Bold, color = primaryTextColor, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                  Text(Formatter.formatFileSize(context, app.sourceApk.length()), color = secondaryTextColor, fontSize = 11.sp)
                }

                Button(
                  onClick = {
                    val outDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "APK_Backups")
                    if (!outDir.exists()) outDir.mkdirs()
                    val targetApk = File(outDir, "Files.apk")
                    app.sourceApk.copyTo(targetApk, overwrite = true)
                    Toast.makeText(context, "Extracted to Downloads/APK_Backups", Toast.LENGTH_SHORT).show()
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                  Text("Dump APK", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black)) {
        Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    containerColor = Color(0xFF1C1D22),
    shape = RoundedCornerShape(16.dp)
  )
}
