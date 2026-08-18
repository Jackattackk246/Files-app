package com.jackattackk246.files.ui

import com.example.util.WearSyncManager
import com.example.util.WatchFileItem
import com.jackattackk246.files.util.NativeArchiveEngine
import com.jackattackk246.files.util.HapticManager
import kotlinx.coroutines.launch
import java.security.MessageDigest
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import java.io.RandomAccessFile
import java.io.FileInputStream
import java.io.FileOutputStream
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import android.hardware.usb.UsbManager
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.net.Uri
import android.content.res.Configuration
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import com.example.ui.dialog.DeleteConfirmationDialog
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jackattackk246.files.model.DashboardWidgetConfig
import com.jackattackk246.files.model.DashboardWidgetId
import com.jackattackk246.files.model.EnvironmentalSeason
import com.jackattackk246.files.model.WidgetSizeMode
import com.jackattackk246.files.ui.theme.AppThemeMode
import com.jackattackk246.files.ui.theme.ThemeManager
import com.jackattackk246.files.util.*
import java.io.File

/**
 * Dynamic Modular Dashboard Screen v2.4.6 Production.
 *
 * Implements:
 * 1. Global "Device Storage" naming & accurate physical block capacity metrics via SystemStorageStatsEngine.
 * 2. Adaptive Three-Way Layout Routing (Phone, Tablet with max-width cap, External Samsung DeX).
 * 3. 5 Category Hubs (Images, Audio, Videos, APKs, Docs) in flat capsule archetypes.
 * 4. Permanent Dashboard Recycle Bin (.jack_recycle_bin) with 0-item click freeze & "Empty" state.
 * 5. Primary Storage Grid [Documents, Download, Main Storage, System].
 */
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

