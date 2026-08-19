package com.jackattackk246.files.ui.section

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.sp
import com.jackattackk246.files.model.FileItem
import com.jackattackk246.files.model.FileSortOrder
import com.jackattackk246.files.security.DeveloperSecurityEngine
import com.jackattackk246.files.ui.dialog.OutOSeasonGameLauncherDialog
import com.jackattackk246.files.ui.theme.AppThemeMode
import com.jackattackk246.files.ui.theme.ThemeManager
import com.jackattackk246.files.util.DeveloperToolsManager
import com.jackattackk246.files.util.FestiveCalendarEngine
import com.jackattackk246.files.util.HapticFeedbackHelper
import com.jackattackk246.files.util.NearbyDevicesEngine
import java.io.File
import java.io.FileOutputStream

@Composable
fun DeveloperUtilitiesSubSection(
  onBack: () -> Unit,
  currentThemeMode: AppThemeMode = AppThemeMode.MIDNIGHT_MATTE_BLACK
) {
  val context = LocalContext.current
  val activeThemeAccent = ThemeManager.getThemeAccentColor(currentThemeMode)
  val cardContainer = ThemeManager.getAdaptiveCardContainerColor(currentThemeMode)
  val cardBorder = ThemeManager.getAdaptiveCardBorderColor(currentThemeMode)
  val primaryTextColor = ThemeManager.getAdaptivePrimaryTextColor(currentThemeMode)
  val secondaryTextColor = ThemeManager.getAdaptiveSecondaryTextColor(currentThemeMode)

  var selectedSimulatedEvent by remember { mutableStateOf(FestiveCalendarEngine.getSimulatedEvent()) }
  var isSimEventDropdownExpanded by remember { mutableStateOf(false) }

  // 7 Control States from DeveloperToolsManager
  var originalSettingsEnabled by remember { mutableStateOf(DeveloperToolsManager.isOriginalSettingsEnabled(context)) }
  var fpsOverlayEnabled by remember { mutableStateOf(DeveloperToolsManager.isFpsOverlayEnabled(context)) }
  var powerProfile by remember { mutableStateOf(DeveloperToolsManager.getPowerProfile(context)) }
  var isPowerDropdownExpanded by remember { mutableStateOf(false) }
  var universalTextOverrideEnabled by remember { mutableStateOf(DeveloperToolsManager.isUniversalTextOverrideEnabled(context)) }

  // New Diagnostic States
  var simulatedPowerChoice by remember { mutableStateOf(DeveloperToolsManager.getSimulatedPowerChoice(context)) }
  var isSimPowerDropdownExpanded by remember { mutableStateOf(false) }

  // Hardware Simulation Override States
  val configuration = androidx.compose.ui.platform.LocalConfiguration.current
  val isNativeWatch: Boolean = remember(configuration) {
    com.jackattackk246.files.util.DeviceEnvironmentDetector.isPhysicalSmartwatch(context, configuration)
  }

  val activeHardwareProfile by DeveloperToolsManager.simulatedHardwareProfileState.collectAsState()
  val persistHardwareProfile by DeveloperToolsManager.persistHardwareProfileState.collectAsState()

  var isHardwareDropdownExpanded by remember { mutableStateOf(false) }
  var pendingHardwareProfileChoice by remember { mutableStateOf<String?>(null) }

  // Mandatory UI Overhaul Warning Dialog
  if (pendingHardwareProfileChoice != null) {
    val choice = pendingHardwareProfileChoice!!
    AlertDialog(
      onDismissRequest = { pendingHardwareProfileChoice = null },
      title = {
        Text("UI Transformation Alert", fontWeight = FontWeight.Bold, color = primaryTextColor)
      },
      text = {
        Text(
          "UI Transformation Alert: Changing this hardware profile will immediately force the application layout, available features, and navigation nodes to restructure to match the simulated device type.",
          color = secondaryTextColor,
          fontSize = 13.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            DeveloperToolsManager.setSimulatedHardwareProfile(context, choice)
            pendingHardwareProfileChoice = null
            Toast.makeText(context, "Hardware Profile set to: $choice", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = activeThemeAccent, contentColor = Color.Black)
        ) {
          Text("Confirm Overhaul", fontWeight = FontWeight.Bold, color = Color.Black)
        }
      },
      dismissButton = {
        TextButton(
          onClick = { pendingHardwareProfileChoice = null }
        ) {
          Text("Cancel", color = secondaryTextColor)
        }
      },
      containerColor = Color(0xFF1C1D22),
      shape = RoundedCornerShape(16.dp)
    )
  }

  // Out of season games launcher dialog state
  var showGamesLauncherDialog by remember { mutableStateOf(false) }

  if (showGamesLauncherDialog) {
    OutOSeasonGameLauncherDialog(
      onDismiss = { showGamesLauncherDialog = false },
      onLaunchGame = { gameState ->
        Toast.makeText(context, "Standalone Game Container Started: $gameState", Toast.LENGTH_LONG).show()
      }
    )
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("developer_utilities_screen"),
    verticalArrangement = Arrangement.spacedBy(14.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
  ) {
    // Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("developer_menu_back_button")
        ) {
          Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = primaryTextColor
          )
        }
        Column {
          Text(
            text = "Developer Diagnostics & Controls",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp
            ),
            color = primaryTextColor
          )
          Text(
            text = "Expanded Master System Configuration v2.4.6",
            style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
          )
        }
      }
    }

    // Row Toggle 1: Original Settings
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainer),
        border = BorderStroke(1.dp, cardBorder)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(text = "Original Settings Layout Mode", fontWeight = FontWeight.Bold, color = primaryTextColor)
            Text(text = "Flips system rendering logic between custom layout parameters and classic settings nodes.", fontSize = 12.sp, color = secondaryTextColor)
          }
          Switch(
            checked = originalSettingsEnabled,
            onCheckedChange = {
              originalSettingsEnabled = it
              DeveloperToolsManager.setOriginalSettingsEnabled(context, it)
              Toast.makeText(context, "Original Settings Mode: $it", Toast.LENGTH_SHORT).show()
            },
            colors = SwitchDefaults.colors(checkedThumbColor = activeThemeAccent)
          )
        }
      }
    }

    // Action Row 2: Clear App Image & Layout Caches Button
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainer),
        border = BorderStroke(1.dp, cardBorder)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(text = "Clear App Image & Layout Caches", fontWeight = FontWeight.Bold, color = primaryTextColor)
          Text(text = "Instantly purges un-cached graphics bitmap frames, temporary canvas asset memory maps, and thumbnail disk caches.", fontSize = 12.sp, color = secondaryTextColor)
          Button(
            onClick = {
              DeveloperToolsManager.clearAllCaches(context)
              HapticFeedbackHelper.performTransferSuccessFeedback(context)
              Toast.makeText(context, "Caches Purged Successfully", Toast.LENGTH_SHORT).show()
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = activeThemeAccent),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("PURGE ALL BITMAP & LAYOUT CACHES", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Switch Row 3: Performance Metrics FPS Overlay
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainer),
        border = BorderStroke(1.dp, cardBorder)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(text = "Performance Metrics FPS Overlay", fontWeight = FontWeight.Bold, color = primaryTextColor)
            Text(text = "Render real-time digital frames-per-second performance tracker floating on layout canvas.", fontSize = 12.sp, color = secondaryTextColor)
          }
          Switch(
            checked = fpsOverlayEnabled,
            onCheckedChange = {
              fpsOverlayEnabled = it
              DeveloperToolsManager.setFpsOverlayEnabled(context, it)
              Toast.makeText(context, "FPS Overlay: $it", Toast.LENGTH_SHORT).show()
            },
            colors = SwitchDefaults.colors(checkedThumbColor = activeThemeAccent)
          )
        }
      }
    }

    // Spinner Row 4: Simulated Power Gating Profiles
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainer),
        border = BorderStroke(1.dp, cardBorder)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(text = "Simulated Power Gating Profiles", fontWeight = FontWeight.Bold, color = primaryTextColor)
          Text(text = "Throttles or accelerates composition update intervals on host runtime loops.", fontSize = 12.sp, color = secondaryTextColor)

          Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
              onClick = { isPowerDropdownExpanded = true },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, activeThemeAccent)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = powerProfile, color = primaryTextColor, fontWeight = FontWeight.Medium)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = primaryTextColor)
              }
            }

            DropdownMenu(
              expanded = isPowerDropdownExpanded,
              onDismissRequest = { isPowerDropdownExpanded = false },
              modifier = Modifier
                .background(cardContainer)
                .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
            ) {
              val profiles = listOf("Standard Balance Profile", "Aggressive Power Gater", "Uncapped Frame Redraw Pass")
              profiles.forEach { profileOption ->
                DropdownMenuItem(
                  text = { Text(profileOption, color = if (profileOption == powerProfile) activeThemeAccent else primaryTextColor) },
                  onClick = {
                    powerProfile = profileOption
                    DeveloperToolsManager.setPowerProfile(context, profileOption)
                    isPowerDropdownExpanded = false
                    Toast.makeText(context, "Power Profile Set: $profileOption", Toast.LENGTH_SHORT).show()
                  }
                )
              }
            }
          }
        }
      }
    }

    // Action Row 5: P2P Sync Hub Force-Broadcast Target
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainer),
        border = BorderStroke(1.dp, cardBorder)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(text = "P2P Sync Hub Force-Broadcast Target", fontWeight = FontWeight.Bold, color = primaryTextColor)
          Text(text = "Overrides group dormancy rules, forcefully spinning up offline chip-to-chip TCP/IP socket direct hardware discovery loop.", fontSize = 12.sp, color = secondaryTextColor)
          Button(
            onClick = {
              NearbyDevicesEngine.startServices(context)
              HapticFeedbackHelper.performTransferSuccessFeedback(context)
              Toast.makeText(context, "P2P Sync Hub: Force-broadcast socket active", Toast.LENGTH_SHORT).show()
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = activeThemeAccent),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.WifiTethering, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("FORCE BROADCAST P2P SOCKET", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Selector Row 6: Direct Launch Out-of-Season Games Menu
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainer),
        border = BorderStroke(1.dp, cardBorder)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(text = "Direct Launch Out-of-Season Games Menu", fontWeight = FontWeight.Bold, color = primaryTextColor)
          Text(text = "Opens internal sub-panel list indexing cross-platform seasonal mini-game frameworks as simple un-gated shortcuts.", fontSize = 12.sp, color = secondaryTextColor)
          OutlinedButton(
            onClick = { showGamesLauncherDialog = true },
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, activeThemeAccent),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(16.dp), tint = activeThemeAccent)
            Spacer(modifier = Modifier.width(6.dp))
            Text("OPEN GAMES LAUNCHPAD", color = primaryTextColor, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Switch Row 7: Universal UI Text Overwrite Toggle System
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainer),
        border = BorderStroke(1.dp, cardBorder)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(text = "Enable Universal UI Text Editing Override", fontWeight = FontWeight.Bold, color = primaryTextColor)
            Text(text = "Allows direct live text-field overrides on otherwise read-only string properties.", fontSize = 12.sp, color = secondaryTextColor)
          }
          Switch(
            checked = universalTextOverrideEnabled,
            onCheckedChange = {
              universalTextOverrideEnabled = it
              DeveloperToolsManager.setUniversalTextOverrideEnabled(context, it)
              Toast.makeText(context, "Universal Text Overwrite: $it", Toast.LENGTH_SHORT).show()
            },
            colors = SwitchDefaults.colors(checkedThumbColor = activeThemeAccent)
          )
        }
      }
    }

    // Row 8: Time-Warp Simulation Engine
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainer),
        border = BorderStroke(1.dp, cardBorder)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = activeThemeAccent)
            Text(
              text = "Time-Warp Simulation Engine",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = primaryTextColor
            )
          }

          Text(
            text = "Inject mock system calendar events completely offline to preview dynamic seasonal headers & overlays.",
            style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
          )

          Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
              onClick = { isSimEventDropdownExpanded = true },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("spinner_countdown_day_selector"),
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, activeThemeAccent)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (selectedSimulatedEvent == FestiveCalendarEngine.FestiveEvent.NONE) "Active Device Time (Live)" else selectedSimulatedEvent.title,
                  color = primaryTextColor,
                  fontWeight = FontWeight.Medium
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = primaryTextColor)
              }
            }

            DropdownMenu(
              expanded = isSimEventDropdownExpanded,
              onDismissRequest = { isSimEventDropdownExpanded = false },
              modifier = Modifier
                .background(cardContainer)
                .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
            ) {
              FestiveCalendarEngine.FestiveEvent.entries.forEach { evt ->
                DropdownMenuItem(
                  text = {
                    Text(
                      text = if (evt == FestiveCalendarEngine.FestiveEvent.NONE) "Live Device Clock" else evt.title,
                      color = if (evt == selectedSimulatedEvent) activeThemeAccent else primaryTextColor
                    )
                  },
                  onClick = {
                    selectedSimulatedEvent = evt
                    FestiveCalendarEngine.setSimulatedEvent(evt)
                    isSimEventDropdownExpanded = false
                    Toast.makeText(context, "Simulating: ${if (evt == FestiveCalendarEngine.FestiveEvent.NONE) "Live Clock" else evt.title}", Toast.LENGTH_SHORT).show()
                  }
                )
              }
            }
          }
        }
      }
    }

    // Row 9 (NEW): Simulate Local Power States (Dropdown Selector)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainer),
        border = BorderStroke(1.dp, cardBorder)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.BatterySaver, contentDescription = null, tint = activeThemeAccent)
            Text(
              text = "Simulate Local Power States",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = primaryTextColor
            )
          }

          Text(
            text = "Fakes native BatteryManager return value to immediately test Nearby Devices session low-power alert banners.",
            style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
          )

          Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
              onClick = { isSimPowerDropdownExpanded = true },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, activeThemeAccent)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = simulatedPowerChoice,
                  color = primaryTextColor,
                  fontWeight = FontWeight.Medium
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = primaryTextColor)
              }
            }

            DropdownMenu(
              expanded = isSimPowerDropdownExpanded,
              onDismissRequest = { isSimPowerDropdownExpanded = false },
              modifier = Modifier
                .background(cardContainer)
                .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
            ) {
              val choices = listOf(
                "Normal Power Status",
                "Force Low Battery Warning (15%)",
                "Critical Cutoff State (5%)"
              )
              choices.forEach { choiceOption ->
                DropdownMenuItem(
                  text = {
                    Text(
                      text = choiceOption,
                      color = if (choiceOption == simulatedPowerChoice) activeThemeAccent else primaryTextColor
                    )
                  },
                  onClick = {
                    simulatedPowerChoice = choiceOption
                    DeveloperToolsManager.setSimulatedPowerChoice(context, choiceOption)
                    isSimPowerDropdownExpanded = false
                    Toast.makeText(context, "Power State: $choiceOption", Toast.LENGTH_SHORT).show()
                  }
                )
              }
            }
          }
        }
      }
    }

    // Row 10 (NEW): Purge LAN Access Restrictions (Button Action)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainer),
        border = BorderStroke(1.dp, cardBorder)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.CleaningServices, contentDescription = null, tint = activeThemeAccent)
            Text(
              text = "Purge LAN Access Restrictions",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = primaryTextColor
            )
          }

          Text(
            text = "Wipes offline hardware signature database and IP blocklist array to instantly unblock all restricted physical peers.",
            style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
          )

          Button(
            onClick = {
              NearbyDevicesEngine.clearAllBlockedDevices(context)
              HapticFeedbackHelper.performTransferSuccessFeedback(context)
              Toast.makeText(context, "Cleared all blocked IP modules and peer access restrictions.", Toast.LENGTH_LONG).show()
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = activeThemeAccent),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("CLEAR ALL BLOCKED IP MODULES", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Row 11: Dump Installation APK Binary
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainer),
        border = BorderStroke(1.dp, cardBorder)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.Download, contentDescription = null, tint = activeThemeAccent)
            Text(
              text = "Dump Installation APK Binary",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = primaryTextColor
            )
          }

          Text(
            text = "Queries local source package sector and writes an offline compiled .apk binary package copy directly into device storage without root.",
            style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
          )

          Button(
            onClick = {
              try {
                val sourceDir = context.applicationInfo.sourceDir
                val sourceFile = File(sourceDir)
                val targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!targetDir.exists()) targetDir.mkdirs()
                val targetApk = File(targetDir, "Files.apk")

                sourceFile.inputStream().use { input ->
                  targetApk.outputStream().use { output ->
                    input.copyTo(output)
                  }
                }
                HapticFeedbackHelper.performTransferSuccessFeedback(context)
                Toast.makeText(context, "Dumped APK to: Downloads/Files.apk", Toast.LENGTH_LONG).show()
              } catch (e: Exception) {
                HapticFeedbackHelper.performErrorFeedback(context)
                Toast.makeText(context, "Failed to dump APK: ${e.message}", Toast.LENGTH_SHORT).show()
              }
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = activeThemeAccent),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("dump_installation_apk_button")
          ) {
            Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("DUMP INSTALLATION APK", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Row 11B: Mount Simulated OTG Storage Volume
    item {
      val simulatedOtgEnabled by DeveloperToolsManager.simulatedOtgState.collectAsState()
      Card(
        modifier = Modifier.fillMaxWidth().testTag("simulated_otg_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainer),
        border = BorderStroke(1.dp, cardBorder)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.Usb, contentDescription = null, tint = activeThemeAccent, modifier = Modifier.size(20.dp))
              Text(text = "Mount Simulated OTG Storage Volume", fontWeight = FontWeight.Bold, color = primaryTextColor)
            }
            Text(
              text = "Initializes a genuine, sandboxed local partition '.virtual_usb_sandbox' in secure storage and dynamically registers it as an external hardware drive for authentic loopback testing.",
              fontSize = 12.sp,
              color = secondaryTextColor
            )
          }
          Switch(
            checked = simulatedOtgEnabled,
            onCheckedChange = {
              DeveloperToolsManager.setSimulatedOtgEnabled(context, it)
              val status = if (it) "Mounted Simulated USB OTG Volume" else "Unmounted Simulated USB OTG Volume"
              Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
            },
            colors = SwitchDefaults.colors(checkedThumbColor = activeThemeAccent)
          )
        }
      }
    }

    // Hardware Simulation Override Card (Visible ONLY on Phone/Tablet Viewports)
    if (!isNativeWatch) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("simulate_hardware_profile_card"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = cardContainer),
          border = BorderStroke(1.dp, cardBorder)
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.Devices, contentDescription = null, tint = activeThemeAccent)
              Text(
                text = "Simulate Hardware Profile Sizing",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = primaryTextColor
              )
            }

            Text(
              text = "Tricks the system WindowSizeClass alternator to test responsive UI changes and layout constraints completely offline.",
              style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
            )

            Box(modifier = Modifier.fillMaxWidth()) {
              OutlinedButton(
                onClick = { isHardwareDropdownExpanded = true },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("hardware_profile_dropdown_button"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, activeThemeAccent)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = activeHardwareProfile,
                    color = primaryTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                  )
                  Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = activeThemeAccent)
                }
              }

              DropdownMenu(
                expanded = isHardwareDropdownExpanded,
                onDismissRequest = { isHardwareDropdownExpanded = false },
                modifier = Modifier.background(Color(0xFF1C1D22))
              ) {
                DeveloperToolsManager.HARDWARE_PROFILES.forEach { profile ->
                  DropdownMenuItem(
                    text = {
                      Text(
                        text = profile,
                        color = if (profile == activeHardwareProfile) activeThemeAccent else Color.White,
                        fontWeight = if (profile == activeHardwareProfile) FontWeight.Bold else FontWeight.Normal
                      )
                    },
                    onClick = {
                      isHardwareDropdownExpanded = false
                      if (profile != activeHardwareProfile) {
                        if (profile == "Default (Native Hardware Detection)") {
                          DeveloperToolsManager.setSimulatedHardwareProfile(context, profile)
                        } else {
                          pendingHardwareProfileChoice = profile
                        }
                      }
                    }
                  )
                }
              }
            }

            // Persist Switch
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Persist Simulated Profile Across Restarts",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                  color = primaryTextColor
                )
                Text(
                  text = "Saves mocked hardware layout parameters to local preferences map.",
                  style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
                )
              }
              Switch(
                checked = persistHardwareProfile,
                onCheckedChange = {
                  DeveloperToolsManager.setPersistHardwareProfile(context, it)
                },
                modifier = Modifier.testTag("persist_hardware_profile_switch")
              )
            }
          }
        }
      }
    }
  }
}
