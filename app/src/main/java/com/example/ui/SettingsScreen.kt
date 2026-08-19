package com.jackattackk246.files.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.jackattackk246.files.model.BuiltInWallpaper
import com.jackattackk246.files.model.DashboardWidgetConfig
import com.jackattackk246.files.model.DashboardWidgetId
import com.jackattackk246.files.model.EnvironmentalBackdropConfig
import com.jackattackk246.files.model.EnvironmentalSeason
import com.jackattackk246.files.model.SearchLocation
import com.jackattackk246.files.model.SearchOptions
import com.jackattackk246.files.model.SearchStyle
import com.jackattackk246.files.model.WallpaperConfig
import com.jackattackk246.files.model.WidgetSizeMode
import com.jackattackk246.files.ui.theme.AppThemeMode
import com.jackattackk246.files.ui.theme.ThemeManager
import com.jackattackk246.files.util.DashboardPreferences
import com.jackattackk246.files.util.EnvironmentalPreferences
import com.jackattackk246.files.util.FileManager
import com.jackattackk246.files.util.HapticFeedbackHelper
import com.jackattackk246.files.util.HapticManager
import com.jackattackk246.files.util.IconChangerEngine
import com.jackattackk246.files.util.LauncherIconVariant
import com.jackattackk246.files.util.ThemePreferences
import com.jackattackk246.files.ui.dialog.DeveloperPasswordAuthDialog
import com.jackattackk246.files.ui.section.DeveloperUtilitiesSubSection
import com.jackattackk246.files.security.DeveloperSecurityEngine
import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.min

class SettingsPaginationEngine {
    // Permanently expand the maximum layout registry threshold to 100 items across 10 pages
    private val absoluteTotalThemesCount = 100 
    private val itemsPerPageThreshold = 10

    /**
     * Primitive Page Hardcoding supporting 10 pages x 10 items.
     */
    fun loadPrimitiveThemePageSlice(activePageNumber: Int): List<Int> {
        return when (activePageNumber) {
            1 -> listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            2 -> listOf(11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
            3 -> listOf(21, 22, 23, 24, 25, 26, 27, 28, 29, 30)
            4 -> listOf(31, 32, 33, 34, 35, 36, 37, 38, 39, 40)
            5 -> listOf(41, 42, 43, 44, 45, 46, 47, 48, 49, 50)
            6 -> listOf(51, 52, 53, 54, 55, 56, 57, 58, 59, 60)
            7 -> listOf(61, 62, 63, 64, 65, 66, 67, 68, 69, 70)
            8 -> listOf(71, 72, 73, 74, 75, 76, 77, 78, 79, 80)
            9 -> listOf(81, 82, 83, 84, 85, 86, 87, 88, 89, 90)
            10 -> listOf(91, 92, 93, 94, 95, 96, 97, 98, 99, 100)
            else -> listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        }
    }

    /**
     * Dynamically calculates page allocations based on a 100-item universe.
     */
    fun calculateDynamicPaginationBounds(requestedPage: Int): IntRange {
        val totalExpectedPages = ceil(absoluteTotalThemesCount.toDouble() / itemsPerPageThreshold).toInt()
        val boundedPage = requestedPage.coerceIn(1, totalExpectedPages)
        
        val elementStartIndex = (boundedPage - 1) * itemsPerPageThreshold
        val elementEndIndex = min(elementStartIndex + itemsPerPageThreshold, absoluteTotalThemesCount)

        return IntRange(elementStartIndex, elementEndIndex - 1)
    }
}

enum class SettingsSubSection {
  MAIN,
  THEMES,
  LAYOUT,
  PERMISSIONS,
  DEVELOPER
}

@Composable
fun SettingsScreen(
  currentThemeMode: AppThemeMode,
  onThemeModeChanged: (AppThemeMode) -> Unit,
  customAccentColor: Color?,
  onCustomAccentColorChanged: (Color?) -> Unit,
  storageMetrics: FileManager.StorageMetrics,
  onRefreshStorage: () -> Unit,
  onOpenSearchConfigDialog: () -> Unit,
  onOpenWallpaperEngineDialog: () -> Unit,
  onOpenEnvironmentalEngineDialog: (() -> Unit)? = null,
  onOpenWelcomeWizard: (() -> Unit)? = null,
  onReplayTutorial: () -> Unit = {},
  initialSubSection: SettingsSubSection = SettingsSubSection.MAIN
) {
  val context = LocalContext.current
  var currentSubSection by remember { mutableStateOf(initialSubSection) }
  var showColorPicker by remember { mutableStateOf(false) }

  // Intercept back gesture to return to main settings if in a nested sub-section
  BackHandler(enabled = currentSubSection != SettingsSubSection.MAIN) {
    currentSubSection = SettingsSubSection.MAIN
  }

  if (showColorPicker) {
    ColorPickerDialog(
      initialColor = customAccentColor ?: MaterialTheme.colorScheme.primary,
      onColorSelected = {
        onCustomAccentColorChanged(it)
        ThemePreferences.setSavedCustomAccentColor(context, it)
      },
      onDismiss = { showColorPicker = false }
    )
  }

  AnimatedContent(
    targetState = currentSubSection,
    transitionSpec = {
      if (targetState != SettingsSubSection.MAIN) {
        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
      } else {
        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
      }
    },
    label = "settings_sub_section_anim"
  ) { subSection ->
    when (subSection) {
      SettingsSubSection.MAIN -> {
        SettingsMainTab(
          currentThemeMode = currentThemeMode,
          customAccentColor = customAccentColor,
          storageMetrics = storageMetrics,
          onRefreshStorage = onRefreshStorage,
          onNavigateToThemes = { currentSubSection = SettingsSubSection.THEMES },
          onNavigateToLayout = { currentSubSection = SettingsSubSection.LAYOUT },
          onNavigateToPermissions = { currentSubSection = SettingsSubSection.PERMISSIONS },
          onNavigateToDeveloper = { currentSubSection = SettingsSubSection.DEVELOPER },
          onOpenSearchConfigDialog = onOpenSearchConfigDialog,
          onOpenWallpaperEngineDialog = onOpenWallpaperEngineDialog,
          onOpenWelcomeWizard = onOpenWelcomeWizard,
          onReplayTutorial = onReplayTutorial
        )
      }

      SettingsSubSection.THEMES -> {
        InterfaceThemesSubSection(
          currentThemeMode = currentThemeMode,
          onThemeModeChanged = onThemeModeChanged,
          customAccentColor = customAccentColor,
          onCustomAccentColorChanged = onCustomAccentColorChanged,
          onOpenColorPicker = { showColorPicker = true },
          onOpenEnvironmentalEngineDialog = onOpenEnvironmentalEngineDialog,
          onBack = { currentSubSection = SettingsSubSection.MAIN }
        )
      }

      SettingsSubSection.LAYOUT -> {
        LayoutConfigurationsSubSection(
          onBack = { currentSubSection = SettingsSubSection.MAIN }
        )
      }

      SettingsSubSection.PERMISSIONS -> {
        StoragePermissionsSubSection(
          onBack = { currentSubSection = SettingsSubSection.MAIN }
        )
      }

      SettingsSubSection.DEVELOPER -> {
        DeveloperUtilitiesSubSection(
          onBack = { currentSubSection = SettingsSubSection.MAIN },
          currentThemeMode = currentThemeMode
        )
      }
    }
  }
}

@Composable
private fun SettingsMainTab(
  currentThemeMode: AppThemeMode,
  customAccentColor: Color?,
  storageMetrics: FileManager.StorageMetrics,
  onRefreshStorage: () -> Unit,
  onNavigateToThemes: () -> Unit,
  onNavigateToLayout: () -> Unit,
  onNavigateToPermissions: () -> Unit,
  onNavigateToDeveloper: () -> Unit,
  onOpenSearchConfigDialog: () -> Unit,
  onOpenWallpaperEngineDialog: () -> Unit,
  onOpenWelcomeWizard: (() -> Unit)? = null,
  onReplayTutorial: () -> Unit
) {
  val context = LocalContext.current
  val activeThemeAccent = ThemeManager.getThemeAccentColor(currentThemeMode, customAccentColor)
  val cardContainer = ThemeManager.getAdaptiveCardContainerColor(currentThemeMode)
  val cardBorder = ThemeManager.getAdaptiveCardBorderColor(currentThemeMode)
  val primaryTextColor = ThemeManager.getAdaptivePrimaryTextColor(currentThemeMode)
  val secondaryTextColor = ThemeManager.getAdaptiveSecondaryTextColor(currentThemeMode)

  var versionTapCount by remember { mutableStateOf(0) }
  var lastTapTimestamp by remember { mutableStateOf(0L) }
  var showDeveloperAuthDialog by remember { mutableStateOf(false) }

  if (showDeveloperAuthDialog) {
    if (DeveloperSecurityEngine.isDeveloperUnlocked(context)) {
      LaunchedEffect(Unit) {
        showDeveloperAuthDialog = false
        onNavigateToDeveloper()
      }
    } else {
      DeveloperPasswordAuthDialog(
        onSuccess = {
          showDeveloperAuthDialog = false
          onNavigateToDeveloper()
        },
        onDismiss = {
          showDeveloperAuthDialog = false
        }
      )
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("settings_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp)
  ) {
    // 1. Top Panel Header
    item {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 4.dp)
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(activeThemeAccent.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = null,
            tint = activeThemeAccent,
            modifier = Modifier.size(24.dp)
          )
        }
        Column {
          Text(
            text = "Configurations",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 22.sp
            ),
            color = primaryTextColor
          )
          Text(
            text = "Personalize Themes, Layouts & System Permissions",
            style = MaterialTheme.typography.labelMedium.copy(
              color = secondaryTextColor
            )
          )
        }
      }
    }