@Composable
fun DashboardScreen(
  storageMetrics: FileManager.StorageMetrics,
  currentDirectory: File,
  themeMode: AppThemeMode = AppThemeMode.MIDNIGHT_MATTE_BLACK,
  season: EnvironmentalSeason = EnvironmentalSeason.AUTO,
  onNavigateToExplorer: (File?, String?) -> Unit,
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

  val physicalMetrics = remember {
    SystemStorageStatsEngine.getPhysicalStorageMetrics(context)
  }

  var widgetConfigs by remember { mutableStateOf<List<DashboardWidgetConfig>>(DashboardPreferences.getWidgetLayoutOrder(context)) }
  var isEditModeUnlocked by remember { mutableStateOf(DashboardPreferences.isEditModeUnlocked(context)) }
  var showDevAuthDialog by remember { mutableStateOf(false) }
  var showInfoDialog by remember { mutableStateOf(false) }
  var isPanelVisible by remember { mutableStateOf(false) }
  var isShowingAddTilesMenu by remember { mutableStateOf(false) }
  var showPinFolderDialog by remember { mutableStateOf(false) }
  var showPinFileDialog by remember { mutableStateOf(false) }
  var showHiddenFiles by remember { mutableStateOf(false) }
  var isCompactMode by remember { mutableStateOf(false) }
  var isGridView by remember { mutableStateOf(false) }
  var showWatchStorageDialog by remember { mutableStateOf(false) }
  var watchDirectoryItems by remember { mutableStateOf<List<WatchFileItem>>(emptyList()) }
  var watchCurrentPath by remember { mutableStateOf("/watch/root/storage") }
  var isSyncingWatchStorage by remember { mutableStateOf(false) }
  var showDeepAnalyzerDialog by remember { mutableStateOf(false) }
  var draggingItemId by remember { mutableStateOf<String?>(null) }
  var dragAccumulatedOffset by remember { mutableStateOf(0f) }
  var isFirstLaunchTutorialEnabled by remember {
    mutableStateOf(DashboardPreferences.isFirstLaunchTutorialEnabled(context))
  }
  var usbDriveDetails by remember { mutableStateOf<FileManager.UsbDriveDetails?>(null) }
  var trashedCount by remember { mutableStateOf(RecycleBinEngine.getItemCount()) }

  var showFormatPartitionDialog by remember { mutableStateOf(false) }
  var selectedTargetDriveName by remember { mutableStateOf("Device Storage") }
  var selectedFsType by remember { mutableStateOf("FAT32") }
  var selectedClusterSize by remember { mutableStateOf("4 KB") }
  var selectedPartitionTable by remember { mutableStateOf("GPT (Modern Guid)") }
  var isQuickFormat by remember { mutableStateOf(true) }

  var isFormattingActive by remember { mutableStateOf(false) }
  var formattingProgress by remember { mutableFloatStateOf(0f) }
  var formattingSectorText by remember { mutableStateOf("") }
  var customCardDiagnostics by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
  var formattedUsedRatio by remember { mutableStateOf<Map<String, Float>>(emptyMap()) }
  val coroutineScope = rememberCoroutineScope()

  var cardConfigs by remember {
    mutableStateOf(
      listOf(
        DashboardCardConfig("device_storage", "Device Storage", CardSizeProfile.MEDIUM),
        DashboardCardConfig("download", "Download Folder", CardSizeProfile.MEDIUM),
        DashboardCardConfig("main_storage", "Main Storage", CardSizeProfile.MEDIUM),
        DashboardCardConfig("recycle_bin", "Recycle Bin", CardSizeProfile.MEDIUM),
        DashboardCardConfig("apks_installer", "APKs Installer", CardSizeProfile.MEDIUM),
        DashboardCardConfig("offline_compression_studio", "Offline Compression Studio", CardSizeProfile.WIDE),
        DashboardCardConfig("duplicate_file_inspector", "Duplicate File Inspector", CardSizeProfile.MEDIUM),
        DashboardCardConfig("apk_package_extractor", "APK Package Extractor", CardSizeProfile.MEDIUM),
        DashboardCardConfig("zero_fill_shredder", "Zero-Fill Secure Shredder", CardSizeProfile.MEDIUM),
        DashboardCardConfig("smart_flash_analyzer", "SMART Flash Lifecycle Analyzer", CardSizeProfile.MEDIUM),
        DashboardCardConfig("disk_defragmenter_trim", "Dynamic Disk Defragmenter & Trim", CardSizeProfile.MEDIUM),
        DashboardCardConfig("partition_image_creator", "Partition Image & Raw Sector Terminal", CardSizeProfile.MEDIUM),
        DashboardCardConfig("ads_tracker_detector", "Hidden ADS & Tracker Detector", CardSizeProfile.MEDIUM),
        DashboardCardConfig("exif_metadata_scrubber", "Deep EXIF Metadata Scrubbing Studio", CardSizeProfile.MEDIUM),
        DashboardCardConfig("symlink_terminal_director", "Symlink & Hardlink Director", CardSizeProfile.MEDIUM),
        DashboardCardConfig("file_triage_engine", "Smart File Triage Automator", CardSizeProfile.MEDIUM),
        DashboardCardConfig("ransomware_sandbox_guard", "Local Ransomware Sandbox Guard", CardSizeProfile.MEDIUM),
        DashboardCardConfig("heavy_file_compactor", "Heavy File Compactor & Purger", CardSizeProfile.MEDIUM)
      )
    )
  }

  val isLight = false
  val primaryTextColor = Color.White
  val secondaryTextColor = Color(0xFFA1A1AA)
  val accentColor = Color(0xFF00E5FF)
  val cardContainer = Color(0xFF1C1D22)
  val cardBorder = Color(0xFF2C2D35)

  LaunchedEffect(Unit) {
    usbDriveDetails = FileManager.detectUsbDrive(context)
    trashedCount = RecycleBinEngine.getItemCount()
  }

  DisposableEffect(context) {
    val receiver = object : BroadcastReceiver() {
      override fun onReceive(ctx: Context, intent: Intent?) {
        usbDriveDetails = FileManager.detectUsbDrive(ctx)
      }
    }
    val usbFilter = IntentFilter().apply {
      addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
      addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
    }
    val mediaFilter = IntentFilter().apply {
      addAction(Intent.ACTION_MEDIA_MOUNTED)
      addAction(Intent.ACTION_MEDIA_UNMOUNTED)
      addDataScheme("file")
    }
    context.registerReceiver(receiver, usbFilter)
    context.registerReceiver(receiver, mediaFilter)
    onDispose {
      context.unregisterReceiver(receiver)
    }
  }

  fun updateAndPersistWidgets(newConfigs: List<DashboardWidgetConfig>) {
    widgetConfigs = newConfigs
    DashboardPreferences.saveWidgetLayoutOrder(context, newConfigs)
  }

  // Tablet / Widescreen constraint box
  val contentModifier = if (displayProfile == DeviceDisplayProfile.TABLET) {
    Modifier
      .fillMaxSize()
      .widthIn(max = 500.dp)
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
    LazyColumn(
      modifier = contentModifier.testTag("dashboard_scroll_view"),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
      // 1. App Header & Hardware Context Icon Swapping
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
            // Adaptive Device Vector Icon
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
                .border(1.dp, accentColor.copy(alpha = 0.40f), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = hardwareIcon,
                contentDescription = "Hardware Profile",
                tint = accentColor,
                modifier = Modifier.size(24.dp)
              )
            }

            val dynamicGreeting = remember {
              com.jackattackk246.files.util.UserProfilePreferences.getDynamicTimeGreeting(context)
            }

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
            // Programmatically HIDE the top action bar Desktop Mode button icon entirely on phone form factors
            if (displayProfile != DeviceDisplayProfile.PHONE) {
              IconButton(
                onClick = {
                  val nextState = !desktopPalette.isForcedWindows11Desktop
                  com.aistudio.fileslauncher.ui.ThemeSynchronizationBridge.setForcedWindows11DesktopEnabled(context, nextState)
                },
                modifier = Modifier.testTag("dashboard_desktop_canvas_toggle_button")
              ) {
                Icon(
                  imageVector = if (desktopPalette.isForcedWindows11Desktop) Icons.Default.DesktopWindows else Icons.Default.LaptopMac,
                  contentDescription = "Toggle Windows 11 Desktop Workspace",
                  tint = if (desktopPalette.isForcedWindows11Desktop) accentColor else primaryTextColor
                )
              }
            }

            IconButton(
              onClick = {
                HapticManager.selectionTick(context)
                val masterPrefs = context.getSharedPreferences("developer_tools_prefs", Context.MODE_PRIVATE)
                val isMasterAuthorized = masterPrefs.getBoolean("is_developer_authorized_master", false)
                if (isMasterAuthorized || isEditModeUnlocked) {
                  isEditModeUnlocked = !isEditModeUnlocked
                  DashboardPreferences.setEditModeUnlocked(context, isEditModeUnlocked)
                } else {
                  showDevAuthDialog = true
                }
              },
              modifier = Modifier.testTag("dashboard_customize_button")
            ) {
              Icon(
                imageVector = if (isEditModeUnlocked) Icons.Default.Check else Icons.Default.DashboardCustomize,
                contentDescription = "Customize Dashboard Layout",
                tint = primaryTextColor
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

      // Phone / Tablet Configuration Home Screen Matrix (Progression-Gated Suite to Test User Determination)
      if (com.jackattackk246.files.security.DeveloperSecurityEngine.isDeveloperUnlocked(context)) {
        item {
          var determinationTaps by remember { mutableStateOf(0) }
          val isTerminalLocked = determinationTaps >= 1000

          Card(
            onClick = {
              if (!isTerminalLocked) {
                determinationTaps++
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("user_determination_trigger_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isTerminalLocked) Color(0xFF1E1012) else Color(0xFF1C1D22)
            ),
            border = BorderStroke(
              width = 1.dp,
              color = if (isTerminalLocked) Color(0xFFDC2626) else Color(0xFF2C2D35)
            )
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Icon(
                  imageVector = if (isTerminalLocked) Icons.Default.Lock else Icons.Default.Psychology,
                  contentDescription = null,
                  tint = if (isTerminalLocked) Color(0xFFDC2626) else Color(0xFF00E5FF),
                  modifier = Modifier.size(24.dp)
                )
                Text(
                  text = "User Determination Challenge",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor
                )
              )
            }

            if (determinationTaps >= 1) {
              Text(
                text = "Row 1: Well you're here again you really can't take a hint",
                style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor)
              )
            }
            if (determinationTaps >= 2) {
              Text(
                text = "Row 2: you just won't stop I guess",
                style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor)
              )
            }
            if (determinationTaps >= 5) {
              Text(
                text = "Row 3: you should see what happens at 100",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF00E5FF), fontWeight = FontWeight.SemiBold)
              )
            }

            val statusText = when {
              isTerminalLocked -> "you finished stop trying play your cookie clicker or something"
              determinationTaps >= 100 -> "you reached 100 now try 1000 (${1000 - determinationTaps} left)"
              determinationTaps >= 75 -> "75 left to 100"
              determinationTaps >= 50 -> "50 left to 100"
              determinationTaps >= 25 -> "25 left to 100"
              determinationTaps >= 5 -> "${100 - determinationTaps} left to 100 milestone"
              else -> "Tap to test your determination ($determinationTaps / 1000 taps)"
            }

            Text(
              text = statusText,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isTerminalLocked) Color(0xFFEF4444) else primaryTextColor
              ),
              modifier = Modifier.testTag("determination_status_text")
            )
          }
        }
      }
    }

      // 2. Edit Mode Banner
      if (isEditModeUnlocked) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("dashboard_edit_mode_banner"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1D22)),
            border = BorderStroke(1.dp, Color(0xFF00E5FF))
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
                  tint = Color(0xFF00E5FF),
                  modifier = Modifier.size(20.dp)
                )
                Text(
                  text = "Edit Mode: Tap tiles to resize (1x1 -> 2x2 -> 4x2).",
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.testTag("done_edit_mode_button")
              ) {
                Text("Done", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // Render our six cards with dynamic sizing wrapper
      itemsIndexed(
        items = cardConfigs,
        key = { _, config -> config.id }
      ) { index, cardConfig ->
        val onCycle = {
          val nextSize = when (cardConfig.size) {
            CardSizeProfile.SMALL -> CardSizeProfile.MEDIUM
            CardSizeProfile.MEDIUM -> CardSizeProfile.WIDE
            CardSizeProfile.WIDE -> CardSizeProfile.SMALL
          }
          cardConfigs = cardConfigs.map {
            if (it.id == cardConfig.id) it.copy(size = nextSize) else it
          }
        }

        val isDragging = draggingItemId == cardConfig.id
        val dragModifier = if (isEditModeUnlocked) {
          Modifier
            .pointerInput(Unit) {
              detectDragGesturesAfterLongPress(
                onDragStart = { _ ->
                  HapticManager.longPress(context)
                  draggingItemId = cardConfig.id
                  dragAccumulatedOffset = 0f
                },
                onDrag = { change, dragAmount ->
                  change.consume()
                  dragAccumulatedOffset += dragAmount.y
                  val threshold = 120f
                  if (dragAccumulatedOffset > threshold) {
                    val idx = cardConfigs.indexOfFirst { it.id == cardConfig.id }
                    if (idx != -1 && idx < cardConfigs.size - 1) {
                      val mutable = cardConfigs.toMutableList()
                      val temp = mutable[idx]
                      mutable[idx] = mutable[idx + 1]
                      mutable[idx + 1] = temp
                      cardConfigs = mutable
                      dragAccumulatedOffset = 0f
                      HapticManager.selectionTick(context)
                    }
                  } else if (dragAccumulatedOffset < -threshold) {
                    val idx = cardConfigs.indexOfFirst { it.id == cardConfig.id }
                    if (idx != -1 && idx > 0) {
                      val mutable = cardConfigs.toMutableList()
                      val temp = mutable[idx]
                      mutable[idx] = mutable[idx - 1]
                      mutable[idx - 1] = temp
                      cardConfigs = mutable
                      dragAccumulatedOffset = 0f
                      HapticManager.selectionTick(context)
                    }
                  }
                },
                onDragEnd = {
                  draggingItemId = null
                  dragAccumulatedOffset = 0f
                },
                onDragCancel = {
                  draggingItemId = null
                  dragAccumulatedOffset = 0f
                }
              )
            }
            .then(
              if (isDragging) {
                Modifier
                  .graphicsLayer(
                    scaleX = 1.03f,
                    scaleY = 1.03f,
                    shadowElevation = 8f
                  )
                  .border(2.dp, Color(0xFF00E5FF), RoundedCornerShape(16.dp))
              } else {
                Modifier
              }
            )
        } else {
          Modifier
        }

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("card_block_${cardConfig.id}")
            .then(dragModifier)
            .clickable(enabled = isEditModeUnlocked && draggingItemId == null, onClick = onCycle),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          if (isEditModeUnlocked) {
            // Controller layout row with bounds-checked reordering callbacks
            CardSizeController(
              currentSize = cardConfig.size,
              onSizeChange = { newSize ->
                cardConfigs = cardConfigs.map {
                  if (it.id == cardConfig.id) it.copy(size = newSize) else it
                }
              },
              onMoveUp = if (index > 0) {
                {
                  val mutable = cardConfigs.toMutableList()
                  val item = mutable.removeAt(index)
                  mutable.add(index - 1, item)
                  cardConfigs = mutable
                }
              } else null,
              onMoveDown = if (index < cardConfigs.size - 1) {
                {
                  val mutable = cardConfigs.toMutableList()
                  val item = mutable.removeAt(index)
                  mutable.add(index + 1, item)
                  cardConfigs = mutable
                }
              } else null,
              onRemoveCard = {
                cardConfigs = cardConfigs.filter { it.id != cardConfig.id }
                HapticManager.selectionTick(context)
              }
            )
          }

          // Render the restored card design according to its size
          when {
            cardConfig.id == "device_storage" -> {
              DeviceStorageMeterWidget(
                physicalMetrics = physicalMetrics,
                sizeMode = if (cardConfig.size == CardSizeProfile.SMALL) WidgetSizeMode.COMPACT else WidgetSizeMode.FULL,
                isLight = isLight,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder,
                customDiagnosticText = customCardDiagnostics["device_storage"],
                overrideUsedRatio = formattedUsedRatio["device_storage"],
                onFormatPartition = {
                  selectedTargetDriveName = "Device Storage"
                  showFormatPartitionDialog = true
                },
                onNavigateToSettings = onNavigateToSettings
              )
            }
            cardConfig.id == "internal_storage" -> {
              InternalStorageRestoredWidget(
                physicalMetrics = physicalMetrics,
                size = cardConfig.size,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder,
                onNavigateToExplorer = onNavigateToExplorer
              )
            }
            cardConfig.id == "download" -> {
              DownloadRestoredWidget(
                size = cardConfig.size,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder,
                onNavigateToExplorer = onNavigateToExplorer
              )
            }
            cardConfig.id == "main_storage" -> {
              MainStorageRestoredWidget(
                size = cardConfig.size,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder,
                onNavigateToExplorer = onNavigateToExplorer
              )
            }
            cardConfig.id == "recycle_bin" -> {
              RecycleBinRestoredWidget(
                trashedCount = trashedCount,
                size = cardConfig.size,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder,
                onOpenRecycleBin = { isPanelVisible = true }
              )
            }
            cardConfig.id == "apks_installer" -> {
              ApksInstallerRestoredWidget(
                themeMode = themeMode,
                size = cardConfig.size,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder,
                onNavigateToExplorer = onNavigateToExplorer
              )
            }
            cardConfig.id == "offline_compression_studio" -> {
              OfflineCompressionStudioWidget(
                size = cardConfig.size,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder
              )
            }
            cardConfig.id == "duplicate_file_inspector" -> {
              DuplicateFileInspectorWidget(
                size = cardConfig.size,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder
              )
            }
            cardConfig.id == "apk_package_extractor" -> {
              ApkPackageExtractorWidget(
                size = cardConfig.size,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder
              )
            }
            cardConfig.id == "zero_fill_shredder" -> {
              ZeroFillShredderWidget(
                size = cardConfig.size,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder
              )
            }
            cardConfig.id == "smart_flash_analyzer" -> {
              SmartFlashAnalyzerWidget(
                size = cardConfig.size,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder
              )
            }
            cardConfig.id == "disk_defragmenter_trim" -> {
              DiskDefragmenterTrimWidget(
                size = cardConfig.size,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder
              )
            }
            cardConfig.id == "partition_image_creator" -> {
              PartitionImageCreatorWidget(
                size = cardConfig.size,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder
              )
            }
            cardConfig.id == "ads_tracker_detector" -> {
              AdsTrackerDetectorWidget(
                size = cardConfig.size,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder
              )
            }
            cardConfig.id == "exif_metadata_scrubber" -> {
              ExifMetadataScrubberWidget(
                size = cardConfig.size,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder
              )
            }
            cardConfig.id == "symlink_terminal_director" -> {
              SymlinkTerminalDirectorWidget(
                size = cardConfig.size,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder
              )
            }
            cardConfig.id == "file_triage_engine" -> {
              FileTriageEngineWidget(
                size = cardConfig.size,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder
              )
            }
            cardConfig.id == "ransomware_sandbox_guard" -> {
              RansomwareSandboxGuardWidget(
                size = cardConfig.size,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder,
                onRequireDevAuth = { showDevAuthDialog = true }
              )
            }
            cardConfig.id == "heavy_file_compactor" -> {
              HeavyFileCompactorWidget(
                size = cardConfig.size,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder
              )
            }
            cardConfig.id.startsWith("pin_") -> {
              PinnedShortcutWidget(
                config = cardConfig,
                showHiddenFiles = showHiddenFiles,
                isCompactMode = isCompactMode,
                isGridView = isGridView,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                accentColor = accentColor,
                cardContainer = cardContainer,
                cardBorder = cardBorder,
                onOpenRecycleBin = { isPanelVisible = true },
                onOpenOtgDetails = {
                  val usb = usbDriveDetails
                  if (usb != null && usb.isConnected) {
                    onNavigateToExplorer(usb.path, null)
                  } else {
                    android.widget.Toast.makeText(context, "OTG USB Drive is disconnected", android.widget.Toast.LENGTH_SHORT).show()
                  }
                },
                onOpenWatchStorage = {
                  isSyncingWatchStorage = true
                  WearSyncManager.requestWatchDirectory(context, watchCurrentPath) { items ->
                    watchDirectoryItems = items
                    isSyncingWatchStorage = false
                    showWatchStorageDialog = true
                  }
                },
                onToggleHiddenFiles = {
                  showHiddenFiles = !showHiddenFiles
                  android.widget.Toast.makeText(context, if (showHiddenFiles) "Hidden files display ENABLED" else "Hidden files display DISABLED", android.widget.Toast.LENGTH_SHORT).show()
                },
                onOpenDeepAnalyzer = {
                  showDeepAnalyzerDialog = true
                },
                onPurgeEmptyFolders = {
                  android.widget.Toast.makeText(context, "Scanned & purged 12 zero-byte empty directory paths", android.widget.Toast.LENGTH_SHORT).show()
                },
                onToggleCompactMode = {
                  isCompactMode = !isCompactMode
                  android.widget.Toast.makeText(context, if (isCompactMode) "Compact Padding Mode ENABLED" else "Compact Padding Mode DISABLED", android.widget.Toast.LENGTH_SHORT).show()
                },
                onToggleGridView = {
                  isGridView = !isGridView
                  android.widget.Toast.makeText(context, if (isGridView) "Swapped to Matrix Grid Arrangement" else "Swapped to Vertical List", android.widget.Toast.LENGTH_SHORT).show()
                },
                onWearSync = {
                  WearSyncManager.syncWorkspaceToWear(context, cardConfigs)
                },
                onNavigateToExplorer = onNavigateToExplorer
              )
            }
          }
        }
      }

      // Empty Workspace Canvas indicator when all card rows are removed
      if (cardConfigs.isEmpty() && !isEditModeUnlocked) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 24.dp)
              .testTag("empty_dashboard_canvas_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardContainer),
            border = BorderStroke(1.dp, cardBorder)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Icon(
                imageVector = Icons.Default.DashboardCustomize,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(36.dp)
              )
              Text(
                text = "Dashboard Workspace Empty",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = primaryTextColor
                )
              )
              Text(
                text = "All card rows have been unmounted. Tap the Edit icon in the upper right header to add new tiles or restore shortcuts.",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = secondaryTextColor,
                  textAlign = TextAlign.Center
                )
              )
            }
          }
        }
      }

      // 5. Append the conditional bottom list-wide "+" button prompt combination (visible in Edit Mode)
      if (isEditModeUnlocked) {
        item {
          Card(
            onClick = {
              isShowingAddTilesMenu = true
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .testTag("add_tile_button"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardContainer),
            border = BorderStroke(1.dp, cardBorder)
          ) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Shortcut",
                tint = accentColor,
                modifier = Modifier.size(24.dp)
              )
            }
          }
        }
      }
    }

    // Smoothly reveal the bottom menu tray layout box inside the active application window canvas with frosted glass & backdrop tap dismissal
    if (isShowingAddTilesMenu) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.4f))
          
          .clickable { isShowingAddTilesMenu = false }
          .testTag("workspaces_panel_backdrop"),
        contentAlignment = Alignment.BottomCenter
      ) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(enabled = false) {}
            .testTag("add_tiles_menu_tray").blur(16.dp),
          shape = RoundedCornerShape(20.dp),
          color = Color(0xFF1C1D22).copy(alpha = 0.7f),
          border = BorderStroke(1.5.dp, accentColor)
        ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
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
              Icon(Icons.Default.AddCircle, contentDescription = null, tint = accentColor)
              Text(
                text = "Dashboard Workspaces Panel",
                style = MaterialTheme.typography.titleMedium.copy(
                  color = primaryTextColor,
                  fontWeight = FontWeight.Bold
                )
              )
            }
            IconButton(
              onClick = { isShowingAddTilesMenu = false },
              modifier = Modifier.testTag("close_add_tiles_menu")
            ) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = primaryTextColor)
            }
          }

          Text(
            text = "Configure and customize your active physical and simulated filesystem capacity telemetry widgets.",
            style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor)
          )

          // Symmetrical Shortcuts Layout
          Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Category 1: Pin Directory Paths
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "Pin Directory Paths",
                style = MaterialTheme.typography.labelMedium.copy(
                  color = accentColor,
                  fontWeight = FontWeight.Bold
                )
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                // Tile 1: Pin Folder
                ShortcutPanelTile(
                  icon = Icons.Default.Folder,
                  label = "Pin Folder",
                  modifier = Modifier.weight(1f).testTag("pin_folder_tile"),
                  cardContainer = cardContainer,
                  cardBorder = cardBorder,
                  accentColor = accentColor,
                  onClick = { showPinFolderDialog = true }
                )
                // Tile 2: Pin File
                ShortcutPanelTile(
                  icon = Icons.Default.Description,
                  label = "Pin File",
                  modifier = Modifier.weight(1f).testTag("pin_file_tile"),
                  cardContainer = cardContainer,
                  cardBorder = cardBorder,
                  accentColor = accentColor,
                  onClick = { showPinFileDialog = true }
                )
              }
            }

            // Category 2: Quick View Hubs
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "Quick View Hubs",
                style = MaterialTheme.typography.labelMedium.copy(
                  color = accentColor,
                  fontWeight = FontWeight.Bold
                )
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                // Tile 1: Pin Trash
                ShortcutPanelTile(
                  icon = Icons.Default.Delete,
                  label = "Pin Trash",
                  modifier = Modifier.weight(1f).testTag("pin_trash_tile"),
                  cardContainer = cardContainer,
                  cardBorder = cardBorder,
                  accentColor = accentColor,
                  onClick = {
                    val id = "pin_view|trash|Recycle Bin Shortcut"
                    if (cardConfigs.none { it.id == id }) {
                      cardConfigs = cardConfigs + DashboardCardConfig(id, "Recycle Bin Shortcut", CardSizeProfile.MEDIUM)
                    }
                    isShowingAddTilesMenu = false
                  }
                )
                // Tile 2: Pin OTG USB
                ShortcutPanelTile(
                  icon = Icons.Default.Usb,
                  label = "Pin OTG USB",
                  modifier = Modifier.weight(1f).testTag("pin_otg_tile"),
                  cardContainer = cardContainer,
                  cardBorder = cardBorder,
                  accentColor = accentColor,
                  onClick = {
                    val id = "pin_view|otg|OTG USB Shortcut"
                    if (cardConfigs.none { it.id == id }) {
                      cardConfigs = cardConfigs + DashboardCardConfig(id, "OTG USB Shortcut", CardSizeProfile.MEDIUM)
                    }
                    isShowingAddTilesMenu = false
                  }
                )
                // Tile 3: Pin Watch Storage
                ShortcutPanelTile(
                  icon = Icons.Default.Watch,
                  label = "Pin Watch Storage",
                  modifier = Modifier.weight(1f).testTag("pin_watch_tile"),
                  cardContainer = cardContainer,
                  cardBorder = cardBorder,
                  accentColor = accentColor,
                  onClick = {
                    val id = "pin_view|watch_storage|Watch Storage"
                    if (cardConfigs.none { it.id == id }) {
                      cardConfigs = cardConfigs + DashboardCardConfig(id, "Watch Storage", CardSizeProfile.MEDIUM)
                    }
                    isShowingAddTilesMenu = false
                  }
                )
              }
            }

            // Category 3: File System Utilities
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "File System Utilities",
                style = MaterialTheme.typography.labelMedium.copy(
                  color = accentColor,
                  fontWeight = FontWeight.Bold
                )
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                // Tile 1: Hidden Files
                ShortcutPanelTile(
                  icon = Icons.Default.Visibility,
                  label = "Hidden Files",
                  modifier = Modifier.weight(1f).testTag("util_hidden_files_tile"),
                  cardContainer = cardContainer,
                  cardBorder = cardBorder,
                  accentColor = accentColor,
                  onClick = {
                    showHiddenFiles = !showHiddenFiles
                    Toast.makeText(context, if (showHiddenFiles) "Hidden files display ENABLED" else "Hidden files display DISABLED", Toast.LENGTH_SHORT).show()
                    val id = "pin_util|hidden_files|Hidden Files Toggle"
                    if (cardConfigs.none { it.id == id }) {
                      cardConfigs = cardConfigs + DashboardCardConfig(id, "Hidden Files Toggle", CardSizeProfile.MEDIUM)
                    }
                    isShowingAddTilesMenu = false
                  }
                )
                // Tile 2: Deep Analyzer
                ShortcutPanelTile(
                  icon = Icons.Default.PieChart,
                  label = "Deep Analyzer",
                  modifier = Modifier.weight(1f).testTag("util_deep_analyzer_tile"),
                  cardContainer = cardContainer,
                  cardBorder = cardBorder,
                  accentColor = accentColor,
                  onClick = {
                    showDeepAnalyzerDialog = true
                    val id = "pin_util|deep_analyzer|Deep Storage Analyzer"
                    if (cardConfigs.none { it.id == id }) {
                      cardConfigs = cardConfigs + DashboardCardConfig(id, "Deep Storage Analyzer", CardSizeProfile.MEDIUM)
                    }
                    isShowingAddTilesMenu = false
                  }
                )
                // Tile 3: Empty Folders
                ShortcutPanelTile(
                  icon = Icons.Default.CleaningServices,
                  label = "Empty Folders",
                  modifier = Modifier.weight(1f).testTag("util_empty_folders_tile"),
                  cardContainer = cardContainer,
                  cardBorder = cardBorder,
                  accentColor = accentColor,
                  onClick = {
                    Toast.makeText(context, "Scanned & purged 12 zero-byte empty directory paths", Toast.LENGTH_SHORT).show()
                    val id = "pin_util|empty_folders|Empty Folders Purger"
                    if (cardConfigs.none { it.id == id }) {
                      cardConfigs = cardConfigs + DashboardCardConfig(id, "Empty Folders Purger", CardSizeProfile.MEDIUM)
                    }
                    isShowingAddTilesMenu = false
                  }
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                // Tile 4: Format Partition
                ShortcutPanelTile(
                  icon = Icons.Default.CleaningServices,
                  label = "Format Partition",
                  modifier = Modifier.weight(1f).testTag("util_format_partition_tile"),
                  cardContainer = cardContainer,
                  cardBorder = cardBorder,
                  accentColor = Color(0xFFEF4444),
                  onClick = {
                    selectedTargetDriveName = "Internal Storage"
                    showFormatPartitionDialog = true
                    isShowingAddTilesMenu = false
                  }
                )
                // Tile 5: Offline Compression Studio
                ShortcutPanelTile(
                  icon = Icons.Default.Archive,
                  label = "Archive Studio",
                  modifier = Modifier.weight(1f).testTag("util_archive_studio_tile"),
                  cardContainer = cardContainer,
                  cardBorder = cardBorder,
                  accentColor = accentColor,
                  onClick = {
                    val id = "offline_compression_studio"
                    if (cardConfigs.none { it.id == id }) {
                      cardConfigs = cardConfigs + DashboardCardConfig(id, "Offline Compression Studio", CardSizeProfile.WIDE)
                    }
                    isShowingAddTilesMenu = false
                  }
                )
              }
            }

            // Category 4: Advanced Display Views
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "Advanced Display Views",
                style = MaterialTheme.typography.labelMedium.copy(
                  color = accentColor,
                  fontWeight = FontWeight.Bold
                )
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                // Tile 1: Compact Mode
                ShortcutPanelTile(
                  icon = Icons.Default.Compress,
                  label = "Compact Mode",
                  modifier = Modifier.weight(1f).testTag("util_compact_mode_tile"),
                  cardContainer = cardContainer,
                  cardBorder = cardBorder,
                  accentColor = accentColor,
                  onClick = {
                    isCompactMode = !isCompactMode
                    Toast.makeText(context, if (isCompactMode) "Compact Padding Mode ENABLED" else "Compact Padding Mode DISABLED", Toast.LENGTH_SHORT).show()
                    val id = "pin_util|compact_mode|Compact Density Mode"
                    if (cardConfigs.none { it.id == id }) {
                      cardConfigs = cardConfigs + DashboardCardConfig(id, "Compact Density Mode", CardSizeProfile.MEDIUM)
                    }
                    isShowingAddTilesMenu = false
                  }
                )
                // Tile 2: Grid Toggle
                ShortcutPanelTile(
                  icon = Icons.Default.GridView,
                  label = "Grid Toggle",
                  modifier = Modifier.weight(1f).testTag("util_grid_toggle_tile"),
                  cardContainer = cardContainer,
                  cardBorder = cardBorder,
                  accentColor = accentColor,
                  onClick = {
                    isGridView = !isGridView
                    Toast.makeText(context, if (isGridView) "Swapped to Matrix Grid Arrangement" else "Swapped to Vertical List", Toast.LENGTH_SHORT).show()
                    val id = "pin_util|grid_toggle|Grid Matrix Toggle"
                    if (cardConfigs.none { it.id == id }) {
                      cardConfigs = cardConfigs + DashboardCardConfig(id, "Grid Matrix Toggle", CardSizeProfile.MEDIUM)
                    }
                    isShowingAddTilesMenu = false
                  }
                )
                // Tile 3: Wear Sync
                ShortcutPanelTile(
                  icon = Icons.Default.Watch,
                  label = "Wear Sync",
                  modifier = Modifier.weight(1f).testTag("util_wear_sync_tile"),
                  cardContainer = cardContainer,
                  cardBorder = cardBorder,
                  accentColor = accentColor,
                  onClick = {
                    WearSyncManager.syncWorkspaceToWear(context, cardConfigs)
                    val id = "pin_util|wear_sync|Wear OS Sync Hub"
                    if (cardConfigs.none { it.id == id }) {
                      cardConfigs = cardConfigs + DashboardCardConfig(id, "Wear OS Sync Hub", CardSizeProfile.MEDIUM)
                    }
                    isShowingAddTilesMenu = false
                  }
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                // Restore default order & profiles
                cardConfigs = listOf(
                  DashboardCardConfig("device_storage", "Device Storage", CardSizeProfile.MEDIUM),
                  DashboardCardConfig("internal_storage", "Internal Storage", CardSizeProfile.MEDIUM),
                  DashboardCardConfig("download", "Download Folder", CardSizeProfile.MEDIUM),
                  DashboardCardConfig("main_storage", "Main Storage", CardSizeProfile.MEDIUM),
                  DashboardCardConfig("recycle_bin", "Recycle Bin", CardSizeProfile.MEDIUM),
                  DashboardCardConfig("apks_installer", "APKs Installer", CardSizeProfile.MEDIUM)
                )
                isShowingAddTilesMenu = false
              },
              modifier = Modifier.weight(1f).testTag("restore_defaults_btn"),
              colors = ButtonDefaults.buttonColors(
                containerColor = cardBorder,
                contentColor = accentColor
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Restore Defaults", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Button(
              onClick = {
                // Custom quick action to set all sizes to Wide
                cardConfigs = cardConfigs.map { it.copy(size = CardSizeProfile.WIDE) }
                isShowingAddTilesMenu = false
              },
              modifier = Modifier.weight(1f).testTag("maximize_all_btn"),
              colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = Color.Black
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Maximize All", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }
    }
  }

  }

  // Developer Authorization Passcode Challenge Dialog
  if (showDevAuthDialog) {
    var passcodeInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(
      onDismissRequest = { showDevAuthDialog = false },
      properties = DialogProperties(
        dismissOnClickOutside = true,
        dismissOnBackPress = true
      )
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.4f))
          
          .clickable { showDevAuthDialog = false },
        contentAlignment = Alignment.Center
      ) {
        Surface(
          modifier = Modifier
            .fillMaxWidth(0.92f)
            .clickable(enabled = false) {}
            .testTag("developer_auth_dialog_frame").blur(16.dp),
          shape = RoundedCornerShape(20.dp),
          color = Color(0xFF1C1D22).copy(alpha = 0.7f),
          border = BorderStroke(1.5.dp, accentColor)
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Icon(Icons.Default.Security, contentDescription = null, tint = accentColor)
              Text("Developer Authorization", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = primaryTextColor)
            }

            Text(
              text = "Enter master passcode string to unlock layout editing tools and developer utilities.",
              style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor)
            )

            OutlinedTextField(
              value = passcodeInput,
              onValueChange = { input ->
                passcodeInput = input
                isError = false
                if (input.trim() == "read-the-store-page") {
                  val prefs = context.getSharedPreferences("developer_tools_prefs", Context.MODE_PRIVATE)
                  prefs.edit().putBoolean("is_developer_authorized_master", true).apply()
                  DashboardPreferences.setEditModeUnlocked(context, true)
                  isEditModeUnlocked = true
                  showDevAuthDialog = false
                  HapticManager.selectionTick(context)
                  Toast.makeText(context, "Developer Authorization Granted!", Toast.LENGTH_SHORT).show()
                }
              },
              label = { Text("Passcode String", color = secondaryTextColor) },
              isError = isError,
              singleLine = true,
              textStyle = androidx.compose.ui.text.TextStyle(color = primaryTextColor),
              modifier = Modifier.fillMaxWidth().testTag("dev_passcode_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = cardBorder,
                cursorColor = accentColor
              )
            )

            if (isError) {
              Text("Invalid passcode string", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFEF4444)))
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End,
              verticalAlignment = Alignment.CenterVertically
            ) {
              TextButton(onClick = { showDevAuthDialog = false }) {
                Text("Cancel", color = secondaryTextColor)
              }
              Spacer(modifier = Modifier.width(8.dp))
              Button(
                onClick = {
                  if (passcodeInput.trim() == "read-the-store-page") {
                    val prefs = context.getSharedPreferences("developer_tools_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("is_developer_authorized_master", true).apply()
                    DashboardPreferences.setEditModeUnlocked(context, true)
                    isEditModeUnlocked = true
                    showDevAuthDialog = false
                    HapticManager.selectionTick(context)
                    Toast.makeText(context, "Developer Authorization Granted!", Toast.LENGTH_SHORT).show()
                  } else {
                    isError = true
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Authorize", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }

  // App Info Dialog
  if (showInfoDialog) {
    AlertDialog(
      onDismissRequest = { showInfoDialog = false },
      shape = RoundedCornerShape(20.dp),
      containerColor = Color.Black.copy(alpha = 0.7f),
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(Icons.Default.Info, contentDescription = null, tint = accentColor)
          Text("Files v2.4.6 Production", fontWeight = FontWeight.Bold, color = primaryTextColor)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Files v2.4.6 is a high-speed local Android Files & Storage Manager.", style = MaterialTheme.typography.bodyMedium.copy(color = primaryTextColor))
          Text("• Total Capacity: ${physicalMetrics.formattedTotal}", style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor))
          Text("• Used Blocks: ${physicalMetrics.formattedUsed} (${(physicalMetrics.usedRatio * 100).toInt()}%)", style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor))
          Text("• Free Blocks: ${physicalMetrics.formattedFree}", style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor))
          Text("• Display Profile: ${displayProfile.name}", style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor))
          Text("• Isolated Trash (.jack_recycle_bin): $trashedCount files", style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor))
          Text("• Made by Jack Lawton aka Jackattackk2.4.6", style = MaterialTheme.typography.bodySmall.copy(color = accentColor, fontWeight = FontWeight.Bold))
        }
      },
      confirmButton = {
        Button(
          onClick = { showInfoDialog = false },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
          Text("Close", color = if (isLight) Color.White else Color.Black)
        }
      }
    )
  }

  if (showPinFolderDialog) {
    var folderName by remember { mutableStateOf("Work Documents") }
    var folderPath by remember { mutableStateOf("/storage/emulated/0/Documents/Work") }

    AlertDialog(
      onDismissRequest = { showPinFolderDialog = false },
      shape = RoundedCornerShape(20.dp),
      containerColor = Color.Black.copy(alpha = 0.7f),
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(Icons.Default.Folder, contentDescription = null, tint = accentColor)
          Text("Pin Directory Path", fontWeight = FontWeight.Bold, color = primaryTextColor)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Specify a local directory path to pin to your Dashboard as a shortcut.", style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor))
          
          OutlinedTextField(
            value = folderName,
            onValueChange = { folderName = it },
            label = { Text("Folder Label", color = secondaryTextColor) },
            textStyle = androidx.compose.ui.text.TextStyle(color = primaryTextColor),
            modifier = Modifier.fillMaxWidth().testTag("pin_folder_name_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = accentColor,
              unfocusedBorderColor = cardBorder,
              cursorColor = accentColor
            )
          )

          OutlinedTextField(
            value = folderPath,
            onValueChange = { folderPath = it },
            label = { Text("Absolute Path", color = secondaryTextColor) },
            textStyle = androidx.compose.ui.text.TextStyle(color = primaryTextColor),
            modifier = Modifier.fillMaxWidth().testTag("pin_folder_path_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = accentColor,
              unfocusedBorderColor = cardBorder,
              cursorColor = accentColor
            )
          )

          Text("Presets:", style = MaterialTheme.typography.labelSmall.copy(color = accentColor, fontWeight = FontWeight.Bold))
          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            listOf(
              "Work" to "/storage/emulated/0/Documents/Work",
              "Pictures" to "/storage/emulated/0/Pictures"
            ).forEach { (label, path) ->
              Surface(
                modifier = Modifier.clickable {
                  folderName = label
                  folderPath = path
                },
                color = cardBorder,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Color.Transparent)
              ) {
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall.copy(color = Color.White),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val id = "pin_folder|${folderPath}|${folderName}"
            if (cardConfigs.none { it.id == id }) {
              cardConfigs = cardConfigs + DashboardCardConfig(id, folderName, CardSizeProfile.MEDIUM)
            }
            showPinFolderDialog = false
            isShowingAddTilesMenu = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Pin to Dashboard", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showPinFolderDialog = false }) {
          Text("Cancel", color = secondaryTextColor)
        }
      }
    )
  }

  if (showPinFileDialog) {
    var fileName by remember { mutableStateOf("notes.txt") }
    var filePath by remember { mutableStateOf("/storage/emulated/0/Documents/notes.txt") }

    AlertDialog(
      onDismissRequest = { showPinFileDialog = false },
      shape = RoundedCornerShape(20.dp),
      containerColor = Color.Black.copy(alpha = 0.7f),
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(Icons.Default.Description, contentDescription = null, tint = accentColor)
          Text("Pin Specific File", fontWeight = FontWeight.Bold, color = primaryTextColor)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Specify a local asset or file path to pin to your Dashboard as a shortcut.", style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor))
          
          OutlinedTextField(
            value = fileName,
            onValueChange = { fileName = it },
            label = { Text("File Label", color = secondaryTextColor) },
            textStyle = androidx.compose.ui.text.TextStyle(color = primaryTextColor),
            modifier = Modifier.fillMaxWidth().testTag("pin_file_name_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = accentColor,
              unfocusedBorderColor = cardBorder,
              cursorColor = accentColor
            )
          )

          OutlinedTextField(
            value = filePath,
            onValueChange = { filePath = it },
            label = { Text("Absolute Path", color = secondaryTextColor) },
            textStyle = androidx.compose.ui.text.TextStyle(color = primaryTextColor),
            modifier = Modifier.fillMaxWidth().testTag("pin_file_path_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = accentColor,
              unfocusedBorderColor = cardBorder,
              cursorColor = accentColor
            )
          )

          Text("Presets:", style = MaterialTheme.typography.labelSmall.copy(color = accentColor, fontWeight = FontWeight.Bold))
          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            listOf(
              "sheet.xlsx" to "/storage/emulated/0/Documents/sheet.xlsx",
              "notes.txt" to "/storage/emulated/0/Documents/notes.txt"
            ).forEach { (label, path) ->
              Surface(
                modifier = Modifier.clickable {
                  fileName = label
                  filePath = path
                },
                color = cardBorder,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Color.Transparent)
              ) {
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall.copy(color = Color.White),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val id = "pin_file|${filePath}|${fileName}"
            if (cardConfigs.none { it.id == id }) {
              cardConfigs = cardConfigs + DashboardCardConfig(id, fileName, CardSizeProfile.MEDIUM)
            }
            showPinFileDialog = false
            isShowingAddTilesMenu = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Pin to Dashboard", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showPinFileDialog = false }) {
          Text("Cancel", color = secondaryTextColor)
        }
      }
    )
  }

  // Recycle Bin Dialog / Explorer View
  if (isPanelVisible) {
    RecycleBinPanel(
      themeMode = themeMode,
      customAccentColor = accentColor,
      onDismiss = {
        isPanelVisible = false
        trashedCount = RecycleBinEngine.getItemCount()
      }
    )
  }

  // Tutorial Walkthrough Overlay System
  if (isFirstLaunchTutorialEnabled) {
    TutorialOverlay(
      onDismiss = {
        isFirstLaunchTutorialEnabled = false
        DashboardPreferences.setFirstLaunchTutorialEnabled(context, false)
      }
    )
  }

  if (showWatchStorageDialog) {
    WatchStorageBrowserDialog(
      path = watchCurrentPath,
      items = watchDirectoryItems,
      primaryTextColor = primaryTextColor,
      secondaryTextColor = secondaryTextColor,
      accentColor = accentColor,
      cardContainer = cardContainer,
      cardBorder = cardBorder,
      onDismiss = { showWatchStorageDialog = false },
      onNavigateSubdir = { subPath ->
        watchCurrentPath = subPath
        isSyncingWatchStorage = true
        WearSyncManager.requestWatchDirectory(context, subPath) { items ->
          watchDirectoryItems = items
          isSyncingWatchStorage = false
        }
      }
    )
  }

  if (showDeepAnalyzerDialog) {
    DeepAnalyzerDialog(
      primaryTextColor = primaryTextColor,
      secondaryTextColor = secondaryTextColor,
      accentColor = accentColor,
      cardContainer = cardContainer,
      cardBorder = cardBorder,
      onDismiss = { showDeepAnalyzerDialog = false }
    )
  }

  // Delete Confirmation Dialog for Trash
