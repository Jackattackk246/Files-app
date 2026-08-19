package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jackattackk246.files.model.EnvironmentalSeason
import com.jackattackk246.files.model.FileItem
import com.jackattackk246.files.ui.theme.AppThemeMode
import com.jackattackk246.files.ui.theme.ThemeManager
import com.jackattackk246.files.util.*
import java.io.File

@Composable
fun NearbyDevicesScreen(
  themeMode: AppThemeMode,
  customAccentColor: Color? = null,
  season: EnvironmentalSeason = EnvironmentalSeason.AUTO,
  onNavigateBack: () -> Unit = {},
  onStreamMediaUrl: (FileItem) -> Unit = {}
) {
  val context = LocalContext.current
  val isLight = ThemeManager.isLightBackgroundProfile(themeMode, season)
  val primaryTextColor = ThemeManager.getAdaptivePrimaryTextColor(themeMode, season)
  val secondaryTextColor = ThemeManager.getAdaptiveSecondaryTextColor(themeMode, season)
  val accentColor = ThemeManager.getThemeAccentColor(themeMode, customAccentColor)
  val cardContainer = ThemeManager.getAdaptiveCardContainerColor(themeMode, season)
  val cardBorder = ThemeManager.getAdaptiveCardBorderColor(themeMode, season)

  // 1. Profile State Checks (Bypass identity prompts if global name exists)
  val globalProfileName = remember { UserProfilePreferences.getRawUserName(context) }
  val hasGlobalProfile = globalProfileName.isNotBlank()

  var sessionName by remember { mutableStateOf(if (hasGlobalProfile) globalProfileName else "") }
  var showNamePromptDialog by remember { mutableStateOf(!hasGlobalProfile && sessionName.isBlank()) }
  var tempNameInput by remember { mutableStateOf("") }

  LaunchedEffect(globalProfileName, hasGlobalProfile) {
    if (hasGlobalProfile) {
      NearbyDevicesEngine.setSessionTemporaryName(globalProfileName)
    }
  }

  // 2. Battery Safeguard Check (15% or lower)
  val currentBatteryLevel = remember { NearbyDevicesEngine.getBatteryLevel(context) }
  var showBatteryWarningDialog by remember { mutableStateOf(currentBatteryLevel <= 15) }

  // Blocked Devices View State
  var showBlockedDevicesDialog by remember { mutableStateOf(false) }

  // Active Session and Remote Explorer State
  val isSessionActive by NearbyDevicesEngine.isSessionActive.collectAsState()
  val activePeer by NearbyDevicesEngine.activePeer.collectAsState()
  val discoveredPeers by NearbyDevicesEngine.discoveredPeers.collectAsState()
  val incomingRequest by NearbyDevicesEngine.incomingRequest.collectAsState()
  val statusMessage by NearbyDevicesEngine.statusMessage.collectAsState()
  val remoteCurrentPath by NearbyDevicesEngine.remoteCurrentPath.collectAsState()
  val remoteFileList by NearbyDevicesEngine.remoteFileList.collectAsState()
  val remoteMetrics by NearbyDevicesEngine.remoteStorageMetrics.collectAsState()
  val blockedList by NearbyDevicesEngine.blockedDevices.collectAsState()
  val localBatteryLevel by NearbyDevicesEngine.localBatteryLevel.collectAsState()
  val remoteBatteryLevel by NearbyDevicesEngine.remoteBatteryLevel.collectAsState()
  val syncIntervalMs by NearbyDevicesEngine.syncIntervalMs.collectAsState()

  var showSyncIntervalDialog by remember { mutableStateOf(false) }

  var connectingPeerToken by remember { mutableStateOf<String?>(null) }
  var remoteRenameTarget by remember { mutableStateOf<NearbyDevicesEngine.RemoteFileItem?>(null) }
  var newRenameInput by remember { mutableStateOf("") }
  var isCreateFolderDialogOpen by remember { mutableStateOf(false) }
  var newFolderInput by remember { mutableStateOf("") }

  // LifeCycle: Open menu socket discovery
  DisposableEffect(Unit) {
    NearbyDevicesEngine.onNearbyMenuOpened(context)
    onDispose {
      NearbyDevicesEngine.onNearbyMenuClosed()
    }
  }

  // Pulsing scan radar animation
  val infiniteTransition = rememberInfiniteTransition(label = "nearby_radar")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_alpha"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .testTag("nearby_devices_screen")
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Header Banner & Profile Identifier
      item {
        Surface(
          color = cardContainer,
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(1.dp, cardBorder),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("nearby_header_card")
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.CastConnected,
                    contentDescription = "Nearby Devices",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                  )
                }
                Column {
                  Text(
                    text = "Nearby Devices",
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = primaryTextColor
                    )
                  )
                  Text(
                    text = "Device Profile: ${sessionName.ifBlank { "Temporary Session" }}",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = secondaryTextColor
                    )
                  )
                }
              }

              // Active Signal Status Badge
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isSessionActive) accentColor else accentColor.copy(alpha = pulseAlpha))
                )
                Text(
                  text = if (isSessionActive) "Connected" else "Broadcasting",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSessionActive) accentColor else secondaryTextColor
                  )
                )
              }
            }

            Text(
              text = "Wirelessly stream media, browse, copy, and edit files over local Wi-Fi without internet or cloud servers. Session remains open while the app is active.",
              style = MaterialTheme.typography.bodySmall.copy(
                color = secondaryTextColor,
                lineHeight = 18.sp
              )
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Status Pill
              Surface(
                color = if (isLight) Color(0x10000000) else Color(0x20FFFFFF),
                shape = RoundedCornerShape(8.dp)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                  )
                  Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.labelSmall.copy(color = primaryTextColor)
                  )
                }
              }

              Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Sync Interval Selector Trigger
                val activeIntervalLabel = NearbyDevicesEngine.SyncInterval.entries.firstOrNull { it.ms == syncIntervalMs }?.label ?: "${syncIntervalMs / 1000}s"
                TextButton(
                  onClick = {
                    HapticManager.selectionTick(context)
                    showSyncIntervalDialog = true
                  },
                  modifier = Modifier.testTag("open_sync_interval_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Sync: $activeIntervalLabel",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = accentColor,
                      fontWeight = FontWeight.Bold
                    )
                  )
                }

                // Blocked Devices Sub-Menu Trigger
                TextButton(
                  onClick = {
                    HapticManager.selectionTick(context)
                    showBlockedDevicesDialog = true
                  },
                  modifier = Modifier.testTag("open_blocked_devices_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    tint = secondaryTextColor,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Blocked (${blockedList.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = secondaryTextColor,
                      fontWeight = FontWeight.Bold
                    )
                  )
                }
              }
            }
          }
        }
      }

      // 2. Active Session Card with 'End Connection' Action
      if (isSessionActive && activePeer != null) {
        item {
          Surface(
            color = cardContainer,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, accentColor),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("active_nearby_session_card")
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
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
                  Icon(
                    imageVector = Icons.Default.Smartphone,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                  )
                  Column {
                    Text(
                      text = activePeer!!.deviceName,
                      style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor
                      )
                    )
                    Text(
                      text = "${activePeer!!.deviceModel} • ${activePeer!!.ipAddress}",
                      style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
                    )
                  }
                }

                // Rule 4: Prominent Solid Crimson Red 'End Connection' Action Button
                Button(
                  onClick = {
                    HapticManager.errorPulse(context)
                    NearbyDevicesEngine.endSession()
                    Toast.makeText(context, "Nearby Devices session ended", Toast.LENGTH_SHORT).show()
                  },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                  ),
                  shape = RoundedCornerShape(10.dp),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("end_connection_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "End Connection",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("End Connection", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
              }

              // Remote Storage Meter Display
              remoteMetrics?.let { metrics ->
                HorizontalDivider(color = cardBorder)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = "Remote Storage Capacity",
                      style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
                    )
                    Text(
                      text = "${metrics.usedFormatted} used of ${metrics.totalFormatted}",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = primaryTextColor)
                    )
                  }
                  LinearProgressIndicator(
                    progress = { metrics.usedRatio },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(8.dp)
                      .clip(RoundedCornerShape(4.dp)),
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.2f)
                  )
                }
              }
            }
          }
        }

        // 1. LOCAL MAIN DEVICE WARNING (When Local Main Device drops to 15% or lower)
        if (localBatteryLevel <= 15) {
          item {
            Surface(
              color = Color(0x22D32F2F),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, Color(0xFFD32F2F)),
              modifier = Modifier
                .fillMaxWidth()
                .isolateInputLayer(enabled = true)
                .testTag("local_low_battery_banner")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  modifier = Modifier.weight(1f),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.BatteryAlert,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(24.dp)
                  )
                  Text(
                    text = "The device is on ${localBatteryLevel}% battery, disconnect or connect a charger to continue safely using the transfer to prevent corruption.",
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = primaryTextColor,
                      lineHeight = 16.sp
                    )
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                  onClick = {
                    HapticManager.errorPulse(context)
                    NearbyDevicesEngine.endSession()
                    Toast.makeText(context, "Session disconnected", Toast.LENGTH_SHORT).show()
                  },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                  ),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier.testTag("banner_local_disconnect_button")
                ) {
                  Text("Disconnect", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                }
              }
            }
          }
        }

        // 2. REMOTE CONNECTED DEVICE WARNING (When Remote Peer drops to 15% or lower)
        if (remoteBatteryLevel != null && remoteBatteryLevel!! <= 15) {
          item {
            Surface(
              color = Color(0x22D32F2F),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, Color(0xFFD32F2F)),
              modifier = Modifier
                .fillMaxWidth()
                .isolateInputLayer(enabled = true)
                .testTag("remote_low_battery_banner")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  modifier = Modifier.weight(1f),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.BatteryAlert,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(24.dp)
                  )
                  Text(
                    text = "The connected device is on ${remoteBatteryLevel}%. You should disconnect the device to prevent data corruption.",
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = primaryTextColor,
                      lineHeight = 16.sp
                    )
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                  onClick = {
                    HapticManager.errorPulse(context)
                    NearbyDevicesEngine.endSession()
                    Toast.makeText(context, "Session disconnected", Toast.LENGTH_SHORT).show()
                  },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                  ),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier.testTag("banner_remote_disconnect_button")
                ) {
                  Text("Disconnect", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                }
              }
            }
          }
        }

        // 3. Remote Storage File Explorer
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Remote Storage Explorer",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
              )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              IconButton(
                onClick = {
                  activePeer?.let { peer ->
                    isCreateFolderDialogOpen = true
                  }
                },
                modifier = Modifier.testTag("remote_create_folder_button")
              ) {
                Icon(
                  imageVector = Icons.Default.CreateNewFolder,
                  contentDescription = "New Folder",
                  tint = accentColor
                )
              }

              IconButton(
                onClick = {
                  activePeer?.let { peer ->
                    NearbyDevicesEngine.fetchRemoteDirectoryListing(peer.ipAddress, remoteCurrentPath)
                  }
                },
                modifier = Modifier.testTag("remote_refresh_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = "Refresh",
                  tint = accentColor
                )
              }
            }
          }
        }

        // Path Navigation bar
        item {
          Surface(
            color = cardContainer,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
              )
              Text(
                text = remoteCurrentPath.ifEmpty { "Root Partition" },
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = primaryTextColor
                ),
                modifier = Modifier.weight(1f)
              )
              if (remoteCurrentPath.isNotBlank() && remoteCurrentPath != "/") {
                IconButton(
                  onClick = {
                    val parent = File(remoteCurrentPath).parent ?: ""
                    activePeer?.let { peer ->
                      NearbyDevicesEngine.fetchRemoteDirectoryListing(peer.ipAddress, parent)
                    }
                  },
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Up Directory",
                    tint = primaryTextColor,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }

        // NOTE: Rule 5 constraint:
        // Shortcut button that launches 'System Hidden Files App' (SAF Backdoor) is strictly HIDDEN
        // when actively viewing or managing a remote device's storage structure!

        // Remote File Items List
        if (remoteFileList.isEmpty()) {
          item {
            Surface(
              color = cardContainer,
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, cardBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(24.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "Directory is empty",
                  style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor)
                )
              }
            }
          }
        } else {
          items(remoteFileList, key = { it.path }) { file ->
            val isMedia = file.name.endsWith(".mp4", true) ||
                file.name.endsWith(".mkv", true) ||
                file.name.endsWith(".mp3", true) ||
                file.name.endsWith(".wav", true) ||
                file.name.endsWith(".aac", true)

            Surface(
              color = cardContainer,
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, cardBorder),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  HapticManager.selectionTick(context)
                  if (file.isDirectory) {
                    activePeer?.let { peer ->
                      NearbyDevicesEngine.fetchRemoteDirectoryListing(peer.ipAddress, file.path)
                    }
                  } else if (isMedia && activePeer != null) {
                    // Rule 3: Live Media Streaming over HTTP Range Server
                    val streamUrl = NearbyDevicesEngine.getRemoteStreamingUrl(activePeer!!.ipAddress, file.path)
                    val dummyFile = FileItem(File(file.path)).copy(customStreamUrl = streamUrl)
                    onStreamMediaUrl(dummyFile)
                  } else {
                    Toast
                      .makeText(context, "Selected remote file: ${file.name}", Toast.LENGTH_SHORT)
                      .show()
                  }
                }
                .testTag("remote_file_row_${file.name}")
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
                  horizontalArrangement = Arrangement.spacedBy(12.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(
                    imageVector = when {
                      file.isDirectory -> Icons.Default.Folder
                      isMedia -> Icons.Default.PlayCircle
                      else -> Icons.Default.InsertDriveFile
                    },
                    contentDescription = null,
                    tint = if (file.isDirectory) accentColor else secondaryTextColor,
                    modifier = Modifier.size(24.dp)
                  )
                  Column {
                    Text(
                      text = file.name,
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor
                      )
                    )
                    Text(
                      text = file.formattedSize,
                      style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
                    )
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (isMedia && !file.isDirectory && activePeer != null) {
                    IconButton(
                      onClick = {
                        val streamUrl = NearbyDevicesEngine.getRemoteStreamingUrl(activePeer!!.ipAddress, file.path)
                        val dummyFile = FileItem(File(file.path)).copy(customStreamUrl = streamUrl)
                        onStreamMediaUrl(dummyFile)
                      },
                      modifier = Modifier.size(32.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Stream Media",
                        tint = accentColor
                      )
                    }
                  }

                  IconButton(
                    onClick = {
                      remoteRenameTarget = file
                      newRenameInput = file.name
                    },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Edit,
                      contentDescription = "Rename Remote",
                      tint = secondaryTextColor,
                      modifier = Modifier.size(16.dp)
                    )
                  }

                  IconButton(
                    onClick = {
                      activePeer?.let { peer ->
                        NearbyDevicesEngine.deleteRemoteItem(peer.ipAddress, file.path) { success ->
                          if (success) {
                            Toast.makeText(context, "Deleted ${file.name}", Toast.LENGTH_SHORT).show()
                          }
                        }
                      }
                    },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = "Delete Remote",
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }
          }
        }
      } else {
        // Discovered Devices Section
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Discovered Devices (${discoveredPeers.size})",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
              )
            )
          }
        }

        if (discoveredPeers.isEmpty()) {
          item {
            Surface(
              color = cardContainer,
              shape = RoundedCornerShape(16.dp),
              border = BorderStroke(1.dp, cardBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Box(
                  modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = accentColor.copy(alpha = pulseAlpha),
                    modifier = Modifier.size(32.dp)
                  )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                  text = "Scanning for Nearby Devices...",
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor
                  )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Open Nearby Devices on another phone or tablet connected to this Wi-Fi network to begin streaming or managing files.",
                  style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor),
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          }
        } else {
          items(discoveredPeers, key = { it.hardwareToken }) { peer ->
            val isConnecting = connectingPeerToken == peer.hardwareToken

            Surface(
              color = cardContainer,
              shape = RoundedCornerShape(16.dp),
              border = BorderStroke(1.dp, cardBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Box(
                    modifier = Modifier
                      .size(44.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.PhoneAndroid,
                      contentDescription = null,
                      tint = accentColor,
                      modifier = Modifier.size(24.dp)
                    )
                  }
                  Column {
                    Text(
                      text = peer.deviceName,
                      style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor
                      )
                    )
                    Text(
                      text = "${peer.deviceModel} • ${peer.ipAddress}",
                      style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
                    )
                  }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Button(
                    onClick = {
                      connectingPeerToken = peer.hardwareToken
                      HapticManager.selectionTick(context)
                      NearbyDevicesEngine.requestConnectPeer(
                        peer = peer,
                        context = context,
                        onSuccess = {
                          connectingPeerToken = null
                          HapticManager.selectionTick(context)
                          Toast.makeText(context, "Connected to ${peer.deviceName}", Toast.LENGTH_SHORT).show()
                        },
                        onRejected = { reason ->
                          connectingPeerToken = null
                          HapticManager.errorPulse(context)
                          Toast.makeText(context, reason, Toast.LENGTH_LONG).show()
                        },
                        onError = { err ->
                          connectingPeerToken = null
                          HapticManager.errorPulse(context)
                          Toast.makeText(context, "Connection failed: $err", Toast.LENGTH_SHORT).show()
                        }
                      )
                    },
                    enabled = !isConnecting,
                    colors = ButtonDefaults.buttonColors(
                      containerColor = accentColor,
                      contentColor = if (isLight) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("connect_peer_button_${peer.hardwareToken}")
                  ) {
                    if (isConnecting) {
                      CircularProgressIndicator(
                        color = if (isLight) Color.White else Color.Black,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                      )
                    } else {
                      Text("Connect", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                  }

                  // Block hardware token button
                  IconButton(
                    onClick = {
                      NearbyDevicesEngine.blockDevice(context, peer.hardwareToken, peer.deviceName, peer.ipAddress)
                      Toast.makeText(context, "Blocked ${peer.deviceName}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Block,
                      contentDescription = "Block Device",
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // 4. On-Screen Handshake Approval Dialog (Device B)
    incomingRequest?.let { request ->
      AlertDialog(
        onDismissRequest = {
          NearbyDevicesEngine.rejectIncomingConnection(request)
        },
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = accentColor)
            Text(
              text = "Nearby Devices Request",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = primaryTextColor)
            )
          }
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "${request.requesterName} (${request.requesterModel}) on ${request.requesterIp} wants to connect to your device over local Wi-Fi.",
              style = MaterialTheme.typography.bodyMedium.copy(color = secondaryTextColor)
            )
            Text(
              text = "Approving will allow remote file browsing, media streaming, and atomic transfers between both devices.",
              style = MaterialTheme.typography.bodySmall.copy(color = primaryTextColor, fontWeight = FontWeight.Medium)
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              HapticManager.selectionTick(context)
              NearbyDevicesEngine.approveIncomingConnection(request)
              Toast.makeText(context, "Nearby Devices Session Approved", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = accentColor,
              contentColor = if (isLight) Color.White else Color.Black
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("dialog_approve_nearby_button")
          ) {
            Text("Allow Connection", fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(
              onClick = {
                HapticManager.errorPulse(context)
                NearbyDevicesEngine.blockDevice(context, request.requesterToken, request.requesterName, request.requesterIp)
                NearbyDevicesEngine.rejectIncomingConnection(request)
                Toast.makeText(context, "Blocked ${request.requesterName}", Toast.LENGTH_SHORT).show()
              }
            ) {
              Text("Block Device", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }

            TextButton(
              onClick = {
                HapticManager.errorPulse(context)
                NearbyDevicesEngine.rejectIncomingConnection(request)
              }
            ) {
              Text("Decline", color = secondaryTextColor, fontWeight = FontWeight.Bold)
            }
          }
        },
        containerColor = cardContainer,
        shape = RoundedCornerShape(16.dp)
      )
    }

    // Rule 2: Mandatory Profile Selection Dialog
    // Temporary Session Boundary Profile Prompt Dialog
    if (showNamePromptDialog) {
      AlertDialog(
        onDismissRequest = {
          val fallback = if (tempNameInput.isBlank()) "Guest_${(1000..9999).random()}" else tempNameInput.trim()
          NearbyDevicesEngine.setSessionTemporaryName(fallback)
          sessionName = fallback
          showNamePromptDialog = false
        },
        title = {
          Text(
            text = "Session Display Name",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = primaryTextColor)
          )
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "Enter a temporary name for this Nearby Devices session. This name will only be used for over-the-air broadcasts during this session and will not be saved to your permanent user profile.",
              style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor)
            )
            OutlinedTextField(
              value = tempNameInput,
              onValueChange = { tempNameInput = it },
              placeholder = { Text("e.g. Nearby Guest") },
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = primaryTextColor,
                unfocusedTextColor = primaryTextColor,
                focusedBorderColor = accentColor,
                unfocusedBorderColor = cardBorder
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              val nameToUse = if (tempNameInput.isBlank()) "Guest_${(1000..9999).random()}" else tempNameInput.trim()
              NearbyDevicesEngine.setSessionTemporaryName(nameToUse)
              sessionName = nameToUse
              showNamePromptDialog = false
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = accentColor,
              contentColor = if (isLight) Color.White else Color.Black
            )
          ) {
            Text("Set Session Name", fontWeight = FontWeight.Bold)
          }
        },
        containerColor = cardContainer,
        shape = RoundedCornerShape(16.dp)
      )
    }

    // Rule 7: Low Battery Safeguard Warning Dialog (15% or lower)
    if (showBatteryWarningDialog) {
      AlertDialog(
        onDismissRequest = { showBatteryWarningDialog = false },
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(imageVector = Icons.Default.BatteryAlert, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Text(
              text = "Low Battery Warning",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = primaryTextColor)
            )
          }
        },
        text = {
          Text(
            text = "Low Battery Warning: Nearby Devices wireless file management consumes significant power ($currentBatteryLevel% remaining). Please connect your device to a charger to prevent connection drops or data corruption.",
            style = MaterialTheme.typography.bodyMedium.copy(color = secondaryTextColor)
          )
        },
        confirmButton = {
          Button(
            onClick = { showBatteryWarningDialog = false },
            colors = ButtonDefaults.buttonColors(
              containerColor = accentColor,
              contentColor = if (isLight) Color.White else Color.Black
            )
          ) {
            Text("Continue Anyway", fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(
            onClick = {
              showBatteryWarningDialog = false
              onNavigateBack()
            }
          ) {
            Text("Cancel", color = secondaryTextColor, fontWeight = FontWeight.Bold)
          }
        },
        containerColor = cardContainer,
        shape = RoundedCornerShape(16.dp)
      )
    }

    // Rule 6: Blocked Devices Sub-Menu Dialog
    if (showBlockedDevicesDialog) {
      AlertDialog(
        onDismissRequest = { showBlockedDevicesDialog = false },
        title = {
          Text(
            text = "Blocked Devices",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = primaryTextColor)
          )
        },
        text = {
          if (blockedList.isEmpty()) {
            Text("No devices are currently blocked.", style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor))
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              blockedList.forEach { blocked ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(blocked.deviceName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = primaryTextColor))
                    Text(blocked.lastKnownIp, style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor))
                  }
                  TextButton(
                    onClick = {
                      NearbyDevicesEngine.unblockDevice(context, blocked.hardwareToken)
                      Toast.makeText(context, "Unblocked ${blocked.deviceName}", Toast.LENGTH_SHORT).show()
                    }
                  ) {
                    Text("Unblock", color = accentColor, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { showBlockedDevicesDialog = false }) {
            Text("Close", color = secondaryTextColor, fontWeight = FontWeight.Bold)
          }
        },
        containerColor = cardContainer,
        shape = RoundedCornerShape(16.dp)
      )
    }

    // Remote Rename Dialog
    remoteRenameTarget?.let { target ->
      AlertDialog(
        onDismissRequest = { remoteRenameTarget = null },
        title = { Text("Rename Remote File", color = primaryTextColor, fontWeight = FontWeight.Bold) },
        text = {
          OutlinedTextField(
            value = newRenameInput,
            onValueChange = { newRenameInput = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        },
        confirmButton = {
          Button(
            onClick = {
              val targetToRename = remoteRenameTarget
              remoteRenameTarget = null
              if (targetToRename != null && activePeer != null && newRenameInput.isNotBlank()) {
                NearbyDevicesEngine.renameRemoteItem(activePeer!!.ipAddress, targetToRename.path, newRenameInput.trim()) { success ->
                  if (success) Toast.makeText(context, "Renamed successfully", Toast.LENGTH_SHORT).show()
                }
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
          ) {
            Text("Rename", fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { remoteRenameTarget = null }) {
            Text("Cancel", color = secondaryTextColor)
          }
        },
        containerColor = cardContainer
      )
    }

    // Remote Create Folder Dialog
    if (isCreateFolderDialogOpen) {
      AlertDialog(
        onDismissRequest = { isCreateFolderDialogOpen = false },
        title = { Text("Create Remote Folder", color = primaryTextColor, fontWeight = FontWeight.Bold) },
        text = {
          OutlinedTextField(
            value = newFolderInput,
            onValueChange = { newFolderInput = it },
            placeholder = { Text("Folder Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        },
        confirmButton = {
          Button(
            onClick = {
              val folderName = newFolderInput.trim()
              isCreateFolderDialogOpen = false
              newFolderInput = ""
              if (folderName.isNotBlank() && activePeer != null) {
                NearbyDevicesEngine.createRemoteFolder(activePeer!!.ipAddress, remoteCurrentPath, folderName) { success ->
                  if (success) Toast.makeText(context, "Created folder $folderName", Toast.LENGTH_SHORT).show()
                }
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
          ) {
            Text("Create", fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { isCreateFolderDialogOpen = false }) {
            Text("Cancel", color = secondaryTextColor)
          }
        },
        containerColor = cardContainer
      )
    }

    // Sync Interval Setting Selector Dialog
    if (showSyncIntervalDialog) {
      AlertDialog(
        onDismissRequest = { showSyncIntervalDialog = false },
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = accentColor)
            Text(
              text = "Sync Interval Settings",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = primaryTextColor)
            )
          }
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "Adjust how frequently telemetry updates are broadcasted during active sessions to optimize power consumption:",
              style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor)
            )
            Spacer(modifier = Modifier.height(4.dp))
            NearbyDevicesEngine.SyncInterval.entries.forEach { option ->
              val isSelected = option.ms == syncIntervalMs
              Surface(
                color = if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (isSelected) accentColor else cardBorder),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    HapticManager.selectionTick(context)
                    NearbyDevicesEngine.setSyncInterval(context, option.ms)
                    showSyncIntervalDialog = false
                  }
                  .padding(10.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = option.label,
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) accentColor else primaryTextColor
                      )
                    )
                    Text(
                      text = option.description,
                      style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
                    )
                  }
                  RadioButton(
                    selected = isSelected,
                    onClick = {
                      HapticManager.selectionTick(context)
                      NearbyDevicesEngine.setSyncInterval(context, option.ms)
                      showSyncIntervalDialog = false
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                  )
                }
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { showSyncIntervalDialog = false }) {
            Text("Close", color = accentColor, fontWeight = FontWeight.Bold)
          }
        },
        containerColor = cardContainer,
        shape = RoundedCornerShape(16.dp)
      )
    }
  }
}