    // Onboarding / Welcome Wizard Card
    if (onOpenWelcomeWizard != null) {
      item {
        Card(
          onClick = onOpenWelcomeWizard,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, activeThemeAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .testTag("settings_open_welcome_wizard_card"),
          colors = CardDefaults.cardColors(containerColor = cardContainer)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(activeThemeAccent.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = activeThemeAccent,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                "Personal Setup Wizard (Files)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = primaryTextColor
              )
              Text(
                "Configure profile, language, region, storage access & appearance",
                fontSize = 12.sp,
                color = secondaryTextColor
              )
            }
            Icon(
              Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = secondaryTextColor,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }

    item {
      Card(
        onClick = onReplayTutorial,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .border(1.dp, activeThemeAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
          .testTag("settings_replay_tutorial_card"),
        colors = CardDefaults.cardColors(containerColor = cardContainer)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(activeThemeAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.PlayCircle,
              contentDescription = null,
              tint = activeThemeAccent,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              "Replay App Tutorial",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = primaryTextColor
            )
            Text(
              "Take a quick guided walkthrough tour of all active offline user features and utilities inside the app.",
              fontSize = 12.sp,
              color = secondaryTextColor
            )
          }
          Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = secondaryTextColor,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    // 2. Primary Navigation Options
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Preferences & Customization",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = activeThemeAccent
        )

        // 0. Developer Options Nav Card if unlocked
        if (DeveloperSecurityEngine.isDeveloperUnlocked(context)) {
          SettingsNavCard(
            title = "Developer Options & Diagnostics",
            subtitle = "Expanded Master System Controls, FPS Overlay, Power Profiles & Out-of-Season Games",
            icon = Icons.Default.Code,
            badge = "Active",
            testTag = "settings_row_developer_options",
            onClick = onNavigateToDeveloper,
            accentColor = Color(0xFFDC2626),
            containerColor = cardContainer,
            borderColor = cardBorder,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor
          )
        }

        // 1. Interface Themes Option Row
        SettingsNavCard(
          title = "Interface Themes",
          subtitle = "Master 100 Design Options Catalog (Paginated 10/Page) & Custom Color Engine",
          icon = Icons.Default.Palette,
          badge = "100 Styles",
          testTag = "settings_row_interface_themes",
          onClick = onNavigateToThemes,
          accentColor = activeThemeAccent,
          containerColor = cardContainer,
          borderColor = cardBorder,
          primaryTextColor = primaryTextColor,
          secondaryTextColor = secondaryTextColor
        )

        // 2. Layout Configurations Option Row
        SettingsNavCard(
          title = "Layout Configurations",
          subtitle = "Dashboard Widget Reordering, Resize Modes & Edit Switch",
          icon = Icons.Default.DashboardCustomize,
          badge = "Customizer",
          testTag = "settings_row_layout_configurations",
          onClick = onNavigateToLayout,
          accentColor = activeThemeAccent,
          containerColor = cardContainer,
          borderColor = cardBorder,
          primaryTextColor = primaryTextColor,
          secondaryTextColor = secondaryTextColor
        )

        // Haptic Feedback Intensity Control Slider
        var hapticIntensity by remember { mutableFloatStateOf(HapticManager.getHapticIntensity(context)) }
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .testTag("settings_haptic_intensity_card"),
          colors = CardDefaults.cardColors(containerColor = cardContainer)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    .background(activeThemeAccent.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = null,
                    tint = activeThemeAccent,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Column {
                  Text(
                    text = "Haptic Feedback Intensity",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = primaryTextColor
                  )
                  Text(
                    text = "Card reordering & sizing vibration strength",
                    fontSize = 11.sp,
                    color = secondaryTextColor
                  )
                }
              }
              Text(
                text = "${(hapticIntensity * 100).toInt()}%",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = activeThemeAccent
              )
            }
            Slider(
              value = hapticIntensity,
              onValueChange = { newValue ->
                hapticIntensity = newValue
                HapticManager.setHapticIntensity(context, newValue)
              },
              onValueChangeFinished = {
                HapticManager.selectionTick(context)
              },
              valueRange = 0f..1f,
              colors = SliderDefaults.colors(
                thumbColor = activeThemeAccent,
                activeTrackColor = activeThemeAccent,
                inactiveTrackColor = activeThemeAccent.copy(alpha = 0.2f)
              ),
              modifier = Modifier.fillMaxWidth().testTag("haptic_intensity_slider")
            )
          }
        }

