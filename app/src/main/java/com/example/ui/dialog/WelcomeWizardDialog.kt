package com.jackattackk246.files.ui.dialog

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.jackattackk246.files.model.BuiltInWallpaper
import com.jackattackk246.files.model.WallpaperConfig
import com.jackattackk246.files.ui.theme.AppThemeMode
import com.jackattackk246.files.ui.theme.ThemeManager
import com.jackattackk246.files.ui.wallpaper.BuiltInWallpaperBackdrop
import com.jackattackk246.files.util.HapticManager
import com.jackattackk246.files.util.ThemePreferences
import com.jackattackk246.files.util.UserProfilePreferences

enum class WelcomeTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
  PROFILE("Profile", Icons.Default.Person),
  PERMISSIONS("Permissions", Icons.Default.Security),
  THEME("Theme", Icons.Default.Palette),
  WALLPAPER("Background", Icons.Default.Wallpaper)
}

@Composable
fun WelcomeWizardDialog(
  initialTheme: AppThemeMode,
  initialWallpaper: WallpaperConfig,
  onComplete: (
    userName: String,
    language: String,
    region: String,
    selectedTheme: AppThemeMode,
    selectedWallpaper: WallpaperConfig
  ) -> Unit
) {
  val context = LocalContext.current
  val systemLang = java.util.Locale.getDefault().displayLanguage.let { if (it.isBlank()) "English (US)" else it }

  var currentTab by remember { mutableStateOf(WelcomeTab.PROFILE) }
  var userName by remember { mutableStateOf(UserProfilePreferences.getUserName(context).let { if (it == "User") "" else it }) }
  var selectedLanguage by remember { mutableStateOf(UserProfilePreferences.getLanguage(context).let { if (it.isBlank() || it == "English (US)") systemLang else it }) }
  var selectedRegion by remember { mutableStateOf(UserProfilePreferences.getRegion(context).let { if (it == "United States") "" else it }) }
  var selectedTheme by remember { mutableStateOf(initialTheme) }
  var selectedWallpaper by remember { mutableStateOf(initialWallpaper) }

  // Permissions state
  fun checkStoragePermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      Environment.isExternalStorageManager()
    } else {
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_EXTERNAL_STORAGE
      ) == PackageManager.PERMISSION_GRANTED
    }
  }

  var isPermissionGranted by remember { mutableStateOf(checkStoragePermission()) }

  val manageStorageLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) {
    isPermissionGranted = checkStoragePermission()
  }

  val legacyPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) {
    isPermissionGranted = checkStoragePermission()
  }

  fun requestPermission() {
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
      legacyPermissionLauncher.launch(
        arrayOf(
          Manifest.permission.READ_EXTERNAL_STORAGE,
          Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
      )
    }
  }

  val accentColor = ThemeManager.getThemeAccentColor(selectedTheme)

  Dialog(
    onDismissRequest = {},
    properties = DialogProperties(
      dismissOnBackPress = false,
      dismissOnClickOutside = false,
      usePlatformDefaultWidth = false
    )
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF0D0F14))
        .testTag("welcome_wizard_dialog"),
      color = Color(0xFF0D0F14)
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        // Dynamic Live Wallpaper Background Preview
        if (selectedWallpaper.builtInPattern != null) {
          BuiltInWallpaperBackdrop(
            pattern = selectedWallpaper.builtInPattern!!,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(ThemeManager.getThemeVerticalGradient(selectedTheme))
          )
        }

        // Semi-transparent overlay to ensure readability
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC0D0F14))
        )

        Column(
          modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Top Header Branding
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(alpha = 0.2f))
                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "Files Icon",
                tint = accentColor,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Files",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Personal Setup & Experience Wizard",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Fluid Scrollable Tab Row Component
          ScrollableTabRow(
            selectedTabIndex = currentTab.ordinal,
            edgePadding = 0.dp,
            containerColor = Color(0xFF1C1D22),
            contentColor = Color(0xFF00E5FF),
            indicator = { tabPositions ->
              if (currentTab.ordinal < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                  Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                  color = Color(0xFF00E5FF)
                )
              }
            },
            divider = {},
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .border(1.dp, Color(0xFF2C2D35), RoundedCornerShape(16.dp))
          ) {
            WelcomeTab.entries.forEach { tab ->
              val isSelected = currentTab == tab
              Tab(
                selected = isSelected,
                onClick = {
                  HapticManager.navigationClick(context)
                  currentTab = tab
                },
                modifier = Modifier
                  .wrapContentWidth()
                  .testTag("welcome_tab_${tab.name.lowercase()}"),
                text = {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                  ) {
                    Icon(
                      imageVector = tab.icon,
                      contentDescription = tab.title,
                      tint = if (isSelected) Color(0xFF00E5FF) else Color(0xFF64748B),
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = tab.title,
                      fontSize = 13.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = if (isSelected) Color.White else Color(0xFF94A3B8),
                      maxLines = 1,
                      softWrap = false,
                      overflow = TextOverflow.Visible
                    )
                  }
                }
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Content Pane for Selected Tab
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
              .clip(RoundedCornerShape(20.dp))
              .background(Color(0xFF161A23).copy(alpha = 0.92f))
              .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
              .padding(16.dp)
          ) {
            when (currentTab) {
              WelcomeTab.PROFILE -> ProfileTabContent(
                userName = userName,
                onUserNameChange = { userName = it },
                selectedLanguage = selectedLanguage,
                onLanguageChange = { selectedLanguage = it },
                selectedRegion = selectedRegion,
                onRegionChange = { selectedRegion = it },
                accentColor = accentColor
              )
              WelcomeTab.PERMISSIONS -> PermissionsTabContent(
                isPermissionGranted = isPermissionGranted,
                onRequestPermission = { requestPermission() },
                accentColor = accentColor
              )
              WelcomeTab.THEME -> ThemeTabContent(
                selectedTheme = selectedTheme,
                onThemeSelect = { selectedTheme = it },
                accentColor = accentColor
              )
              WelcomeTab.WALLPAPER -> WallpaperTabContent(
                selectedWallpaper = selectedWallpaper,
                onWallpaperChange = { selectedWallpaper = it },
                accentColor = accentColor
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Bottom Stepper & Completion Bar
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 4.dp)
              .navigationBarsPadding()
              .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Previous button
            if (currentTab.ordinal > 0) {
              OutlinedButton(
                onClick = {
                  val prevIndex = currentTab.ordinal - 1
                  currentTab = WelcomeTab.entries[prevIndex]
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                  brush = androidx.compose.ui.graphics.SolidColor(Color(0x44FFFFFF))
                ),
                modifier = Modifier
                  .height(48.dp)
                  .testTag("welcome_prev_button")
              ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Back")
              }
            } else {
              Spacer(modifier = Modifier.width(1.dp))
            }

            // Step Dots
            Row(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              WelcomeTab.entries.forEach { tab ->
                val isActive = currentTab == tab
                Box(
                  modifier = Modifier
                    .size(if (isActive) 24.dp else 8.dp, 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isActive) accentColor else Color(0x33FFFFFF))
                )
              }
            }

            // Next / Finish button
            Button(
              onClick = {
                if (currentTab.ordinal < WelcomeTab.entries.size - 1) {
                  val nextIndex = currentTab.ordinal + 1
                  currentTab = WelcomeTab.entries[nextIndex]
                } else {
                  // Validate mandatory storage permission for setup completion
                  val hasStoragePermission = checkStoragePermission()
                  if (!hasStoragePermission) {
                    com.jackattackk246.files.util.HapticManager.errorPulse(context)
                    android.widget.Toast.makeText(
                      context,
                      "Access Denied. You must grant All Files Access inside System Settings to complete the setup and initialize your storage monitoring dashboard panels.",
                      android.widget.Toast.LENGTH_LONG
                    ).show()
                    currentTab = WelcomeTab.PERMISSIONS
                    return@Button
                  }

                  // Finish onboarding
                  UserProfilePreferences.setUserName(context, userName.trim())
                  UserProfilePreferences.setLanguage(context, selectedLanguage)
                  UserProfilePreferences.setRegion(context, selectedRegion)
                  UserProfilePreferences.setOnboardingCompleted(context, true)
                  context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("setup_completed", true)
                    .apply()
                  ThemePreferences.setSavedThemeMode(context, selectedTheme)
                  onComplete(
                    userName.trim(),
                    selectedLanguage,
                    selectedRegion,
                    selectedTheme,
                    selectedWallpaper
                  )
                }
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = Color.White
              ),
              modifier = Modifier
                .height(48.dp)
                .testTag("welcome_next_button")
            ) {
              Text(
                text = if (currentTab.ordinal == WelcomeTab.entries.size - 1) "Get Started" else "Next",
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = if (currentTab.ordinal == WelcomeTab.entries.size - 1) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ProfileTabContent(
  userName: String,
  onUserNameChange: (String) -> Unit,
  selectedLanguage: String,
  onLanguageChange: (String) -> Unit,
  selectedRegion: String,
  onRegionChange: (String) -> Unit,
  accentColor: Color
) {
  var languageExpanded by remember { mutableStateOf(false) }
  var regionExpanded by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Text(
        text = "Personalize Your Profile",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Text(
        text = "Set your nickname, language preference, and home region for Files.",
        fontSize = 13.sp,
        color = Color(0xFF94A3B8)
      )
    }

    // User Name Field
    item {
      Column {
        Text(
          text = "Username (optional)",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFFE2E8F0)
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = userName,
          onValueChange = onUserNameChange,
          placeholder = { Text("Username (optional)", color = Color(0xFF64748B)) },
          leadingIcon = {
            Icon(Icons.Default.Person, contentDescription = null, tint = accentColor)
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("welcome_name_input"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            unfocusedBorderColor = Color(0x44FFFFFF),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color(0xFF0F1218),
            unfocusedContainerColor = Color(0xFF0F1218)
          )
        )
      }
    }

    // Language Dropdown Selector
    item {
      Column {
        Text(
          text = "Display Language",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFFE2E8F0)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(
            onClick = { languageExpanded = true },
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .testTag("welcome_language_selector"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = Color(0xFF0F1218),
              contentColor = Color.White
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
              brush = androidx.compose.ui.graphics.SolidColor(Color(0x44FFFFFF))
            )
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Language, contentDescription = null, tint = accentColor)
                Spacer(modifier = Modifier.width(10.dp))
                Text(selectedLanguage, color = Color.White, fontWeight = FontWeight.Medium)
              }
              Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF94A3B8))
            }
          }

          DropdownMenu(
            expanded = languageExpanded,
            onDismissRequest = { languageExpanded = false },
            modifier = Modifier
              .background(Color(0xFF141418))
              .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
          ) {
            UserProfilePreferences.availableLanguages.forEach { lang ->
              DropdownMenuItem(
                text = {
                  Text(
                    text = lang,
                    color = if (lang == selectedLanguage) accentColor else Color.White,
                    fontWeight = if (lang == selectedLanguage) FontWeight.Bold else FontWeight.Normal
                  )
                },
                onClick = {
                  onLanguageChange(lang)
                  languageExpanded = false
                }
              )
            }
          }
        }
      }
    }

    // Region Dropdown Selector
    item {
      Column {
        Text(
          text = "Region / Format",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFFE2E8F0)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(
            onClick = { regionExpanded = true },
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .testTag("welcome_region_selector"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = Color(0xFF0F1218),
              contentColor = Color.White
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
              brush = androidx.compose.ui.graphics.SolidColor(Color(0x44FFFFFF))
            )
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Public, contentDescription = null, tint = accentColor)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = selectedRegion.ifBlank { "Select Region (optional)" },
                  color = if (selectedRegion.isBlank()) Color(0xFF64748B) else Color.White,
                  fontWeight = FontWeight.Medium
                )
              }
              Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF94A3B8))
            }
          }

          DropdownMenu(
            expanded = regionExpanded,
            onDismissRequest = { regionExpanded = false },
            modifier = Modifier
              .background(Color(0xFF141418))
              .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
          ) {
            UserProfilePreferences.availableRegions.forEach { reg ->
              DropdownMenuItem(
                text = {
                  Text(
                    text = reg,
                    color = if (reg == selectedRegion) accentColor else Color.White,
                    fontWeight = if (reg == selectedRegion) FontWeight.Bold else FontWeight.Normal
                  )
                },
                onClick = {
                  onRegionChange(reg)
                  regionExpanded = false
                }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PermissionsTabContent(
  isPermissionGranted: Boolean,
  onRequestPermission: () -> Unit,
  accentColor: Color
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Text(
        text = "Device Storage Permissions",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Text(
        text = "Files works completely on your local device. Storage permission allows the app to browse, create, move, compress, and organize your files.",
        fontSize = 13.sp,
        color = Color(0xFF94A3B8),
        lineHeight = 18.sp
      )
    }

    // Permission Status Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .border(
            1.dp,
            if (isPermissionGranted) Color(0x4410B981) else Color(0x44F59E0B),
            RoundedCornerShape(16.dp)
          ),
        colors = CardDefaults.cardColors(
          containerColor = if (isPermissionGranted) Color(0x1A10B981) else Color(0x1AF59E0B)
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(if (isPermissionGranted) Color(0xFF10B981) else Color(0xFFF59E0B)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(28.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (isPermissionGranted) "All Files Access Granted" else "Permission Required",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = if (isPermissionGranted) "Full storage reading and writing active." else "Tap the button below to allow full storage access.",
              fontSize = 12.sp,
              color = Color(0xFFCBD5E1)
            )
          }
        }
      }
    }

    // Permission Action Button
    item {
      Button(
        onClick = onRequestPermission,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("welcome_grant_permission_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isPermissionGranted) Color(0xFF1E293B) else accentColor,
          contentColor = Color.White
        )
      ) {
        Icon(
          imageVector = if (isPermissionGranted) Icons.Default.Settings else Icons.Default.Key,
          contentDescription = null,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = if (isPermissionGranted) "Open System Storage Settings" else "Grant Storage Access",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
      }
    }

    // Privacy Badge Note
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1218))
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "100% Offline & Private: No analytics, tracking, or cloud storage syncing.",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
          )
        }
      }
    }
  }
}

