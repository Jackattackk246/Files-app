package com.jackattackk246.files.ui

import android.content.Context
import android.os.Environment
import android.text.format.Formatter
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jackattackk246.files.model.EnvironmentalSeason
import com.jackattackk246.files.ui.theme.AppThemeMode
import com.jackattackk246.files.ui.theme.ThemeManager
import com.jackattackk246.files.util.*
import java.io.File

enum class CardSizeProfile {
  SMALL,
  MEDIUM,
  WIDE
}

data class DashboardCardConfig(
  val id: String,
  val title: String,
  val size: CardSizeProfile
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
  storageMetrics: FileManager.StorageMetrics,
  currentDirectory: File,
  themeMode: AppThemeMode = AppThemeMode.MIDNIGHT_MATTE_BLACK,
  customAccentColor: Color? = null,
  season: EnvironmentalSeason = EnvironmentalSeason.AUTO,
  onNavigateToExplorer: (File?, String?) -> Unit,
  onNavigateToRecycleBin: () -> Unit = {},
  onNavigateToSettings: () -> Unit
) {
  val context = LocalContext.current
  val configuration = LocalConfiguration.current
  val desktopPalette by com.aistudio.fileslauncher.ui.ThemeSynchronizationBridge.paletteState.collectAsState()

  val displayProfile = remember(configuration, desktopPalette.isForcedWindows11Desktop) {
    if (desktopPalette.isForcedWindows11Desktop) {
      DeviceDisplayProfile.EXTERNAL_DEX_DESKTOP
    } else {
      DeviceEnvironmentDetector.resolveDisplayProfile(context, configuration)
    }
  }

  var physicalMetrics by remember { mutableStateOf(SystemStorageStatsEngine.getPhysicalStorageMetrics(context)) }
  var isRefreshing by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()
  val refresh = {
    scope.launch {
      isRefreshing = true
      physicalMetrics = SystemStorageStatsEngine.getPhysicalStorageMetrics(context)
      isRefreshing = false
    }
    Unit
  }

  var isEditModeUnlocked by remember { mutableStateOf(DashboardPreferences.isEditModeUnlocked(context)) }
  var isPlusMenuExpanded by remember { mutableStateOf(false) }
  var showInfoDialog by remember { mutableStateOf(false) }
  var trashedCount by remember { mutableStateOf(RecycleBinEngine.getItemCount()) }

  val usbState = UsbStorageManager.usbState.collectAsState().value

  val isLight = ThemeManager.isLightBackgroundProfile(themeMode, season)
  val primaryTextColor = ThemeManager.getAdaptivePrimaryTextColor(themeMode, season)
  val secondaryTextColor = ThemeManager.getAdaptiveSecondaryTextColor(themeMode, season)
  val accentColor = ThemeManager.getThemeAccentColor(themeMode, customAccentColor)
  val cardContainer = ThemeManager.getAdaptiveCardContainerColor(themeMode, season)
  val cardBorder = ThemeManager.getAdaptiveCardBorderColor(themeMode, season)

  // Auto refresh trashed count when returning
  LaunchedEffect(Unit) {
    trashedCount = RecycleBinEngine.getItemCount()
  }

  val contentModifier = if (displayProfile == DeviceDisplayProfile.TABLET) {
    Modifier
      .fillMaxSize()
      .widthIn(max = 560.dp)
      .padding(horizontal = 16.dp)
  } else {
    Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(if (desktopPalette.isDesktopCanvasActive) desktopPalette.widescreenContainerBg else Color.Transparent),
    contentAlignment = Alignment.TopCenter
  ) {
    PullToRefreshBox(
      isRefreshing = isRefreshing,
      onRefresh = refresh
    ) {
      LazyColumn(
        modifier = contentModifier.testTag("dashboard_scroll_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
      ) {

      // ====================================================================
      // 1. TOP PROFILE & GREETING BANNER
      // ====================================================================
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            val hardwareIcon = when (displayProfile) {
              DeviceDisplayProfile.EXTERNAL_DEX_DESKTOP -> Icons.Default.DesktopWindows
              DeviceDisplayProfile.TABLET -> Icons.Default.TabletAndroid
              DeviceDisplayProfile.PHONE -> Icons.Default.Smartphone
            }

            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(alpha = 0.20f))
                .border(1.dp, accentColor.copy(alpha = 0.40f), RoundedCornerShape(12.dp))
                .clickable {
                  val newDesktopState = !desktopPalette.isForcedWindows11Desktop
                  com.aistudio.fileslauncher.ui.ThemeSynchronizationBridge.setForcedWindows11DesktopEnabled(context, newDesktopState)
                  HapticManager.selectionTick(context)
                  Toast.makeText(context, if (newDesktopState) "Windows 11 Desktop Mode Activated" else "Mobile Dashboard View Activated", Toast.LENGTH_SHORT).show()
                }
                .testTag("desktop_mode_toggle_button"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = hardwareIcon,
                contentDescription = "Hardware Profile / Toggle Desktop Mode",
                tint = accentColor,
                modifier = Modifier.size(24.dp)
              )
            }

            val dynamicGreeting = UserProfilePreferences.getDynamicTimeGreeting(context)

            Text(
              text = dynamicGreeting,
              modifier = Modifier
                .testTag("greeting_text_view")
                .wrapContentHeight(),
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = primaryTextColor
              )
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Edit Mode Button (Zero passcode requirement)
            IconButton(
              onClick = {
                HapticManager.selectionTick(context)
                isEditModeUnlocked = !isEditModeUnlocked
                DashboardPreferences.setEditModeUnlocked(context, isEditModeUnlocked)
              },
              modifier = Modifier.testTag("dashboard_customize_button")
            ) {
              Icon(
                imageVector = if (isEditModeUnlocked) Icons.Default.Check else Icons.Default.DashboardCustomize,
                contentDescription = "Customize Dashboard Layout",
                tint = if (isEditModeUnlocked) accentColor else primaryTextColor
              )
            }

            IconButton(
              onClick = { showInfoDialog = true },
              modifier = Modifier.testTag("header_info_button")
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = primaryTextColor
              )
            }
          }
        }
      }

      // ====================================================================
      // 1B. EDIT MODE BANNER (When Active)
      // ====================================================================
      if (isEditModeUnlocked) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("dashboard_edit_mode_banner"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1D22)),
            border = BorderStroke(1.dp, accentColor)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(
                  Icons.Default.EditNote,
                  contentDescription = null,
                  tint = accentColor,
                  modifier = Modifier.size(20.dp)
                )
                Text(
                  text = "Edit Mode Active: Scroll to bottom (+) to reveal full system utilities.",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = primaryTextColor,
                    fontWeight = FontWeight.SemiBold
                  )
                )
              }

              Button(
                onClick = {
                  HapticManager.selectionTick(context)
                  isEditModeUnlocked = false
                  DashboardPreferences.setEditModeUnlocked(context, false)
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.testTag("done_edit_mode_button")
              ) {
                Text("Done", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // ====================================================================
      // 1C. DEVICE STORAGE PROGRESS METER BLOCK
      // ====================================================================
      item {
        val totalBytes = if (physicalMetrics.totalHardwareBytes > 0L) physicalMetrics.totalHardwareBytes else storageMetrics.realTotalBytes
        val freeBytes = if (physicalMetrics.freeHardwareBytes > 0L) physicalMetrics.freeHardwareBytes else storageMetrics.realFreeBytes
        val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
        val usedRatio = if (totalBytes > 0L) (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("device_storage_meter_card"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = cardContainer),
          border = BorderStroke(1.dp, cardBorder)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Storage,
                  contentDescription = null,
                  tint = accentColor,
                  modifier = Modifier.size(22.dp)
                )
                Text(
                  text = "Device Storage",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor
                  )
                )
              }

              Text(
                text = "%.0f%% Used".format(physicalMetrics.percentageUsed),
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = accentColor
                )
              )
            }

            LinearProgressIndicator(
              progress = { physicalMetrics.usedRatio },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = accentColor,
              trackColor = Color(0xFF2C2D35),
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Used: ${physicalMetrics.formattedUsed} | Free: ${physicalMetrics.formattedFree} | Total: ${physicalMetrics.formattedTotal}",
                style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor)
              )
            }
          }
        }
      }

      // ====================================================================
      // 2. LOCAL STORAGE HUBS GRID (2-Column Row: Download & Main Storage)
      // ====================================================================
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Download Folder Card
          Card(
            onClick = {
              val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
              onNavigateToExplorer(downloadDir, null)
            },
            modifier = Modifier
              .weight(1f)
              .height(100.dp)
              .testTag("download_folder_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardContainer),
            border = BorderStroke(1.dp, cardBorder)
          ) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF3B82F6).copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(20.dp)
                  )
                }
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                  contentDescription = null,
                  tint = secondaryTextColor.copy(alpha = 0.5f),
                  modifier = Modifier.size(14.dp)
                )
              }

              Column {
                Text(
                  text = "Download Folder",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "Downloads & Files",
                  style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor),
                  maxLines = 1
                )
              }
            }
          }

          // Main Storage Card
          Card(
            onClick = {
              val root = FileManager.getRootDirectory()
              onNavigateToExplorer(root, null)
            },
            modifier = Modifier
              .weight(1f)
              .height(100.dp)
              .testTag("main_storage_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardContainer),
            border = BorderStroke(1.dp, cardBorder)
          ) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                  contentDescription = null,
                  tint = secondaryTextColor.copy(alpha = 0.5f),
                  modifier = Modifier.size(14.dp)
                )
              }

              Column {
                Text(
                  text = "Main Storage",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "Internal User Drive",
                  style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor),
                  maxLines = 1
                )
              }
            }
          }
        }
      }

      // ====================================================================
      // 2B. USB OTG CARD (Dynamically Shown when Plugged In)
      // ====================================================================
      if (usbState.isConnected) {
        item {
          Card(
            onClick = {
              if (usbState.mountPath != null) {
                onNavigateToExplorer(usbState.mountPath, null)
              } else {
                Toast.makeText(context, "USB Storage: ${usbState.volumeLabel}", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(72.dp)
              .testTag("usb_storage_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardContainer),
            border = BorderStroke(1.dp, Color(0xFF10B981))
          ) {
            Row(
              modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .background(Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Usb,
                  contentDescription = "USB",
                  tint = Color(0xFF10B981),
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = usbState.volumeLabel,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor
                  )
                )
                Text(
                  text = if (usbState.freeBytes > 0) "${Formatter.formatFileSize(context, usbState.freeBytes)} free" else "Mounted OTG Flash Drive",
                  style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
                )
              }
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = secondaryTextColor.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
              )
            }
          }
        }
      }

      // ====================================================================
      // 2C. RECYCLE BIN (Clean Full-Width Card)
      // ====================================================================
      item {
        Card(
          onClick = {
            onNavigateToRecycleBin()
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .testTag("recycle_bin_card"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = cardContainer),
          border = BorderStroke(1.dp, cardBorder)
        ) {
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .background(accentColor.copy(alpha = 0.18f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Recycle Bin",
                  tint = accentColor,
                  modifier = Modifier.size(22.dp)
                )
              }

              Column {
                Text(
                  text = "Recycle Bin",
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor
                  )
                )
                Text(
                  text = if (trashedCount > 0) "$trashedCount items in trash" else "Trash is empty",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (trashedCount > 0) accentColor else secondaryTextColor
                  )
                )
              }
            }

            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
              contentDescription = null,
              tint = secondaryTextColor.copy(alpha = 0.5f),
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }

      // ====================================================================
      // 3. MEDIA & PACKAGE HUBS (5-Item Icon Grid)
      // ====================================================================
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("media_and_package_hubs_card"),
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
            Text(
              text = "Media & Package Hubs",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
              )
            )

            // 5-Item Horizontal Grid
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              val mediaCategories = listOf(
                MediaHubItem("Images", Icons.Default.Image, Color(0xFF10B981)) {
                  onNavigateToExplorer(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "jpg")
                },
                MediaHubItem("Audio", Icons.Default.MusicNote, Color(0xFF3B82F6)) {
                  onNavigateToExplorer(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "mp3")
                },
                MediaHubItem("Videos", Icons.Default.Videocam, Color(0xFFF59E0B)) {
                  onNavigateToExplorer(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "mp4")
                },
                MediaHubItem("APK", Icons.Default.Android, Color(0xFF8B5CF6)) {
                  onNavigateToExplorer(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "apk")
                },
                MediaHubItem("Docs", Icons.Default.Description, Color(0xFFEC4899)) {
                  onNavigateToExplorer(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "pdf")
                }
              )

              mediaCategories.forEach { item ->
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { item.onClick() }
                    .padding(horizontal = 4.dp, vertical = 6.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(46.dp)
                      .background(item.color.copy(alpha = 0.20f), RoundedCornerShape(12.dp))
                      .border(1.dp, item.color.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = item.icon,
                      contentDescription = item.label,
                      tint = item.color,
                      modifier = Modifier.size(24.dp)
                    )
                  }
                  Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.SemiBold,
                      color = primaryTextColor,
                      fontSize = 11.sp
                    )
                  )
                }
              }
            }
          }
        }
      }

      // ====================================================================
      // 4. QUICK FILE ACTIONS BUTTONS (At Bottom)
      // ====================================================================
      item {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "Quick File Actions",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = primaryTextColor
            )
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Button(
              onClick = {
                FileManager.openPathSAFBackdoor(context, currentDirectory.absolutePath)
              },
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("open_document_picker_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = cardContainer,
                contentColor = primaryTextColor
              ),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, cardBorder)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Icons.Default.FileOpen, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                Text("Document Picker", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              }
            }

            Button(
              onClick = {
                onNavigateToExplorer(File("/"), null)
              },
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("open_root_directory_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = cardContainer,
                contentColor = primaryTextColor
              ),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, cardBorder)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                Text("Root Directory", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
      }

      // ====================================================================
      // 5. EDIT MODE: BOTTOM (+) TILE ACTION (NESTS 13 ADVANCED SYSTEM ENGINES)
      // ====================================================================
      if (isEditModeUnlocked) {
        item {
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Card(
              onClick = {
                HapticManager.selectionTick(context)
                isPlusMenuExpanded = !isPlusMenuExpanded
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("add_tile_button"),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = cardContainer),
              border = BorderStroke(1.5.dp, accentColor)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = if (isPlusMenuExpanded) Icons.Default.Remove else Icons.Default.Add,
                  contentDescription = "Expand Advanced System Engine Blocks",
                  tint = accentColor,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isPlusMenuExpanded) "Collapse Advanced Utilities (13)" else "Advanced System Engine Utilities (13)",
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor
                  )
                )
              }
            }

            AnimatedVisibility(
              visible = isPlusMenuExpanded,
              enter = expandVertically() + fadeIn(),
              exit = shrinkVertically() + fadeOut()
            ) {
              Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                val engineItems = listOf(
                  AdvancedEngineItem("Archive Compression Studio", "Create, extract and analyze zip/tar/7z archives", Icons.Default.FolderZip, Color(0xFF38BDF8)),
                  AdvancedEngineItem("Duplicate File Inspector", "Scan and identify duplicate blocks offline", Icons.Default.FindInPage, Color(0xFFF43F5E)),
                  AdvancedEngineItem("APK Package Extractor", "Backup and inspect installed app packages", Icons.Default.Android, Color(0xFF10B981)),
                  AdvancedEngineItem("Zero-Fill Secure Shredder", "Multi-pass physical block sanitization", Icons.Default.Security, Color(0xFFEF4444)),
                  AdvancedEngineItem("SMART Flash Lifecycle Wear Analyzer", "Flash memory endurance and block health monitor", Icons.Default.Memory, Color(0xFFA855F7)),
                  AdvancedEngineItem("Dynamic Disk Defragmenter", "Trim and optimize flash storage partitions", Icons.Default.Speed, Color(0xFFEAB308)),
                  AdvancedEngineItem("Partition Image Creator", "Create raw byte-exact disk image dumps", Icons.Default.Save, Color(0xFF06B6D4)),
                  AdvancedEngineItem("Hidden Alternate Data Stream", "Inspect metadata streams and ads trackers", Icons.Default.Visibility, Color(0xFFEC4899)),
                  AdvancedEngineItem("Deep EXIF Metadata Scrubbing Studio", "Sanitize sensitive GPS/camera exif records", Icons.Default.PhotoCamera, Color(0xFF6366F1)),
                  AdvancedEngineItem("Symlink & Hardlink Terminal Director", "Audit and create local filesystem symbolic links", Icons.Default.Terminal, Color(0xFF14B8A6)),
                  AdvancedEngineItem("Smart File Triage Engine", "Automated file categorization matrix", Icons.Default.FilterAlt, Color(0xFFF97316)),
                  AdvancedEngineItem("Local Ransomware Sandbox Guard", "Real-time unauthorized encryption detector", Icons.Default.Shield, Color(0xFF22C55E)),
                  AdvancedEngineItem("Heavy File Compactor", "Lossless block compaction for large datasets", Icons.Default.Compress, Color(0xFF8B5CF6))
                )

                engineItems.forEach { engine ->
                  Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardContainer),
                    border = BorderStroke(1.dp, cardBorder)
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                      Box(
                        modifier = Modifier
                          .size(38.dp)
                          .background(engine.color.copy(alpha = 0.18f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(
                          imageVector = engine.icon,
                          contentDescription = null,
                          tint = engine.color,
                          modifier = Modifier.size(22.dp)
                        )
                      }
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = engine.title,
                          style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = primaryTextColor
                          )
                        )
                        Text(
                          text = engine.description,
                          style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor)
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

      // ====================================================================
      // 5. SYSTEM UTILITIES USER FEATURES SECTION (100% Offline)
      // ====================================================================
      item {
        com.jackattackk246.files.ui.section.SystemUtilitiesCardSection(
          themeMode = themeMode,
          season = season,
          customAccentColor = customAccentColor,
          onTrashUpdated = {
            trashedCount = RecycleBinEngine.getItemCount()
          }
        )
      }

    } // LazyColumn
    } // PullToRefreshBox
  } // Box

  // Info Dialog
  if (showInfoDialog) {
    AlertDialog(
      onDismissRequest = { showInfoDialog = false },
      title = { Text("System Storage Information", color = primaryTextColor, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("Device Capacity: ${physicalMetrics.formattedTotal}", color = secondaryTextColor)
          Text("Available Free: ${physicalMetrics.formattedFree}", color = secondaryTextColor)
          Text("Used Storage: ${physicalMetrics.formattedUsed}", color = secondaryTextColor)
          Text("Display Mode: ${displayProfile.name}", color = secondaryTextColor)
          Text("Trashed Items: $trashedCount", color = secondaryTextColor)
        }
      },
      confirmButton = {
        Button(
          onClick = { showInfoDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black)
        ) {
          Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      },
      containerColor = Color(0xFF1C1D22),
      shape = RoundedCornerShape(16.dp)
    )
  }
}

private data class MediaHubItem(
  val label: String,
  val icon: ImageVector,
  val color: Color,
  val onClick: () -> Unit
)

private data class AdvancedEngineItem(
  val title: String,
  val description: String,
  val icon: ImageVector,
  val color: Color
)