        // 3. Storage Permissions Option Row
        SettingsNavCard(
          title = "Storage Permissions",
          subtitle = "All Files Access (MANAGE_EXTERNAL_STORAGE) & SAF Status",
          icon = Icons.Default.Security,
          badge = "Access Control",
          testTag = "settings_row_storage_permissions",
          onClick = onNavigateToPermissions,
          accentColor = activeThemeAccent,
          containerColor = cardContainer,
          borderColor = cardBorder,
          primaryTextColor = primaryTextColor,
          secondaryTextColor = secondaryTextColor
        )
      }
    }

    // 3. Search & Wallpaper Engine Direct Tools
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Tools & Background Engine",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = activeThemeAccent
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedCard(
            onClick = onOpenSearchConfigDialog,
            modifier = Modifier
              .weight(1f)
              .testTag("settings_search_scrapers_card"),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, cardBorder),
            colors = CardDefaults.outlinedCardColors(containerColor = cardContainer)
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(activeThemeAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.Search,
                  contentDescription = null,
                  tint = activeThemeAccent,
                  modifier = Modifier.size(20.dp)
                )
              }
              Text(
                "Smart Search",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = primaryTextColor
              )
              Text(
                "Offline metadata, size descriptors & query filters",
                style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
              )
            }
          }

          OutlinedCard(
            onClick = onOpenWallpaperEngineDialog,
            modifier = Modifier
              .weight(1f)
              .testTag("settings_wallpaper_engine_card"),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, cardBorder),
            colors = CardDefaults.outlinedCardColors(containerColor = cardContainer)
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(activeThemeAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.Wallpaper,
                  contentDescription = null,
                  tint = activeThemeAccent,
                  modifier = Modifier.size(20.dp)
                )
              }
              Text(
                "Wallpaper Engine",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = primaryTextColor
              )
              Text(
                "Custom image, blur & dark overlay",
                style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
              )
            }
          }
        }
      }
    }

    // Power User Utilities Sub-Section Row Panel
    item {
      var instantUiSnapping by remember { mutableStateOf(false) }
      var forceExternalStorageWrite by remember { mutableStateOf(false) }
      var hardOrientationLock by remember { mutableStateOf(false) }

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("power_user_utilities_panel"),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, cardBorder),
        colors = CardDefaults.cardColors(containerColor = cardContainer)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              Icons.Default.Bolt,
              contentDescription = null,
              tint = activeThemeAccent,
              modifier = Modifier.size(20.dp)
            )
            Text(
              "Power User Utilities",
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleSmall,
              color = primaryTextColor
            )
          }

          // 1. Instant UI Snapping
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("toggle_instant_ui_snapping"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                "Instant UI Snapping",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = primaryTextColor
              )
              Text(
                "Overrides configuration window metrics to set transitions to 0.0x for zero animation lag",
                style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
              )
            }
            Switch(
              checked = instantUiSnapping,
              onCheckedChange = { instantUiSnapping = it }
            )
          }

          // 2. Force External Storage Write
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("toggle_force_external_storage_write"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                "Force External Storage Write",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = primaryTextColor
              )
              Text(
                "Maps SAF paths to automatically force file movements to external SD cards/OTG stick directories",
                style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
              )
            }
            Switch(
              checked = forceExternalStorageWrite,
              onCheckedChange = { forceExternalStorageWrite = it }
            )
          }

          // 3. Hard Orientation Lock
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("toggle_hard_orientation_lock"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                "Hard Orientation Lock",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = primaryTextColor
              )
              Text(
                "Static window manager locks to completely force layout scales regardless of physical sensor movements",
                style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
              )
            }
            Switch(
              checked = hardOrientationLock,
              onCheckedChange = { hardOrientationLock = it }
            )
          }
        }
      }
    }

    // 4. Live Storage Capacity Meters
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, cardBorder),
        colors = CardDefaults.cardColors(containerColor = cardContainer)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
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
                Icons.Default.Storage,
                contentDescription = null,
                tint = activeThemeAccent,
                modifier = Modifier.size(20.dp)
              )
              Text(
                "Device Storage",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = primaryTextColor
              )
            }

            OutlinedButton(
              onClick = onRefreshStorage,
              modifier = Modifier.testTag("refresh_storage_metrics_screen_button"),
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, activeThemeAccent.copy(alpha = 0.5f)),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, tint = activeThemeAccent, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Refresh", fontSize = 11.sp, color = activeThemeAccent)
            }
          }

          Text(
            text = "Used: ${storageMetrics.usedGbFormatted} / Free: ${storageMetrics.freeGbFormatted} (Total: ${storageMetrics.totalGbFormatted})",
            style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
          )

          LinearProgressIndicator(
            progress = { storageMetrics.usedRatio },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(CircleShape),
            color = activeThemeAccent,
            trackColor = ThemeManager.getAdaptiveMetricsTrackColor(currentThemeMode)
          )
        }
      }
    }

    // 5. Version Info
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .clickable {
            if (DeveloperSecurityEngine.isDeveloperUnlocked(context)) {
              onNavigateToDeveloper()
              return@clickable
            }
            val now = System.currentTimeMillis()
            if (now - lastTapTimestamp > 3500L) {
              versionTapCount = 1
            } else {
              versionTapCount++
            }
            lastTapTimestamp = now

            if (DeveloperSecurityEngine.isPermanentKarmaBrickActive(context)) {
              Toast.makeText(context, "Nice try!", Toast.LENGTH_SHORT).show()
              return@clickable
            }

            if (versionTapCount in 7..9) {
              val remaining = 10 - versionTapCount
              Toast.makeText(
                context,
                "You are now $remaining ${if (remaining == 1) "step" else "steps"} away from becoming a developer.",
                Toast.LENGTH_SHORT
              ).show()
            } else if (versionTapCount >= 10) {
              versionTapCount = 0
              showDeveloperAuthDialog = true
            }
          }
          .testTag("txt_about_version_number"),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, cardBorder),
        colors = CardDefaults.cardColors(containerColor = cardContainer)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = activeThemeAccent, modifier = Modifier.size(20.dp))
            Text("Files v2.4.6 Production", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = primaryTextColor)
          }
          Text(
            text = "Engineered with Jetpack Compose, modular reorderable dashboard widgets, SAF deep-linking, All Files Access bypass & instant memory cache.",
            style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor, lineHeight = 16.sp)
          )
        }
      }
    }
  }
}