@Composable
private fun ThemeTabContent(
  selectedTheme: AppThemeMode,
  onThemeSelect: (AppThemeMode) -> Unit,
  accentColor: Color
) {
  Column(modifier = Modifier.fillMaxSize()) {
    Text(
      text = "Select Interface Theme",
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      color = Color.White
    )
    Text(
      text = "Choose your favorite visual theme. You can also customize colors later in Settings.",
      fontSize = 13.sp,
      color = Color(0xFF94A3B8)
    )

    Spacer(modifier = Modifier.height(12.dp))

    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.weight(1f)
    ) {
      items(AppThemeMode.entries) { theme ->
        val isSelected = selectedTheme == theme
        val themeAccent = ThemeManager.getThemeAccentColor(theme)

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onThemeSelect(theme) }
            .border(
              width = if (isSelected) 2.dp else 1.dp,
              color = if (isSelected) themeAccent else Color(0x22FFFFFF),
              shape = RoundedCornerShape(12.dp)
            )
            .testTag("welcome_theme_item_${theme.id}"),
          colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF222834) else Color(0xFF0F1218)
          )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(themeAccent)
              )
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = "Selected",
                  tint = themeAccent,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = theme.displayName,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }
  }
}

@Composable
private fun WallpaperTabContent(
  selectedWallpaper: WallpaperConfig,
  onWallpaperChange: (WallpaperConfig) -> Unit,
  accentColor: Color
) {
  Column(modifier = Modifier.fillMaxSize()) {
    Text(
      text = "Choose Background Style",
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      color = Color.White
    )
    Text(
      text = "Select a built-in abstract pattern or sleek theme gradient.",
      fontSize = 13.sp,
      color = Color(0xFF94A3B8)
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Default Gradient Option
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .clickable {
          onWallpaperChange(selectedWallpaper.copy(imageUri = null, builtInPattern = null))
        }
        .border(
          width = if (!selectedWallpaper.hasWallpaper) 2.dp else 1.dp,
          color = if (!selectedWallpaper.hasWallpaper) accentColor else Color(0x22FFFFFF),
          shape = RoundedCornerShape(12.dp)
        )
        .testTag("welcome_wallpaper_default"),
      colors = CardDefaults.cardColors(
        containerColor = if (!selectedWallpaper.hasWallpaper) Color(0xFF222834) else Color(0xFF0F1218)
      )
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Gradient, contentDescription = null, tint = accentColor)
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text("Default Theme Gradient", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            Text("Clean modern dark gradient with theme accents", color = Color(0xFF94A3B8), fontSize = 12.sp)
          }
        }
        if (!selectedWallpaper.hasWallpaper) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = accentColor)
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
      text = "Built-In Wallpaper Textures",
      fontSize = 13.sp,
      fontWeight = FontWeight.Medium,
      color = Color(0xFFCBD5E1)
    )

    Spacer(modifier = Modifier.height(8.dp))

    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.weight(1f)
    ) {
      items(BuiltInWallpaper.entries) { wp ->
        val isSelected = selectedWallpaper.builtInPattern == wp

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable {
              onWallpaperChange(selectedWallpaper.copy(imageUri = null, builtInPattern = wp))
            }
            .border(
              width = if (isSelected) 2.dp else 1.dp,
              color = if (isSelected) accentColor else Color(0x22FFFFFF),
              shape = RoundedCornerShape(12.dp)
            )
            .testTag("welcome_wallpaper_${wp.id}"),
          colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF222834) else Color(0xFF0F1218)
          )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Image, contentDescription = null, tint = if (isSelected) accentColor else Color(0xFF64748B), modifier = Modifier.size(20.dp))
              if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(wp.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp, maxLines = 1)
            Text(wp.subtitle, color = Color(0xFF94A3B8), fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
          }
        }
      }
    }
  }
}