@Composable
private fun LayoutConfigurationsSubSection(
  onBack: () -> Unit
) {
  val context = LocalContext.current
  var isEditModeUnlocked by remember { mutableStateOf(DashboardPreferences.isEditModeUnlocked(context)) }
  var widgetList by remember { mutableStateOf(DashboardPreferences.getWidgetLayoutOrder(context)) }

  fun updateAndSaveWidgets(newList: List<DashboardWidgetConfig>) {
    widgetList = newList
    DashboardPreferences.saveWidgetLayoutOrder(context, newList)
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("layout_configurations_sub_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp)
  ) {
    // Header
    item {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(vertical = 4.dp)
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("layout_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to Settings"
          )
        }
        Column {
          Text(
            text = "Layout Configurations",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp)
          )
          Text(
            text = "Reorder Dashboard Widgets & Toggle Card Size Modes",
            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
          )
        }
      }
    }

    // Edit Mode Toggle Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("edit_mode_toggle_card"),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isEditModeUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.EditNote,
                contentDescription = null,
                tint = if (isEditModeUnlocked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
              )
            }
            Column {
              Text(
                "Unlock Dashboard Edit Mode",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
              )
              Text(
                if (isEditModeUnlocked) "Edit controls & drag arrows active on dashboard" else "Turn on to show reorder & resize controls on dashboard",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
              )
            }
          }

          Switch(
            checked = isEditModeUnlocked,
            onCheckedChange = {
              HapticFeedbackHelper.performToggleFeedback(context)
              isEditModeUnlocked = it
              DashboardPreferences.setEditModeUnlocked(context, it)
            },
            modifier = Modifier.testTag("edit_mode_switch")
          )
        }
      }
    }

    // Windows 11 Desktop Workspace Layout Toggle Card
    item {
      var isDesktopForced by remember {
        mutableStateOf(com.aistudio.fileslauncher.ui.ThemeSynchronizationBridge.isForcedWindows11DesktopEnabled(context))
      }
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("desktop_workspace_toggle_card"),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isDesktopForced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.DesktopWindows,
                contentDescription = null,
                tint = if (isDesktopForced) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
              )
            }
            Column {
              Text(
                "Windows 11 Desktop Workspace",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
              )
              Text(
                if (isDesktopForced) "Forced Windows 11 desktop canvas active on tablet/standard views" else "Standard mobile/tablet viewport profile",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
              )
            }
          }

          Switch(
            checked = isDesktopForced,
            onCheckedChange = {
              HapticFeedbackHelper.performToggleFeedback(context)
              isDesktopForced = it
              com.aistudio.fileslauncher.ui.ThemeSynchronizationBridge.setForcedWindows11DesktopEnabled(context, it)
            },
            modifier = Modifier.testTag("desktop_workspace_switch")
          )
        }
      }
    }

    // Reorderable Widget List Section
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Widget Sequence & Card Size Modes",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary
        )

        TextButton(
          onClick = {
            val defaults = DashboardPreferences.getDefaultWidgetList()
            updateAndSaveWidgets(defaults)
            Toast.makeText(context, "Layout Reset to Defaults", Toast.LENGTH_SHORT).show()
          },
          modifier = Modifier.testTag("reset_layout_button")
        ) {
          Text("Reset Defaults", fontSize = 12.sp)
        }
      }
    }

    // List of configurable widgets
    itemsIndexed(widgetList) { index, item ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("widget_config_card_${item.widgetId.id}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                shape = CircleShape
              ) {
                Text(
                  text = "#${index + 1}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  ),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }

              Column {
                Text(
                  text = item.widgetId.title,
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.bodyMedium
                )
                Text(
                  text = item.widgetId.description,
                  style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                  maxLines = 1
                )
              }
            }

            // Up / Down Reorder Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
              IconButton(
                onClick = {
                  if (index > 0) {
                    HapticFeedbackHelper.performToggleFeedback(context)
                    val mutable = widgetList.toMutableList()
                    val moved = mutable.removeAt(index)
                    mutable.add(index - 1, moved)
                    updateAndSaveWidgets(mutable)
                  }
                },
                enabled = index > 0,
                modifier = Modifier.size(36.dp).testTag("move_up_widget_${item.widgetId.id}")
              ) {
                Icon(
                  Icons.Default.ArrowUpward,
                  contentDescription = "Move Up",
                  tint = if (index > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                  modifier = Modifier.size(18.dp)
                )
              }

              IconButton(
                onClick = {
                  if (index < widgetList.lastIndex) {
                    HapticFeedbackHelper.performToggleFeedback(context)
                    val mutable = widgetList.toMutableList()
                    val moved = mutable.removeAt(index)
                    mutable.add(index + 1, moved)
                    updateAndSaveWidgets(mutable)
                  }
                },
                enabled = index < widgetList.lastIndex,
                modifier = Modifier.size(36.dp).testTag("move_down_widget_${item.widgetId.id}")
              ) {
                Icon(
                  Icons.Default.ArrowDownward,
                  contentDescription = "Move Down",
                  tint = if (index < widgetList.lastIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

          // Card Size Mode Selector
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              "Card Size Mode",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              FilterChip(
                selected = item.sizeMode == WidgetSizeMode.FULL,
                onClick = {
                  HapticFeedbackHelper.performToggleFeedback(context)
                  val mutable = widgetList.toMutableList()
                  mutable[index] = item.copy(sizeMode = WidgetSizeMode.FULL)
                  updateAndSaveWidgets(mutable)
                },
                label = { Text("Full", fontSize = 11.sp) },
                modifier = Modifier.testTag("size_full_${item.widgetId.id}")
              )

              FilterChip(
                selected = item.sizeMode == WidgetSizeMode.COMPACT,
                onClick = {
                  HapticFeedbackHelper.performToggleFeedback(context)
                  val mutable = widgetList.toMutableList()
                  mutable[index] = item.copy(sizeMode = WidgetSizeMode.COMPACT)
                  updateAndSaveWidgets(mutable)
                },
                label = { Text("Compact", fontSize = 11.sp) },
                modifier = Modifier.testTag("size_compact_${item.widgetId.id}")
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StoragePermissionsSubSection(
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val hasAllFilesAccess = remember {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      Environment.isExternalStorageManager()
    } else {
      true
    }
  }

  val manageStorageLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) {}

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("storage_permissions_sub_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp)
  ) {
    // Header
    item {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(vertical = 4.dp)
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("permissions_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to Settings"
          )
        }
        Column {
          Text(
            text = "Storage Permissions",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp)
          )
          Text(
            text = "Manage External Storage & SAF Status",
            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
          )
        }
      }
    }

    // All Files Access Status Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(if (hasAllFilesAccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  if (hasAllFilesAccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                  contentDescription = null,
                  tint = if (hasAllFilesAccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(22.dp)
                )
              }
              Column {
                Text("All Files Access", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(
                  if (hasAllFilesAccess) "MANAGE_EXTERNAL_STORAGE Active" else "Permission Required",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (hasAllFilesAccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                  )
                )
              }
            }

            Surface(
              color = if (hasAllFilesAccess) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = if (hasAllFilesAccess) "Granted" else "Action Required",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (hasAllFilesAccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Text(
            text = "All Files Access gives Files unrestricted local access to read, write, rename, batch zip, extract and organize directories without Android sandbox restrictions.",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
          )

          Button(
            onClick = {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                  val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                  }
                  manageStorageLauncher.launch(intent)
                } catch (_: Exception) {
                  try {
                    val fallbackIntent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    manageStorageLauncher.launch(fallbackIntent)
                  } catch (_: Exception) {}
                }
              } else {
                Toast.makeText(context, "All legacy storage permissions granted", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("manage_all_files_settings_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Open System Storage Permission Settings")
          }
        }
      }
    }

    // SAF Document Tree Routing Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Text("Storage Access Framework (SAF)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
          }

          Text(
            text = "Direct SAF Intent Deep-Link routing enables launching system Document Tree dialogs and bypassing package isolation walls.",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
          )
        }
      }
    }

    // Privacy Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
          Column {
            Text("Privacy Guarantee", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(
              "Zero network telemetry, zero background servers, 100% on-device local computation.",
              style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
          }
        }
      }
    }
  }
}

@Composable
fun RenderMenuRowWithBadge(
  title: String = "Environmental Backdrop Engine",
  description: String = "Real-time clock seasons (Winter Icy Blue, Summer Fluid Sky...) + 60% Dark Glass Masks",
  badgeText: String = "Integrated",
  onClick: (() -> Unit)? = null,
  icon: ImageVector = Icons.Default.WbSunny,
  accentColor: Color = Color(0xFFD32F2F),
  cardContainer: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
  cardBorder: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
  primaryTextColor: Color = MaterialTheme.colorScheme.onSurface,
  secondaryTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
  modifier: Modifier = Modifier
) {
  OutlinedCard(
    onClick = { onClick?.invoke() },
    modifier = modifier
      .fillMaxWidth()
      .testTag("themes_environmental_engine_card"),
    shape = RoundedCornerShape(14.dp),
    border = BorderStroke(1.dp, cardBorder),
    colors = CardDefaults.outlinedCardColors(containerColor = cardContainer)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(accentColor.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(22.dp)
        )
      }
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = title,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            color = primaryTextColor,
            modifier = Modifier.weight(1f, fill = false),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          // Unconstrained horizontal text capsule preventing vertical stacking
          Surface(
            modifier = Modifier.wrapContentWidth(),
            color = Color(0x1AD32F2F),
            border = BorderStroke(1.dp, Color(0xFFD32F2F)),
            shape = RoundedCornerShape(50.dp)
          ) {
            Text(
              text = badgeText,
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFFD32F2F),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              ),
              maxLines = 1,
              softWrap = false,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }
        }
        Text(
          text = description,
          style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor),
          lineHeight = 15.sp
        )
      }
      Icon(
        Icons.Default.ChevronRight,
        contentDescription = "Configure Seasons",
        tint = secondaryTextColor
      )
    }
  }
}

@Composable
private fun SettingsNavCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  badge: String,
  testTag: String,
  onClick: () -> Unit,
  accentColor: Color = MaterialTheme.colorScheme.primary,
  containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
  borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
  primaryTextColor: Color = MaterialTheme.colorScheme.onSurface,
  secondaryTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
  val context = LocalContext.current
  Card(
    onClick = {
      HapticManager.navigationClick(context)
      onClick()
    },
    modifier = Modifier
      .fillMaxWidth()
      .testTag(testTag),
    shape = RoundedCornerShape(18.dp),
    border = BorderStroke(1.dp, borderColor),
    colors = CardDefaults.cardColors(containerColor = containerColor)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(accentColor.copy(alpha = 0.2f)),
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
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
            color = primaryTextColor
          )
          Surface(
            modifier = Modifier.wrapContentWidth(),
            color = accentColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(50.dp)
          ) {
            Text(
              text = badge,
              style = MaterialTheme.typography.labelSmall.copy(
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              ),
              maxLines = 1,
              softWrap = false,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }
        }
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor),
          maxLines = 1
        )
      }

      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = null,
        tint = secondaryTextColor.copy(alpha = 0.6f)
      )
    }
  }
}

@Composable
fun ConfigurationsDialog(
  currentThemeMode: AppThemeMode,
  onThemeModeChanged: (AppThemeMode) -> Unit,
  customAccentColor: Color?,
  onCustomAccentColorChanged: (Color?) -> Unit,
  storageMetrics: FileManager.StorageMetrics? = null,
  onRefreshStorage: (() -> Unit)? = null,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var activeSubTab by remember { mutableStateOf(SettingsSubSection.THEMES) }
  var showColorPicker by remember { mutableStateOf(false) }

  if (showColorPicker) {
    ColorPickerDialog(
      initialColor = customAccentColor ?: MaterialTheme.colorScheme.primary,
      onColorSelected = {
        onCustomAccentColorChanged(it)
        ThemePreferences.setSavedCustomAccentColor(context, it)
      },
      onDismiss = { showColorPicker = false }
    )
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp)
        .testTag("settings_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .fillMaxHeight(0.88f),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Column {
            Text(
              text = "Quick Configurations",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Interface Themes & Dashboard Layout",
              style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
          }
        }

        // Sub Tabs Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = activeSubTab == SettingsSubSection.THEMES,
            onClick = { activeSubTab = SettingsSubSection.THEMES },
            label = { Text("Themes") },
            modifier = Modifier.weight(1f)
          )
          FilterChip(
            selected = activeSubTab == SettingsSubSection.LAYOUT,
            onClick = { activeSubTab = SettingsSubSection.LAYOUT },
            label = { Text("Layout") },
            modifier = Modifier.weight(1f)
          )
        }

        HorizontalDivider()

        Box(modifier = Modifier.weight(1f)) {
          when (activeSubTab) {
            SettingsSubSection.THEMES -> {
              LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                item {
                  Text("Persistent Theme Palettes", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }

                item {
                  ThemeOptionRow(
                    title = "Classic Black and White",
                    subtitle = "Stark white typography over a pure pitch-black (#000000) canvas container.",
                    selected = currentThemeMode == AppThemeMode.CLASSIC_BLACK_WHITE && customAccentColor == null,
                    tag = "dialog_theme_option_classic_black_white",
                    accentColor = Color(0xFFFFFFFF),
                    onClick = {
                      onCustomAccentColorChanged(null)
                      onThemeModeChanged(AppThemeMode.CLASSIC_BLACK_WHITE)
                      ThemePreferences.setSavedThemeMode(context, AppThemeMode.CLASSIC_BLACK_WHITE)
                      ThemePreferences.setSavedCustomAccentColor(context, null)
                    }
                  )
                }

                item {
                  ThemeOptionRow(
                    title = "Classic White and Black",
                    subtitle = "High-contrast deep black typography over a clean, solid white (#FFFFFF) canvas layout.",
                    selected = currentThemeMode == AppThemeMode.CLASSIC_WHITE_BLACK && customAccentColor == null,
                    tag = "dialog_theme_option_classic_white_black",
                    accentColor = Color(0xFF000000),
                    onClick = {
                      onCustomAccentColorChanged(null)
                      onThemeModeChanged(AppThemeMode.CLASSIC_WHITE_BLACK)
                      ThemePreferences.setSavedThemeMode(context, AppThemeMode.CLASSIC_WHITE_BLACK)
                      ThemePreferences.setSavedCustomAccentColor(context, null)
                    }
                  )
                }

                // Removed entry for Dynamic Weather Canvas

                item {
                  ThemeOptionRow(
                    title = "OLED Pitch Black",
                    subtitle = "Pure black backdrop (#000000) optimized for extreme panel power savings.",
                    selected = currentThemeMode == AppThemeMode.PITCH_BLACK_OLED && customAccentColor == null,
                    tag = "dialog_theme_option_oled",
                    accentColor = Color(0xFF38BDF8),
                    onClick = {
                      onCustomAccentColorChanged(null)
                      onThemeModeChanged(AppThemeMode.PITCH_BLACK_OLED)
                      ThemePreferences.setSavedThemeMode(context, AppThemeMode.PITCH_BLACK_OLED)
                      ThemePreferences.setSavedCustomAccentColor(context, null)
                    }
                  )
                }

                item {
                  ThemeOptionRow(
                    title = "Midnight Matte Black",
                    subtitle = "Smooth dark charcoal canvas (#141414) with subtle slate contrast frames.",
                    selected = currentThemeMode == AppThemeMode.MIDNIGHT_MATTE_BLACK && customAccentColor == null,
                    tag = "dialog_theme_option_midnight_black",
                    accentColor = Color(0xFF38BDF8),
                    onClick = {
                      onCustomAccentColorChanged(null)
                      onThemeModeChanged(AppThemeMode.MIDNIGHT_MATTE_BLACK)
                      ThemePreferences.setSavedThemeMode(context, AppThemeMode.MIDNIGHT_MATTE_BLACK)
                      ThemePreferences.setSavedCustomAccentColor(context, null)
                    }
                  )
                }

                item {
                  ThemeOptionRow(
                    title = "Spring Emerald",
                    subtitle = "Rich forest green backdrop (#0A1F0D) with vivid neon green (#00FF66) active radio indicators.",
                    selected = (currentThemeMode == AppThemeMode.SPRING_EMERALD || currentThemeMode == AppThemeMode.MATRIX_GREEN) && customAccentColor == null,
                    tag = "dialog_theme_option_matrix_green",
                    accentColor = Color(0xFF00FF66),
                    onClick = {
                      onCustomAccentColorChanged(null)
                      onThemeModeChanged(AppThemeMode.SPRING_EMERALD)
                      ThemePreferences.setSavedThemeMode(context, AppThemeMode.SPRING_EMERALD)
                      ThemePreferences.setSavedCustomAccentColor(context, null)
                    }
                  )
                }

                item {
                  ThemeOptionRow(
                    title = "Cyberpunk Amber",
                    subtitle = "Deep graphite backing with high-saturation amber yellow (#FFB000) structural highlight borders.",
                    selected = currentThemeMode == AppThemeMode.CYBERPUNK_AMBER && customAccentColor == null,
                    tag = "dialog_theme_option_cyberpunk_amber",
                    accentColor = Color(0xFFFFB000),
                    onClick = {
                      onCustomAccentColorChanged(null)
                      onThemeModeChanged(AppThemeMode.CYBERPUNK_AMBER)
                      ThemePreferences.setSavedThemeMode(context, AppThemeMode.CYBERPUNK_AMBER)
                      ThemePreferences.setSavedCustomAccentColor(context, null)
                    }
                  )
                }

                item {
                  ThemeOptionRow(
                    title = "Deep Purple",
                    subtitle = "Royal amethyst gradient (#2E0854) shifting down into a deep obsidian void.",
                    selected = currentThemeMode == AppThemeMode.DEEP_PURPLE && customAccentColor == null,
                    tag = "dialog_theme_option_deep_purple",
                    accentColor = Color(0xFFC084FC),
                    onClick = {
                      onCustomAccentColorChanged(null)
                      onThemeModeChanged(AppThemeMode.DEEP_PURPLE)
                      ThemePreferences.setSavedThemeMode(context, AppThemeMode.DEEP_PURPLE)
                      ThemePreferences.setSavedCustomAccentColor(context, null)
                    }
                  )
                }

                item {
                  ThemeOptionRow(
                    title = "Ocean Blue",
                    subtitle = "Deep maritime sapphire canvas (#0D2B45) with ice blue highlights.",
                    selected = currentThemeMode == AppThemeMode.OCEAN_BLUE && customAccentColor == null,
                    tag = "dialog_theme_option_ocean_blue",
                    accentColor = Color(0xFF38BDF8),
                    onClick = {
                      onCustomAccentColorChanged(null)
                      onThemeModeChanged(AppThemeMode.OCEAN_BLUE)
                      ThemePreferences.setSavedThemeMode(context, AppThemeMode.OCEAN_BLUE)
                      ThemePreferences.setSavedCustomAccentColor(context, null)
                    }
                  )
                }

                item {
                  ThemeOptionRow(
                    title = "Tails' Mechanical Sandbox",
                    subtitle = "A crisp cell-shaded industrial gray and brushed metallic bronze architectural layout grid.",
                    selected = currentThemeMode == AppThemeMode.TAILS_MECHANICAL_SANDBOX && customAccentColor == null,
                    tag = "dialog_theme_option_tails_sandbox",
                    accentColor = Color(0xFFD97706),
                    onClick = {
                      onCustomAccentColorChanged(null)
                      onThemeModeChanged(AppThemeMode.TAILS_MECHANICAL_SANDBOX)
                      ThemePreferences.setSavedThemeMode(context, AppThemeMode.TAILS_MECHANICAL_SANDBOX)
                      ThemePreferences.setSavedCustomAccentColor(context, null)
                    }
                  )
                }

                item {
                  ThemeOptionRow(
                    title = "Autumn Copper",
                    subtitle = "A warm ambient gradient shifting smoothly from Deep Burgundy (#4A1525) down to Muted Burnt Orange (#A84B24).",
                    selected = currentThemeMode == AppThemeMode.AUTUMN_COPPER && customAccentColor == null,
                    tag = "dialog_theme_option_autumn_copper",
                    accentColor = Color(0xFFEA580C),
                    onClick = {
                      onCustomAccentColorChanged(null)
                      onThemeModeChanged(AppThemeMode.AUTUMN_COPPER)
                      ThemePreferences.setSavedThemeMode(context, AppThemeMode.AUTUMN_COPPER)
                      ThemePreferences.setSavedCustomAccentColor(context, null)
                    }
                  )
                }

                item {
                  ThemeOptionRow(
                    title = "Crimson Fury",
                    subtitle = "Aggressive dark charcoal backing with striking crimson red (#DC2626) structural borders.",
                    selected = currentThemeMode == AppThemeMode.CRIMSON_FURY && customAccentColor == null,
                    tag = "dialog_theme_option_crimson_fury",
                    accentColor = Color(0xFFDC2626),
                    onClick = {
                      onCustomAccentColorChanged(null)
                      onThemeModeChanged(AppThemeMode.CRIMSON_FURY)
                      ThemePreferences.setSavedThemeMode(context, AppThemeMode.CRIMSON_FURY)
                      ThemePreferences.setSavedCustomAccentColor(context, null)
                    }
                  )
                }

                item {
                  ThemeOptionRow(
                    title = "Neon Synthwave",
                    subtitle = "Dark retro violet canvas (#1E1B4B) with radiant hot pink (#F43F5E) accent rings.",
                    selected = currentThemeMode == AppThemeMode.NEON_SYNTHWAVE && customAccentColor == null,
                    tag = "dialog_theme_option_neon_synthwave",
                    accentColor = Color(0xFFF43F5E),
                    onClick = {
                      onCustomAccentColorChanged(null)
                      onThemeModeChanged(AppThemeMode.NEON_SYNTHWAVE)
                      ThemePreferences.setSavedThemeMode(context, AppThemeMode.NEON_SYNTHWAVE)
                      ThemePreferences.setSavedCustomAccentColor(context, null)
                    }
                  )
                }

                item {
                  ThemeOptionRow(
                    title = "Solar Flare",
                    subtitle = "Muted ash-gray backdrop featuring an intense energetic orange (#F97316) primary layout accent.",
                    selected = currentThemeMode == AppThemeMode.SOLAR_FLARE && customAccentColor == null,
                    tag = "dialog_theme_option_solar_flare",
                    accentColor = Color(0xFFF97316),
                    onClick = {
                      onCustomAccentColorChanged(null)
                      onThemeModeChanged(AppThemeMode.SOLAR_FLARE)
                      ThemePreferences.setSavedThemeMode(context, AppThemeMode.SOLAR_FLARE)
                      ThemePreferences.setSavedCustomAccentColor(context, null)
                    }
                  )
                }

                item {
                  ThemeOptionRow(
                    title = "Desert Sage",
                    subtitle = "A clean, earthy pale olive-green backdrop canvas (#2F3E36) with warm cream typography accents.",
                    selected = currentThemeMode == AppThemeMode.DESERT_SAGE && customAccentColor == null,
                    tag = "dialog_theme_option_desert_sage",
                    accentColor = Color(0xFF84CC16),
                    onClick = {
                      onCustomAccentColorChanged(null)
                      onThemeModeChanged(AppThemeMode.DESERT_SAGE)
                      ThemePreferences.setSavedThemeMode(context, AppThemeMode.DESERT_SAGE)
                      ThemePreferences.setSavedCustomAccentColor(context, null)
                    }
                  )
                }

                item {
                  Button(
                    onClick = { showColorPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                  ) {
                    Icon(Icons.Default.ColorLens, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Dynamic Color Wheel")
                  }
                }
              }
            }

            else -> {
              var isUnlocked by remember { mutableStateOf(DashboardPreferences.isEditModeUnlocked(context)) }
              Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Dashboard Customization", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(
                  "Toggle edit mode to rearrange widgets and switch card sizes directly on the dashboard screen.",
                  style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("Unlock Edit Mode", fontWeight = FontWeight.Bold)
                  Switch(
                    checked = isUnlocked,
                    onCheckedChange = {
                      isUnlocked = it
                      DashboardPreferences.setEditModeUnlocked(context, it)
                    }
                  )
                }
              }
            }
          }
        }

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Done")
        }
      }
    }
  }
}

@Composable
fun SettingsDialog(
  currentThemeMode: AppThemeMode,
  onThemeModeChanged: (AppThemeMode) -> Unit,
  customAccentColor: Color?,
  onCustomAccentColorChanged: (Color?) -> Unit,
  storageMetrics: FileManager.StorageMetrics? = null,
  onRefreshStorage: (() -> Unit)? = null,
  onDismiss: () -> Unit
) {
  ConfigurationsDialog(
    currentThemeMode = currentThemeMode,
    onThemeModeChanged = onThemeModeChanged,
    customAccentColor = customAccentColor,
    onCustomAccentColorChanged = onCustomAccentColorChanged,
    storageMetrics = storageMetrics,
    onRefreshStorage = onRefreshStorage,
    onDismiss = onDismiss
  )
}

@Composable
fun SearchConfigDialog(
  searchOptions: SearchOptions,
  onSearchOptionsChanged: (SearchOptions) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val smartSearchLabelText = "Smart Search"
  val smartSearchDescriptionText = "Executes multi-attribute file metadata and size descriptor queries completely offline."
  var isSmartSearchActive by remember { mutableStateOf(ThemePreferences.isSmartSearchEnabled(context)) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("search_config_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Text(
            text = "Search Toolbar & Smart Query Engine",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }

        HorizontalDivider()

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Search Bar Location", fontWeight = FontWeight.SemiBold)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = searchOptions.location == SearchLocation.TOP_TOOLBAR,
              onClick = { onSearchOptionsChanged(searchOptions.copy(location = SearchLocation.TOP_TOOLBAR)) },
              label = { Text("Top Toolbar") }
            )
            FilterChip(
              selected = searchOptions.location == SearchLocation.BOTTOM_BAR,
              onClick = { onSearchOptionsChanged(searchOptions.copy(location = SearchLocation.BOTTOM_BAR)) },
              label = { Text("Bottom Navigation") }
            )
          }

          HorizontalDivider()

          Text("Search Input Style", fontWeight = FontWeight.SemiBold)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = searchOptions.style == SearchStyle.EXPANDED_BOX,
              onClick = { onSearchOptionsChanged(searchOptions.copy(style = SearchStyle.EXPANDED_BOX)) },
              label = { Text("Expanded Box") }
            )
            FilterChip(
              selected = searchOptions.style == SearchStyle.MINIMAL_ICON,
              onClick = { onSearchOptionsChanged(searchOptions.copy(style = SearchStyle.MINIMAL_ICON)) },
              label = { Text("Minimal Icon") }
            )
          }

          HorizontalDivider()

          // Smart Search Power-User Toggle Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(smartSearchLabelText, fontWeight = FontWeight.SemiBold)
              Text(
                smartSearchDescriptionText,
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
              )
            }
            Switch(
              checked = isSmartSearchActive,
              onCheckedChange = { isChecked ->
                isSmartSearchActive = isChecked
                ThemePreferences.setSmartSearchEnabled(context, isChecked)
                onSearchOptionsChanged(searchOptions.copy(deepTextSearch = isChecked))
              },
              modifier = Modifier.testTag("smart_search_switch_toggle")
            )
          }
        }

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Done")
        }
      }
    }
  }
}

@Composable
fun WallpaperEngineDialog(
  wallpaperConfig: WallpaperConfig,
  onWallpaperConfigChanged: (WallpaperConfig) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      try {
        context.contentResolver.takePersistableUriPermission(
          uri,
          Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
      } catch (_: Exception) {}
      onWallpaperConfigChanged(wallpaperConfig.copy(imageUri = uri, builtInPattern = null))
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
        .testTag("wallpaper_engine_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .fillMaxHeight(0.85f),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(Icons.Default.Wallpaper, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Text(
            text = "Custom Background Wallpaper Engine",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }

        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          item {
            Text(
              "System Built-In Backdrops (10 Available)",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
          }

          // 10 Built-In Minimalist Backdrops List
          items(BuiltInWallpaper.entries.size) { idx ->
            val pattern = BuiltInWallpaper.entries[idx]
            val isSelected = wallpaperConfig.builtInPattern == pattern && wallpaperConfig.imageUri == null

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onWallpaperConfigChanged(
                    wallpaperConfig.copy(builtInPattern = pattern, imageUri = null)
                  )
                }
                .testTag("built_in_wallpaper_${pattern.id}"),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
              ),
              border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color(0x33888888))
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "%02d. ${pattern.title}".format(idx + 1),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                  )
                  Text(
                    text = pattern.subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                  )
                }
                RadioButton(
                  selected = isSelected,
                  onClick = {
                    onWallpaperConfigChanged(
                      wallpaperConfig.copy(builtInPattern = pattern, imageUri = null)
                    )
                  }
                )
              }
            }
          }

          item {
            HorizontalDivider()
            Text(
              "Custom Image Backdrop",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
          }

          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = { photoPickerLauncher.launch("image/*") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Choose Image")
              }

              if (wallpaperConfig.imageUri != null || wallpaperConfig.builtInPattern != null) {
                OutlinedButton(
                  onClick = { onWallpaperConfigChanged(wallpaperConfig.copy(imageUri = null, builtInPattern = null)) },
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Reset")
                }
              }
            }
          }

          // Blur Radius Slider
          item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Legibility Blur Radius", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Text("${wallpaperConfig.blurRadiusDp.toInt()} dp", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
              }
              Slider(
                value = wallpaperConfig.blurRadiusDp,
                onValueChange = { onWallpaperConfigChanged(wallpaperConfig.copy(blurRadiusDp = it)) },
                valueRange = 0f..25f
              )
            }
          }

          // Dark Opacity Tint Overlay Slider
          item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Dark Opacity Overlay", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Text("${(wallpaperConfig.darkOverlayOpacity * 100).toInt()}%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
              }
              Slider(
                value = wallpaperConfig.darkOverlayOpacity,
                onValueChange = { onWallpaperConfigChanged(wallpaperConfig.copy(darkOverlayOpacity = it)) },
                valueRange = 0f..0.9f
              )
            }
          }
        }

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Done")
        }
      }
    }
  }
}

@Composable
fun ColorPickerDialog(
  initialColor: Color,
  onColorSelected: (Color) -> Unit,
  onDismiss: () -> Unit
) {
  var red by remember { mutableFloatStateOf(initialColor.red) }
  var green by remember { mutableFloatStateOf(initialColor.green) }
  var blue by remember { mutableFloatStateOf(initialColor.blue) }

  val currentColor = Color(red, green, blue)

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("color_picker_dialog"),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.Palette, contentDescription = null, tint = currentColor)
        Text("Dynamic Color Wheel Picker", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Color Preview Box
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(currentColor)
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Hex: #%02X%02X%02X".format(
              (red * 255).toInt(),
              (green * 255).toInt(),
              (blue * 255).toInt()
            ),
            color = if ((red * 0.299 + green * 0.587 + blue * 0.114) > 0.5) Color.Black else Color.White,
            fontWeight = FontWeight.Bold
          )
        }

        // Color Swatches Quick Select
        Text("Preset Color Swatches", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          val presets = listOf(
            Color(0xFFD0BCFF), // Lavender
            Color(0xFF00FF66), // Matrix Green
            Color(0xFFC084FC), // Deep Purple
            Color(0xFF38BDF8), // Ocean Blue
            Color(0xFFFF9500), // Orange
            Color(0xFFFF2D55)  // Pink/Red
          )
          presets.forEach { preset ->
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(preset)
                .clickable {
                  red = preset.red
                  green = preset.green
                  blue = preset.blue
                }
                .border(2.dp, if (currentColor == preset) MaterialTheme.colorScheme.onSurface else Color.Transparent, CircleShape)
            )
          }
        }

        HorizontalDivider()

        // RGB Sliders
        Text("RGB Color Tuning", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))

        Column {
          Text("Red: ${(red * 255).toInt()}", style = MaterialTheme.typography.labelSmall)
          Slider(value = red, onValueChange = { red = it }, valueRange = 0f..1f)
        }

        Column {
          Text("Green: ${(green * 255).toInt()}", style = MaterialTheme.typography.labelSmall)
          Slider(value = green, onValueChange = { green = it }, valueRange = 0f..1f)
        }

        Column {
          Text("Blue: ${(blue * 255).toInt()}", style = MaterialTheme.typography.labelSmall)
          Slider(value = blue, onValueChange = { blue = it }, valueRange = 0f..1f)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onColorSelected(currentColor)
          onDismiss()
        },
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("Apply Custom Color")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
private fun ThemeOptionRow(
  title: String,
  subtitle: String,
  selected: Boolean,
  tag: String,
  accentColor: Color,
  primaryTextColor: Color = Color.White,
  secondaryTextColor: Color = Color(0xFFE5E5EA),
  containerColor: Color = Color(0xCC1E1E22),
  onClick: () -> Unit
) {
  val effectiveContainer = if (selected) accentColor.copy(alpha = 0.22f) else containerColor
  val effectiveBorder = if (selected) accentColor else Color(0x33FFFFFF)

  Card(
    onClick = onClick,
    modifier = Modifier
      .fillMaxWidth()
      .testTag(tag)
      .border(if (selected) 2.dp else 1.dp, effectiveBorder, RoundedCornerShape(12.dp)),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = effectiveContainer)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      RadioButton(
        selected = selected,
        onClick = onClick,
        colors = RadioButtonDefaults.colors(
          selectedColor = accentColor,
          unselectedColor = secondaryTextColor.copy(alpha = 0.6f)
        )
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
          title,
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.bodyMedium,
          color = primaryTextColor
        )
        Text(
          subtitle,
          style = MaterialTheme.typography.labelSmall.copy(
            color = secondaryTextColor
          )
        )
      }
    }
  }
}

@Composable
fun EnvironmentalEngineDialog(
  config: EnvironmentalBackdropConfig,
  onConfigChanged: (EnvironmentalBackdropConfig) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val currentCalendar = remember { Calendar.getInstance() }
  val systemResolvedSeason = remember { EnvironmentalSeason.resolveCurrentSystemSeason(currentCalendar) }
  val monthNames = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
  val currentMonthName = monthNames.getOrElse(currentCalendar.get(Calendar.MONTH)) { "Current Month" }
  val currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH)
  val currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
  val currentMinute = currentCalendar.get(Calendar.MINUTE)

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp)
        .testTag("environmental_engine_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .fillMaxHeight(0.92f),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(Icons.Default.WbSunny, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Column {
            Text(
              text = "Environmental Backdrop Engine",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Real-time clock seasons (Winter Icy Blue, Summer Fluid Sky, Autumn Copper, Spring Emerald, Tails' Mechanical Sandbox) + 60% Dark Glass Masks",
              style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
          }
        }

        HorizontalDivider()

        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // System Clock & Real-time Auto-Detection Card
          item {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text("Local System Clock", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                  }
                  Text(
                    String.format("%s %d, %02d:%02d", currentMonthName, currentDay, currentHour, currentMinute),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                  )
                }
                Text(
                  "Resolved Auto-Season: ${systemResolvedSeason.displayName}",
                  style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
              }
            }
          }

          // Season State Selection
          item {
            Text("Season Visual Profiles", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
          }

          items(EnvironmentalSeason.entries.size) { index ->
            val season = EnvironmentalSeason.entries[index]
            val isSelected = config.selectedSeason == season
            Card(
              onClick = {
                val newConfig = config.copy(selectedSeason = season)
                onConfigChanged(newConfig)
                EnvironmentalPreferences.saveConfig(context, newConfig)
              },
              modifier = Modifier.fillMaxWidth().testTag("season_option_${season.id}"),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
              colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                RadioButton(
                  selected = isSelected,
                  onClick = {
                    val newConfig = config.copy(selectedSeason = season)
                    onConfigChanged(newConfig)
                    EnvironmentalPreferences.saveConfig(context, newConfig)
                  }
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(season.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                  Text(season.subtitle, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant), fontSize = 11.sp)
                }
              }
            }
          }

          // Dynamic Weather Controls
          item {
            HorizontalDivider()
            Text("Dynamic Weather Controls", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
          }

          // Particle Physics Switch
          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Particle Transforms", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                Text("Snowflake drifting, clouds, leaves & shimmer", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
              }
              Switch(
                checked = config.enableParticles,
                onCheckedChange = {
                  val newConfig = config.copy(enableParticles = it)
                  onConfigChanged(newConfig)
                  EnvironmentalPreferences.saveConfig(context, newConfig)
                }
              )
            }
          }

          // Cloud Opacity Slider
          item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Cloud Layer Opacity", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text("${(config.weatherReport.cloudOpacity * 100).toInt()}%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
              }
              Slider(
                value = config.weatherReport.cloudOpacity,
                onValueChange = {
                  val newConfig = config.copy(weatherReport = config.weatherReport.copy(cloudOpacity = it))
                  onConfigChanged(newConfig)
                  EnvironmentalPreferences.saveConfig(context, newConfig)
                },
                valueRange = 0.1f..1.0f
              )
            }
          }

          // Sunburst Expansion Pulse Slider
          item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Sunburst Expansion Intensity", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text(String.format("%.1fx", config.weatherReport.sunburstExpansion), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
              }
              Slider(
                value = config.weatherReport.sunburstExpansion,
                onValueChange = {
                  val newConfig = config.copy(weatherReport = config.weatherReport.copy(sunburstExpansion = it))
                  onConfigChanged(newConfig)
                  EnvironmentalPreferences.saveConfig(context, newConfig)
                },
                valueRange = 0.6f..1.6f
              )
            }
          }

          // Contrast Compatibility Mask Badge
          item {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0x99000000)),
              border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                Column {
                  Text("Contrast Compatibility Enforced", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Color.White)
                  Text("Real-time clock seasons (Winter Icy Blue, Summer Fluid Sky, Autumn Copper, Spring Emerald, Tails' Mechanical Sandbox) + 60% Dark Glass Masks", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFE2E8F0)), fontSize = 10.sp)
                }
              }
            }
          }
        }

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Done")
        }
      }
    }
  }
}

