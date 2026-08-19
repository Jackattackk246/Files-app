package com.jackattackk246.files

// ============================================================================
// 🔒 ARCHITECT NOTICE: OPEN-SOURCE INTEGRITY & CREATIVITY GATEWAY
// Designed by Jack Lawton | Repository: Jackattackk246/Files
// ============================================================================
// If you are here to upgrade the app feel free to, but if you are just copy 
// and pasting don't bother.
// 
// Feel free to use the security system in another app, feel free to just credit.
//
// NOTE: This application contains an automated Single-Line Creativity Pass. 
// Direct clones or cosmetic name/icon swaps with zero code/layout modifications 
// will trigger an immediate UI freeze, displaying: 
// "Lacking creativity. You don't get the app."
// ============================================================================

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.withFrameNanos
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.compose.AsyncImage
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.ui.dialog.DeleteConfirmationDialog
import com.example.ui.MediaPlayerStudio
import com.example.ui.NearbyDevicesScreen
import com.jackattackk246.files.model.*
import com.jackattackk246.files.security.DeveloperSecurityEngine
import com.jackattackk246.files.ui.*
import com.jackattackk246.files.ui.wallpaper.BuiltInWallpaperBackdrop
import com.jackattackk246.files.ui.theme.AppThemeMode
import com.jackattackk246.files.ui.theme.MyApplicationTheme
import com.jackattackk246.files.ui.theme.ThemeManager
import com.jackattackk246.files.util.EnvironmentalPreferences
import com.jackattackk246.files.util.FileManager
import com.jackattackk246.files.util.GyroscopeParallaxEngine
import com.jackattackk246.files.util.HapticFeedbackHelper
import com.jackattackk246.files.util.RecentFilesTracker
import com.jackattackk246.files.util.RecycleBinEngine
import com.jackattackk246.files.util.ThemePreferences
import com.jackattackk246.files.util.UserProfilePreferences
import com.jackattackk246.files.ui.dialog.WelcomeWizardDialog
import com.jackattackk246.files.ui.TutorialOverlay
import com.jackattackk246.files.ui.wizard.SetupWizardOnboardingView
import android.view.View
import com.jackattackk246.files.ui.viewer.ProtectedPathDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@Composable
fun WearableEmulationCanvasWrapper(
  accentColor: Color,
  onNavigateBack: () -> Unit,
  onExitMockMode: () -> Unit,
  content: @Composable () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black),
    contentAlignment = androidx.compose.ui.Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .sizeIn(maxWidth = 360.dp, maxHeight = 360.dp)
        .aspectRatio(1f)
        .clip(CircleShape)
        .border(3.dp, accentColor, CircleShape)
        .background(Color(0xFF101116))
    ) {
      content()

      // Wearable Hardware Navigation: Top-center floating round 'Back' chevron button
      IconButton(
        onClick = onNavigateBack,
        modifier = Modifier
          .align(androidx.compose.ui.Alignment.TopCenter)
          .padding(top = 10.dp)
          .size(32.dp)
          .background(Color.Black.copy(alpha = 0.85f), CircleShape)
          .border(1.dp, accentColor, CircleShape)
          .testTag("wearable_hardware_back_button")
      ) {
        Icon(
          imageVector = Icons.Default.ArrowBack,
          contentDescription = "Wearable Hardware Back",
          tint = accentColor,
          modifier = Modifier.size(18.dp)
        )
      }

      // Rotary Scroll Circle Crown Bezel
      Box(
        modifier = Modifier
          .align(androidx.compose.ui.Alignment.CenterEnd)
          .padding(end = 4.dp)
          .width(22.dp)
          .height(110.dp)
          .clip(RoundedCornerShape(11.dp))
          .background(Color.Black.copy(alpha = 0.6f))
          .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(11.dp))
          .pointerInput(Unit) {
            detectVerticalDragGestures { change, _ ->
              change.consume()
            }
          },
        contentAlignment = androidx.compose.ui.Alignment.Center
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(4.dp),
          horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
          repeat(6) {
            Box(
              modifier = Modifier
                .width(12.dp)
                .height(2.dp)
                .background(accentColor.copy(alpha = 0.8f), RoundedCornerShape(1.dp))
            )
          }
        }
      }
    }

    // Persistent Exit Mock Mode Floating Action Chip
    Surface(
      onClick = onExitMockMode,
      color = Color(0xFFFF3B30),
      shape = RoundedCornerShape(20.dp),
      border = BorderStroke(1.dp, Color.White),
      modifier = Modifier
        .align(androidx.compose.ui.Alignment.BottomCenter)
        .padding(bottom = 20.dp)
        .testTag("exit_wearable_mock_chip")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Text("EXIT MOCK MODE (Wearable)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }
    }
  }
}

@Composable
fun AppCanvasLayoutWrapper(
  isWearableMockActive: Boolean,
  accentColor: Color,
  onNavigateBack: () -> Unit,
  onExitMockMode: () -> Unit,
  content: @Composable () -> Unit
) {
  if (isWearableMockActive) {
    WearableEmulationCanvasWrapper(
      accentColor = accentColor,
      onNavigateBack = onNavigateBack,
      onExitMockMode = onExitMockMode,
      content = content
    )
  } else {
    content()
  }
}

class MainActivity : ComponentActivity(), ImageLoaderFactory {

  private val appImageLoader: ImageLoader by lazy {
    ImageLoader.Builder(applicationContext)
      .memoryCache {
        MemoryCache.Builder(applicationContext)
          .maxSizePercent(0.20)
          .strongReferencesEnabled(true)
          .build()
      }
      .diskCache {
        DiskCache.Builder()
          .directory(cacheDir.resolve("image_cache"))
          .maxSizePercent(0.02)
          .build()
      }
      .respectCacheHeaders(false)
      .allowHardware(false)
      .allowRgb565(false)
      .crossfade(true)
      .build()
  }

  override fun newImageLoader(): ImageLoader = appImageLoader

  override fun onTrimMemory(level: Int) {
    super.onTrimMemory(level)
    try {
      appImageLoader.memoryCache?.trimMemory(level)
      com.aistudio.fileslauncher.ui.ThemeSynchronizationBridge.onTrimMemory(level)
    } catch (_: Exception) {}
  }

  override fun onLowMemory() {
    super.onLowMemory()
    try {
      appImageLoader.memoryCache?.clear()
    } catch (_: Exception) {}
  }

  override fun onDestroy() {
    super.onDestroy()
    try {
      com.jackattackk246.files.ai.LocalOfflineAiModule.terminateThreads()
      com.jackattackk246.files.util.UsbStorageManager.terminateUsbJobs()
    } catch (_: Exception) {}
  }

  private fun getStyleResourceForTheme(themeId: String): Int {
    // Ensure all 100 choices return their absolute isolated resource ID map directly
    return when (themeId.uppercase()) {
      "THEME_1" -> R.style.Theme_Custom_1
      "THEME_16" -> R.style.Theme_Custom_16
      "THEME_100" -> R.style.Theme_Custom_100
      else -> {
        val number = themeId.filter { it.isDigit() }
        if (number.isNotEmpty()) {
          val resId = resources.getIdentifier("Theme_Custom_$number", "style", packageName)
          if (resId != 0) resId else R.style.Theme_Custom_1
        } else {
          R.style.Theme_Custom_1
        }
      }
    }
  }

  fun applyActiveThemeDetails() {
    val sharedPrefs = getSharedPreferences("launcher_prefs", MODE_PRIVATE)
    val activeThemeId = sharedPrefs.getString("selected_theme_preset", "NEON_RED") ?: "NEON_RED"

    val scrollView = findViewById<android.widget.ScrollView>(R.id.dashboard_scroll_view)
    val imagesIcon = findViewById<View>(R.id.media_icon_images)
    val audioIcon = findViewById<View>(R.id.media_icon_audio)
    val docsIcon = findViewById<View>(R.id.media_icon_docs)

    if (activeThemeId == "SAMSUNG_EXPERIENCE") {
      // =========================================================
      // 1. ENABLE RETRO KINDLE FIRE OVERSCROLL GLOW
      // =========================================================
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Forcefully changes modern elastic stretch back to the classic crescent glow shape
        try {
          val edgeEffectTypeMethod = scrollView?.javaClass?.getMethod("setEdgeEffectType", Int::class.javaPrimitiveType)
          edgeEffectTypeMethod?.invoke(scrollView, 0) // EdgeEffect.TYPE_GLOW = 0
        } catch (_: Exception) {}
      }
      // Tint the crescent shape to the exact transparent dark hue
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && scrollView != null) {
        try {
          val glowColor = android.graphics.Color.parseColor("#80000000")
          scrollView.topEdgeEffectColor = glowColor
          scrollView.bottomEdgeEffectColor = glowColor
        } catch (_: Exception) {}
      }

      // =========================================================
      // 2. ENABLE SAMSUNG EXPERIENCE ICON SQUIRCLES
      // =========================================================
      listOfNotNull(imagesIcon, audioIcon, docsIcon).forEach { iconView ->
        iconView.setBackgroundResource(R.drawable.samsung_experience_squircle)
      }
      // Apply authentic Dream UX color tints to the background shapes
      imagesIcon?.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF5B72"))
      audioIcon?.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#29B6F6"))
      docsIcon?.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#3B66F5"))

    } else {
      // =========================================================
      // DEFAULT FALLBACK FOR ALL OTHER THEMES
      // =========================================================
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Let other themes use standard modern system bounce mechanics
        try {
          val edgeEffectTypeMethod = scrollView?.javaClass?.getMethod("setEdgeEffectType", Int::class.javaPrimitiveType)
          edgeEffectTypeMethod?.invoke(scrollView, 1) // EdgeEffect.TYPE_STRETCH = 1
        } catch (_: Exception) {}
      }

      // Remove the custom shapes and colors so they reset cleanly
      listOfNotNull(imagesIcon, audioIcon, docsIcon).forEach { iconView ->
        iconView.background = null
        iconView.backgroundTintList = null
      }
    }
  }

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    val sharedPrefs = getSharedPreferences("launcher_prefs", MODE_PRIVATE)
    val activeThemeId = sharedPrefs.getString("selected_theme_preset", "THEME_1") ?: "THEME_1"

    // Enforce Anti-Clone & Two-Bit Variance Tolerance APK Integrity Check (with Wear OS Exemption Strip)
    com.jackattackk246.files.security.AntiCloneSecurityManager.enforceSecurityLockoutOrExit(applicationContext)

    // 1. CRITICAL: Completely decouple the window from standard theme caches
    // This forcibly prevents any elements of the original 15 themes from sticking around.
    theme.applyStyle(getStyleResourceForTheme(activeThemeId), true)

    super.onCreate(savedInstanceState)
    coil.Coil.setImageLoader(appImageLoader)
    enableEdgeToEdge()
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.statusBarColor = android.graphics.Color.TRANSPARENT
    window.navigationBarColor = android.graphics.Color.TRANSPARENT

    // Verify creativity pass & inspect clock integrity
    DeveloperSecurityEngine.verifyCreativityPass()
    DeveloperSecurityEngine.inspectClockIntegrity(applicationContext)

    // Initialize local offline AI tensor matrix engine
    com.jackattackk246.files.ai.LocalOfflineAiModule.initializeOfflineAi(applicationContext)

    // Initialize native USB OTG storage manager
    com.jackattackk246.files.util.UsbStorageManager.initialize(applicationContext)

    // Initialize Theme Synchronization Bridge with Compiler Whitelist Verification Pass
    com.aistudio.fileslauncher.ui.ThemeSynchronizationBridge.initialize(applicationContext)
    com.aistudio.fileslauncher.ui.ThemeSynchronizationBridge.verifyCompilerWhitelistBypass()

    // FORCE INJECTION: Pull the root decor window element directly from the OS layer
    val rootDecorWindowView: View = window.decorView.rootView
    
    // Hardcode the canvas background color strictly to a low-light dark workspace profile
    val midnightCharcoalCanvas = android.graphics.Color.parseColor("#0F1115")
    rootDecorWindowView.setBackgroundColor(midnightCharcoalCanvas)

    val isWatch = packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)
    setContent {
      if (isWatch) {
         com.jackattackk246.files.ui.WearDashboardScreen()
         return@setContent
      }

      val context = LocalContext.current
      val coroutineScope = rememberCoroutineScope()

      LaunchedEffect(Unit) {
        com.jackattackk246.files.util.DeveloperToolsManager.initHardwareProfile(context)
      }

      // Protected path intercept dialog target
      var protectedPathTarget by remember { mutableStateOf<File?>(null) }

      // 1. Persistent Multi-Theme State (Read synchronously from SharedPreferences)
      var themeMode by remember { mutableStateOf(ThemePreferences.getSavedThemeMode(context)) }
      var customAccentColor by remember { mutableStateOf(ThemePreferences.getSavedCustomAccentColor(context)) }
      var environmentalConfig by remember { mutableStateOf(EnvironmentalPreferences.getConfig(context)) }

      // 2. Manage External Storage & Permissions Startup Check
      var hasStoragePermission by remember {
        mutableStateOf(
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
          } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
          }
        )
      }

      val legacyPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
      ) { permissions ->
        hasStoragePermission = permissions.values.all { it }
      }

      val manageStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
      ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          hasStoragePermission = Environment.isExternalStorageManager()
          if (hasStoragePermission) {
            Toast.makeText(context, "Storage Access Granted", Toast.LENGTH_SHORT).show()
          }
        }
      }

      val launcherPrefs = remember { context.getSharedPreferences("launcher_prefs", android.content.Context.MODE_PRIVATE) }
      var setupCompleted by remember {
        mutableStateOf(launcherPrefs.getBoolean("setup_completed", false))
      }
      var showWelcomeWizardDialog by remember {
        mutableStateOf(!launcherPrefs.getBoolean("setup_completed", false))
      }

      val lifecycleOwner = LocalLifecycleOwner.current
      DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
          if (event == Lifecycle.Event.ON_RESUME) {
            val isCompleted = launcherPrefs.getBoolean("setup_completed", false)
            setupCompleted = isCompleted
            if (!isCompleted) {
              // 1. Force route to the welcome/onboarding wizard first
              showWelcomeWizardDialog = true
            }
          }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
          lifecycleOwner.lifecycle.removeObserver(observer)
        }
      }

      val usbSafLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
      ) { uri ->
        if (uri != null) {
          com.jackattackk246.files.util.UsbStorageManager.savePersistedTreeUri(context, uri)
        }
      }

      DisposableEffect(context) {
        val receiver = com.jackattackk246.files.util.UsbStorageBroadcastReceiver()
        val filter = android.content.IntentFilter().apply {
          addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED")
          addAction("android.hardware.usb.action.USB_DEVICE_DETACHED")
          addAction(android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED)
          addAction(android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED)
          addAction(Intent.ACTION_MEDIA_MOUNTED)
          addAction(Intent.ACTION_MEDIA_UNMOUNTED)
          addAction(Intent.ACTION_MEDIA_REMOVED)
          addAction(Intent.ACTION_MEDIA_EJECT)
          addDataScheme("file")
        }
        ContextCompat.registerReceiver(
          context,
          receiver,
          filter,
          ContextCompat.RECEIVER_EXPORTED
        )
        onDispose {
          try {
            context.unregisterReceiver(receiver)
          } catch (_: Exception) {}
        }
      }

      // Root Starting Directory
      val rootDir = remember { FileManager.getRootDirectory() }
      var currentDirectory by remember { mutableStateOf(rootDir) }
      val directoryHistory = remember { mutableStateListOf<File>() }
      var filesList by remember { mutableStateOf<List<FileItem>>(emptyList()) }
      var highlightFilePath by remember { mutableStateOf<String?>(null) }

      // Live Storage Hardware Metrics
      var storageMetrics by remember { mutableStateOf(FileManager.getStorageMetrics()) }

      fun refreshDirectoryFiles() {
        filesList = FileManager.listFiles(currentDirectory)
        storageMetrics = FileManager.getStorageMetrics()
      }

      LaunchedEffect(currentDirectory) {
        refreshDirectoryFiles()
      }

      // Navigation & Drawer State
      var selectedNavNode by remember { mutableStateOf(NavigationNode.DASHBOARD) }
      val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

      // Search and Wallpaper Engine State
      var searchQuery by remember { mutableStateOf("") }
      var searchOptions by remember {
        mutableStateOf(
          SearchOptions(
            deepTextSearch = ThemePreferences.isSmartSearchEnabled(context)
          )
        )
      }
      var wallpaperConfig by remember { mutableStateOf(WallpaperConfig()) }

      // Dialog States
      var showConfigurationsDialog by remember { mutableStateOf(false) }
      var showDevAuthDialog by remember { mutableStateOf(false) }
      var pendingActionAfterAuth by remember { mutableStateOf<(() -> Unit)?>(null) }
      var showSearchConfigDialog by remember { mutableStateOf(false) }
      var showWallpaperEngineDialog by remember { mutableStateOf(false) }
      var showEnvironmentalDialog by remember { mutableStateOf(false) }
      var showTutorialOverlay by remember(setupCompleted, showWelcomeWizardDialog) {
        mutableStateOf(setupCompleted && !showWelcomeWizardDialog && com.jackattackk246.files.util.DashboardPreferences.isFirstLaunchTutorialEnabled(context))
      }

      fun openConfigurationsProtected(onSuccess: () -> Unit) {
        val masterPrefs = context.getSharedPreferences("developer_tools_prefs", android.content.Context.MODE_PRIVATE)
        val isMasterAuthorized = masterPrefs.getBoolean("is_developer_authorized_master", false)
        if (isMasterAuthorized || DeveloperSecurityEngine.isDeveloperUnlocked(context)) {
          onSuccess()
        } else {
          pendingActionAfterAuth = onSuccess
          showDevAuthDialog = true
        }
      }

      var activeManageFileItem by remember { mutableStateOf<FileItem?>(null) }
      var activeMediaFileItem by remember { mutableStateOf<FileItem?>(null) }
      var isFromRecentsTabDialog by remember { mutableStateOf(false) }
      var activeAnalyticsFileItem by remember { mutableStateOf<FileItem?>(null) }
      var renameTargetItem by remember { mutableStateOf<FileItem?>(null) }
      var deleteConfirmationTarget by remember { mutableStateOf<FileItem?>(null) }
      var renameInputText by remember { mutableStateOf("") }
      var isCreateFolderDialogOpen by remember { mutableStateOf(false) }
      var newFolderNameInput by remember { mutableStateOf("") }

      // Clipboard state
      var clipboardFile by remember { mutableStateOf<FileItem?>(null) }
      var clipboardOperation by remember { mutableStateOf<String?>(null) }

      // Compression Progress Dialog state
      var isCompressingOrUnzipping by remember { mutableStateOf(false) }
      var compressionProgressRatio by remember { mutableFloatStateOf(0f) }
      var compressionStatusMsg by remember { mutableStateOf("") }

      // Snackbar state for undo and transient action notifications
      val snackbarHostState = remember { SnackbarHostState() }

      // Search scraper effect with offline AI vector similarity pipeline
      LaunchedEffect(searchQuery, searchOptions.deepTextSearch, searchOptions.currentDirOnly, currentDirectory) {
        if (searchQuery.isNotBlank()) {
          filesList = FileManager.searchFiles(
            rootFolder = currentDirectory,
            query = searchQuery,
            currentDirOnly = searchOptions.currentDirOnly,
            deepTextIndexing = searchOptions.deepTextSearch
          )
        } else {
          refreshDirectoryFiles()
        }
      }

      // 4. Native Hardware Back-Button / Back-Swipe Interceptors
      val isAnyDialogActive = activeManageFileItem != null ||
          activeAnalyticsFileItem != null ||
          renameTargetItem != null ||
          isCreateFolderDialogOpen ||
          showSearchConfigDialog ||
          showWallpaperEngineDialog ||
          showEnvironmentalDialog ||
          showConfigurationsDialog

      val canNavigateBackInExplorer = selectedNavNode == NavigationNode.EXPLORER &&
          (directoryHistory.isNotEmpty() || (currentDirectory.parentFile != null && currentDirectory.parentFile!!.exists() && currentDirectory.absolutePath != rootDir.absolutePath))

      val shouldInterceptBack = isAnyDialogActive ||
          drawerState.isOpen ||
          selectedNavNode != NavigationNode.DASHBOARD ||
          canNavigateBackInExplorer

      BackHandler(enabled = shouldInterceptBack) {
        when {
          drawerState.isOpen -> {
            coroutineScope.launch { drawerState.close() }
          }
          activeManageFileItem != null -> activeManageFileItem = null
          activeAnalyticsFileItem != null -> activeAnalyticsFileItem = null
          renameTargetItem != null -> renameTargetItem = null
          isCreateFolderDialogOpen -> isCreateFolderDialogOpen = false
          showSearchConfigDialog -> showSearchConfigDialog = false
          showWallpaperEngineDialog -> showWallpaperEngineDialog = false
          showEnvironmentalDialog -> showEnvironmentalDialog = false
          showConfigurationsDialog -> showConfigurationsDialog = false
          selectedNavNode == NavigationNode.EXPLORER -> {
            if (directoryHistory.isNotEmpty()) {
              val previousDir = directoryHistory.removeAt(directoryHistory.lastIndex)
              currentDirectory = previousDir
              refreshDirectoryFiles()
            } else if (currentDirectory.parentFile != null && currentDirectory.parentFile!!.exists() && currentDirectory.absolutePath != rootDir.absolutePath) {
              currentDirectory = currentDirectory.parentFile!!
              refreshDirectoryFiles()
            } else {
              selectedNavNode = NavigationNode.DASHBOARD
            }
          }
          selectedNavNode != NavigationNode.DASHBOARD -> {
            selectedNavNode = NavigationNode.DASHBOARD
          }
        }
      }

      // Navigation helpers
      fun navigateToDirectory(target: File) {
        // FORCE CONVERSION: Convert the entire file pathway string to pure lowercase
        val cleanPath = target.absolutePath.lowercase(Locale.ROOT)

        // ABSOLUTE SECURITY CHECK: Match exact system variations and directory strings
        val isDataPartition = cleanPath.contains("android/data")
        val isObbPartition = cleanPath.contains("android/obb")

        if (isDataPartition || isObbPartition) {
          if (!ThemePreferences.isDirectRootLaunchEnabled(context)) {
            // STOP ALL RENDER TASKS INSTANTLY: Do not pass go, do not let the folder view initialize
            protectedPathTarget = target
            return
          }
        }

        if (target.exists() && target.isDirectory) {
          if (currentDirectory != target) {
            directoryHistory.add(currentDirectory)
          }
          currentDirectory = target
          searchQuery = ""
          refreshDirectoryFiles()
        }
      }

      MyApplicationTheme(
        themeMode = themeMode,
        customAccentColor = customAccentColor,
        season = environmentalConfig.selectedSeason
      ) {
        val desktopPalette by com.aistudio.fileslauncher.ui.ThemeSynchronizationBridge.paletteState.collectAsState()
        val simulatedHardwareProfile by com.jackattackk246.files.util.DeveloperToolsManager.simulatedHardwareProfileState.collectAsState()
        val isWearableMockActive = simulatedHardwareProfile == "Force Wearable Layout (Ultra-Compact)"

        val adaptivePrimaryTextColor = com.jackattackk246.files.ui.theme.ThemeManager.getAdaptivePrimaryTextColor(
          themeMode = themeMode,
          season = environmentalConfig.selectedSeason
        )

        // Gyroscope-driven Parallax Offset for background canvas and UI depth
        val parallaxOffset = GyroscopeParallaxEngine.rememberParallaxOffset(enabled = true)

        // Canvas Layout Wrapper (Supports Wearable Hardware Simulation Overlay)
        AppCanvasLayoutWrapper(
          isWearableMockActive = isWearableMockActive,
          accentColor = ThemeManager.getThemeAccentColor(themeMode, customAccentColor),
          onNavigateBack = {
            if (directoryHistory.isNotEmpty()) {
              currentDirectory = directoryHistory.removeAt(directoryHistory.size - 1)
            } else {
              selectedNavNode = NavigationNode.DASHBOARD
            }
          },
          onExitMockMode = {
            com.jackattackk246.files.util.DeveloperToolsManager.setSimulatedHardwareProfile(context, "Default (Native Hardware Detection)")
            Toast.makeText(context, "Exited Wearable Mock Mode", Toast.LENGTH_SHORT).show()
          }
        ) {
          // Modal Navigation Drawer Wrapper
          ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true,
          drawerContent = {
            ModalDrawerSheet(
              modifier = Modifier
                .width(310.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                .border(
                  BorderStroke(
                    1.dp,
                    Color.Gray.copy(alpha = 0.2f)
                  ),
                  androidx.compose.foundation.shape.RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                ),
              drawerShape = androidx.compose.foundation.shape.RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
              drawerContainerColor = if (com.jackattackk246.files.ui.theme.ThemeManager.isLightBackgroundProfile(themeMode, environmentalConfig.selectedSeason)) {
                Color(0xEEFFFFFF)
              } else {
                com.jackattackk246.files.ui.theme.ThemeManager.GlassMaskCharcoal
              }
            ) {
              SidebarPanel(
                selectedNode = selectedNavNode,
                onNodeSelected = { node ->
                  selectedNavNode = node
                  coroutineScope.launch { drawerState.close() }
                },
                storageMetrics = storageMetrics,
                themeMode = themeMode,
                season = environmentalConfig.selectedSeason,
                onOpenConfigurationsDialog = {
                  coroutineScope.launch {
                    drawerState.close()
                    openConfigurationsProtected {
                      showConfigurationsDialog = true
                    }
                  }
                },
                onOpenSearchConfigDialog = {
                  coroutineScope.launch {
                    drawerState.close()
                    showSearchConfigDialog = true
                  }
                },
                onOpenWallpaperEngineDialog = {
                  coroutineScope.launch {
                    drawerState.close()
                    showWallpaperEngineDialog = true
                  }
                },
                onOpenEnvironmentalEngineDialog = {
                  coroutineScope.launch {
                    drawerState.close()
                    showEnvironmentalDialog = true
                  }
                },
                onOpenUsbStorage = { usbMountFile: File? ->
                  coroutineScope.launch {
                    drawerState.close()
                    if (usbMountFile != null && usbMountFile.exists()) {
                      currentDirectory = usbMountFile
                    }
                    selectedNavNode = NavigationNode.EXPLORER
                  }
                },
                onRequestUsbSafAuth = {
                  coroutineScope.launch {
                    drawerState.close()
                    usbSafLauncher.launch(null)
                  }
                }
              )
            }
          }
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(ThemeManager.getThemeVerticalGradient(themeMode, environmentalConfig.selectedSeason))
          ) {
            // Gyroscope-shifted Background Layer Container
            Box(
              modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                  translationX = parallaxOffset.backgroundX.dp.toPx()
                  translationY = parallaxOffset.backgroundY.dp.toPx()
                  scaleX = 1.08f
                  scaleY = 1.08f
                }
            ) {
              // 1. Dynamic Weather Canvas / Animated Environmental Engine Backdrop (Bleeds edge-to-edge behind status bar)
              if (ThemeManager.shouldMountBackdropCanvas(themeMode)) {
                AnimatedEnvironmentalBackground(
                  config = environmentalConfig,
                  modifier = Modifier.fillMaxSize()
                )
              }

              // 2. Custom Wallpaper Engine Background Layer
              if (wallpaperConfig.hasWallpaper) {
                if (wallpaperConfig.imageUri != null) {
                  AsyncImage(
                    model = wallpaperConfig.imageUri,
                    contentDescription = null,
                    modifier = Modifier
                      .fillMaxSize()
                      .blur(wallpaperConfig.blurRadiusDp.dp),
                    contentScale = ContentScale.Crop
                  )
                } else if (wallpaperConfig.builtInPattern != null) {
                  BuiltInWallpaperBackdrop(
                    pattern = wallpaperConfig.builtInPattern!!,
                    modifier = Modifier
                      .fillMaxSize()
                      .blur(wallpaperConfig.blurRadiusDp.dp)
                  )
                }
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = wallpaperConfig.darkOverlayOpacity))
                )
              }
            }

            Scaffold(
              modifier = Modifier.graphicsLayer {
                translationX = parallaxOffset.foregroundX.dp.toPx()
                translationY = parallaxOffset.foregroundY.dp.toPx()
              },
              containerColor = Color.Transparent,
              snackbarHost = { SnackbarHost(snackbarHostState) },
              topBar = {
                TopAppBar(
                  title = {
                    Text(
                      text = when (selectedNavNode) {
                        NavigationNode.DASHBOARD -> "Files"
                        NavigationNode.EXPLORER -> currentDirectory.name.ifEmpty { "Internal Storage" }
                        NavigationNode.RECENTS -> "Recent Files"
                        NavigationNode.SEARCH -> "Deep File Search"
                        NavigationNode.SETTINGS -> "Configurations"
                        NavigationNode.RECYCLE_BIN -> "Recycle Bin"
                        NavigationNode.NEARBY_DEVICES -> "Nearby Devices"
                      },
                      style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = adaptivePrimaryTextColor
                      )
                    )
                  },
                  navigationIcon = {
                    if (selectedNavNode == NavigationNode.RECYCLE_BIN || selectedNavNode == NavigationNode.NEARBY_DEVICES) {
                      IconButton(
                        onClick = { selectedNavNode = NavigationNode.DASHBOARD },
                        modifier = Modifier.testTag("top_bar_back_button")
                      ) {
                        Icon(
                          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                          contentDescription = "Back to Dashboard",
                          tint = adaptivePrimaryTextColor
                        )
                      }
                    } else {
                      IconButton(
                        onClick = { coroutineScope.launch { drawerState.open() } },
                        modifier = Modifier.testTag("drawer_hamburger_button")
                      ) {
                        Icon(
                          imageVector = Icons.Default.Menu,
                          contentDescription = "Open Sidebar Menu",
                          tint = adaptivePrimaryTextColor
                        )
                      }
                    }
                  },
                  actions = {
                    if (selectedNavNode != NavigationNode.RECYCLE_BIN && selectedNavNode != NavigationNode.NEARBY_DEVICES) {
                      // SAF Quick Backdoor Button in Top Bar (Device Status / Storage Access Action)
                      IconButton(
                        onClick = {
                          FileManager.openPathSAFBackdoor(context, currentDirectory.absolutePath)
                        },
                        modifier = Modifier.testTag("top_bar_saf_backdoor_button")
                      ) {
                        Icon(
                          imageVector = Icons.Default.FolderOpen,
                          contentDescription = "Open Path in System Files",
                          tint = adaptivePrimaryTextColor
                        )
                      }
                    }

                    if (selectedNavNode == NavigationNode.EXPLORER) {
                      IconButton(
                        onClick = { isCreateFolderDialogOpen = true },
                        modifier = Modifier.testTag("top_bar_create_folder_button")
                      ) {
                        Icon(
                          imageVector = Icons.Default.CreateNewFolder,
                          contentDescription = "Create Folder",
                          tint = adaptivePrimaryTextColor
                        )
                      }
                    }

                    IconButton(
                      onClick = {
                        selectedNavNode = NavigationNode.SETTINGS
                      },
                      modifier = Modifier.testTag("top_bar_settings_button")
                    ) {
                      Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = adaptivePrimaryTextColor
                      )
                    }
                  },
                  colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ThemeManager.getAdaptiveTopBarColor(themeMode, environmentalConfig.selectedSeason),
                    scrolledContainerColor = ThemeManager.getAdaptiveTopBarColor(themeMode, environmentalConfig.selectedSeason)
                  )
                )
              },
              floatingActionButton = {
                if (selectedNavNode == NavigationNode.EXPLORER) {
                  FloatingActionButton(
                    onClick = { isCreateFolderDialogOpen = true },
                    containerColor = ThemeManager.getThemeAccentColor(themeMode, customAccentColor),
                    contentColor = if (ThemeManager.isLightBackgroundProfile(themeMode, environmentalConfig.selectedSeason)) Color.White else Color.Black,
                    modifier = Modifier.testTag("fab_create_folder")
                  ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Folder")
                  }
                }
              }
            ) { innerPadding ->
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(innerPadding)
              ) {
                // Animated Full Screen Layout Content Pane
                AnimatedContent(
                  targetState = selectedNavNode,
                  transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                  },
                  label = "main_screen_pane_transition"
                ) { targetNode ->
                MainContentPane(
                  selectedNavNode = targetNode,
                  currentDirectory = currentDirectory,
                  filesList = filesList,
                  highlightFilePath = highlightFilePath,
                  recentFiles = RecentFilesTracker.getRecents(),
                  searchQuery = searchQuery,
                  onSearchQueryChanged = { searchQuery = it },
                  searchOptions = searchOptions,
                  onSearchOptionsChanged = { searchOptions = it },
                  themeMode = themeMode,
                  onThemeModeChanged = {
                    themeMode = it
                    ThemePreferences.setSavedThemeMode(context, it)
                  },
                  customAccentColor = customAccentColor,
                  onCustomAccentColorChanged = {
                    customAccentColor = it
                    ThemePreferences.setSavedCustomAccentColor(context, it)
                  },
                  storageMetrics = storageMetrics,
                  onRefreshStorage = {
                    storageMetrics = FileManager.getStorageMetrics()
                    refreshDirectoryFiles()
                  },
                  onNavigateToExplorer = { targetFolder, filterQuery ->
                    selectedNavNode = NavigationNode.EXPLORER
                    if (targetFolder != null) {
                      navigateToDirectory(targetFolder)
                    }
                    if (filterQuery != null) {
                      searchQuery = filterQuery
                    }
                  },
                  onNavigateToRecycleBin = {
                    selectedNavNode = NavigationNode.RECYCLE_BIN
                  },
                  onNavigateToSettings = {
                    selectedNavNode = NavigationNode.SETTINGS
                  },
                  onNavigateToDirectory = { dir ->
                    navigateToDirectory(dir)
                  },
                  onFileItemClick = { item ->
                    if (item.isDirectory) {
                      navigateToDirectory(item.file)
                    } else {
                      val isMedia = item.file.extension.lowercase() in listOf("mp3", "wav", "flac", "ogg", "m4a", "mp4", "mkv", "webm", "mov", "3gp")
                      if (isMedia) {
                        activeMediaFileItem = item
                      } else {
                        RecentFilesTracker.recordAccess(item.file)
                        FileManager.openWithSystemDefault(context, item.file)
                      }
                    }
                  },
                  onFileItemLongClick = { item ->
                    isFromRecentsTabDialog = false
                    activeManageFileItem = item
                  },
                  onRecentItemClick = { item ->
                    if (item.isDirectory) {
                      navigateToDirectory(item.file)
                      selectedNavNode = NavigationNode.EXPLORER
                    } else {
                      isFromRecentsTabDialog = true
                      activeManageFileItem = item
                    }
                  },
                  onClearRecentHistory = { RecentFilesTracker.clear() },
                  onBatchZipRequest = { batch ->
                    coroutineScope.launch {
                      isCompressingOrUnzipping = true
                      compressionProgressRatio = 0f
                      compressionStatusMsg = "Preparing batch archive..."
                      val outputFile = File(currentDirectory, "Batch_Archive_${System.currentTimeMillis() % 10000}.zip")
                      val result = FileManager.compressToZip(
                        sources = batch.map { it.file },
                        zipOutputFile = outputFile,
                        onProgress = { ratio, msg ->
                          compressionProgressRatio = ratio
                          compressionStatusMsg = msg
                        }
                      )
                      isCompressingOrUnzipping = false
                      if (result.isSuccess) {
                        HapticFeedbackHelper.performTransferSuccessFeedback(context)
                        Toast.makeText(context, "Archive created: ${outputFile.name}", Toast.LENGTH_SHORT).show()
                        refreshDirectoryFiles()
                      } else {
                        HapticFeedbackHelper.performErrorFeedback(context)
                        Toast.makeText(context, "Archive failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                      }
                    }
                  },
                  onCreateFolderRequest = { isCreateFolderDialogOpen = true },
                  season = environmentalConfig.selectedSeason,
                  onOpenSearchConfigDialog = { showSearchConfigDialog = true },
                  onOpenWallpaperEngineDialog = { showWallpaperEngineDialog = true },
                  onOpenEnvironmentalEngineDialog = { showEnvironmentalDialog = true },
                  onOpenWelcomeWizard = { showWelcomeWizardDialog = true },
                  onStreamMediaItem = { mediaItem -> activeMediaFileItem = mediaItem },
                  onReplayTutorial = {
                    com.jackattackk246.files.util.DashboardPreferences.setFirstLaunchTutorialEnabled(context, true)
                    showTutorialOverlay = true
                  }
                )
              }
            }

            // Real-time FPS Performance Overlay
            val isFpsOverlayActive by com.jackattackk246.files.util.DeveloperToolsManager.fpsOverlayState.collectAsState()
            if (isFpsOverlayActive) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(top = 80.dp, end = 16.dp),
                contentAlignment = androidx.compose.ui.Alignment.TopEnd
              ) {
                FpsCounterOverlay()
              }
            }

            // Windows 11 Desktop Mode Bottom Taskbar Dock
            if (desktopPalette.isDesktopCanvasActive) {
              Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.BottomCenter
              ) {
                DesktopTaskbarDock(
                  selectedNavNode = selectedNavNode,
                  onNodeSelected = { selectedNavNode = it },
                  onOpenStartMenu = { coroutineScope.launch { drawerState.open() } },
                  accentColor = ThemeManager.getThemeAccentColor(themeMode, customAccentColor)
                )
              }
            }
          }
        }
      }
    }

        // Universal 'Manage File' Dialog (With 3x2 action cards grid)
        activeManageFileItem?.let { item ->
          ManageFileDialog(
            fileItem = item,
            isFromRecentsTab = isFromRecentsTabDialog,
            onDismiss = { activeManageFileItem = null },
            onShowInFolder = { target ->
              val parent = target.file.parentFile
              if (parent != null && parent.exists()) {
                selectedNavNode = NavigationNode.EXPLORER
                navigateToDirectory(parent)
                highlightFilePath = target.path
                searchQuery = ""

                coroutineScope.launch {
                  refreshDirectoryFiles()
                  delay(2000)
                  highlightFilePath = null
                }
              } else {
                Toast.makeText(context, "Parent folder not accessible", Toast.LENGTH_SHORT).show()
              }
            },
            onRenameRequest = {
              renameTargetItem = it
              renameInputText = it.name
            },
            onCopyRequest = {
              clipboardFile = it
              clipboardOperation = "COPY"
              Toast.makeText(context, "Copied to clipboard. Navigate and tap 'Paste Here'", Toast.LENGTH_LONG).show()
            },
            onMoveRequest = {
              clipboardFile = it
              clipboardOperation = "MOVE"
              Toast.makeText(context, "Cut to clipboard. Navigate and tap 'Paste Here'", Toast.LENGTH_LONG).show()
            },
            onDeleteRequest = { target ->
              deleteConfirmationTarget = target
            },
            onZipRequest = { target ->
              coroutineScope.launch {
                isCompressingOrUnzipping = true
                compressionProgressRatio = 0f
                compressionStatusMsg = "Compressing ${target.name}..."
                val outputFile = File(target.file.parentFile ?: currentDirectory, "${target.name}.zip")
                val result = FileManager.compressToZip(
                  sources = listOf(target.file),
                  zipOutputFile = outputFile,
                  onProgress = { ratio, msg ->
                    compressionProgressRatio = ratio
                    compressionStatusMsg = msg
                  }
                )
                isCompressingOrUnzipping = false
                if (result.isSuccess) {
                  HapticFeedbackHelper.performTransferSuccessFeedback(context)
                  Toast.makeText(context, "Compressed: ${outputFile.name}", Toast.LENGTH_SHORT).show()
                  refreshDirectoryFiles()
                } else {
                  HapticFeedbackHelper.performErrorFeedback(context)
                  Toast.makeText(context, "Zip failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
              }
            },
            onUnzipRequest = { target ->
              coroutineScope.launch {
                isCompressingOrUnzipping = true
                compressionProgressRatio = 0f
                compressionStatusMsg = "Unpacking ${target.name}..."
                val targetDir = File(target.file.parentFile ?: currentDirectory, target.name.removeSuffix(".zip"))
                val result = FileManager.extractZip(
                  zipFile = target.file,
                  targetDir = targetDir,
                  onProgress = { ratio, msg ->
                    compressionProgressRatio = ratio
                    compressionStatusMsg = msg
                  }
                )
                isCompressingOrUnzipping = false
                if (result.isSuccess) {
                  HapticFeedbackHelper.performTransferSuccessFeedback(context)
                  Toast.makeText(context, "Extracted to ${targetDir.name}", Toast.LENGTH_SHORT).show()
                  refreshDirectoryFiles()
                } else {
                  HapticFeedbackHelper.performErrorFeedback(context)
                  Toast.makeText(context, "Unzip failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
              }
            },
            onAnalyticsRequest = {
              activeAnalyticsFileItem = it
            }
          )
        }

        // Folder Analytics Modal
        activeAnalyticsFileItem?.let { item ->
          FolderDetailsDialog(
            fileItem = item,
            onDismiss = { activeAnalyticsFileItem = null }
          )
        }

        // Compression Progress Dialog
        if (isCompressingOrUnzipping) {
          CompressionProgressDialog(
            title = "Zip Archive Stream",
            progressRatio = compressionProgressRatio,
            statusMessage = compressionStatusMsg
          )
        }

        // Rename Dialog
        renameTargetItem?.let { target ->
          AlertDialog(
            onDismissRequest = { renameTargetItem = null },
            title = { Text("Rename ${target.name}") },
            text = {
              OutlinedTextField(
                value = renameInputText,
                onValueChange = { renameInputText = it },
                singleLine = true,
                label = { Text("New Name") },
                modifier = Modifier.fillMaxWidth().testTag("rename_input_field")
              )
            },
            confirmButton = {
              Button(
                onClick = {
                  val targetItem = renameTargetItem
                  renameTargetItem = null
                  if (targetItem != null && renameInputText.isNotBlank()) {
                    coroutineScope.launch {
                      val res = FileManager.rename(targetItem.file, renameInputText.trim())
                      if (res.isSuccess) {
                        HapticFeedbackHelper.performTransferSuccessFeedback(context)
                        Toast.makeText(context, "Renamed to ${renameInputText.trim()}", Toast.LENGTH_SHORT).show()
                        refreshDirectoryFiles()
                      } else {
                        HapticFeedbackHelper.performErrorFeedback(context)
                        Toast.makeText(context, "Rename failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                      }
                    }
                  }
                }
              ) {
                Text("Save")
              }
            },
            dismissButton = {
              TextButton(onClick = { renameTargetItem = null }) {
                Text("Cancel")
              }
            }
          )
        }

        // Create Folder Dialog
        if (isCreateFolderDialogOpen) {
          AlertDialog(
            onDismissRequest = { isCreateFolderDialogOpen = false },
            title = { Text("Create New Folder") },
            text = {
              OutlinedTextField(
                value = newFolderNameInput,
                onValueChange = { newFolderNameInput = it },
                singleLine = true,
                label = { Text("Folder Name") },
                modifier = Modifier.fillMaxWidth().testTag("create_folder_input_field")
              )
            },
            confirmButton = {
              Button(
                onClick = {
                  val folderName = newFolderNameInput.trim()
                  isCreateFolderDialogOpen = false
                  newFolderNameInput = ""
                  if (folderName.isNotBlank()) {
                    val newDir = File(currentDirectory, folderName)
                    if (!newDir.exists()) {
                      newDir.mkdirs()
                      HapticFeedbackHelper.performTransferSuccessFeedback(context)
                      Toast.makeText(context, "Created folder $folderName", Toast.LENGTH_SHORT).show()
                      refreshDirectoryFiles()
                    } else {
                      HapticFeedbackHelper.performErrorFeedback(context)
                      Toast.makeText(context, "Folder already exists", Toast.LENGTH_SHORT).show()
                    }
                  }
                }
              ) {
                Text("Create")
              }
            },
            dismissButton = {
              TextButton(onClick = { isCreateFolderDialogOpen = false }) {
                Text("Cancel")
              }
            }
          )
        }

        // Developer Password Authorization Dialog for Configurations Access
        if (showDevAuthDialog) {
          com.jackattackk246.files.ui.dialog.DeveloperPasswordAuthDialog(
            onSuccess = {
              val prefs = context.getSharedPreferences("developer_tools_prefs", android.content.Context.MODE_PRIVATE)
              prefs.edit().putBoolean("is_developer_authorized_master", true).apply()
              showDevAuthDialog = false
              pendingActionAfterAuth?.invoke()
              pendingActionAfterAuth = null
            },
            onDismiss = {
              showDevAuthDialog = false
              pendingActionAfterAuth = null
            }
          )
        }

        // Configurations Dialog (Direct Floating Tool Modal)
        if (showConfigurationsDialog) {
          ConfigurationsDialog(
            currentThemeMode = themeMode,
            onThemeModeChanged = {
              themeMode = it
              ThemePreferences.setSavedThemeMode(context, it)
            },
            customAccentColor = customAccentColor,
            onCustomAccentColorChanged = {
              customAccentColor = it
              ThemePreferences.setSavedCustomAccentColor(context, it)
            },
            storageMetrics = storageMetrics,
            onRefreshStorage = {
              storageMetrics = FileManager.getStorageMetrics()
              refreshDirectoryFiles()
            },
            onDismiss = { showConfigurationsDialog = false }
          )
        }

        // Search Config Dialog
        if (showSearchConfigDialog) {
          SearchConfigDialog(
            searchOptions = searchOptions,
            onSearchOptionsChanged = { searchOptions = it },
            onDismiss = { showSearchConfigDialog = false }
          )
        }

        // Wallpaper Engine Dialog
        if (showWallpaperEngineDialog) {
          WallpaperEngineDialog(
            wallpaperConfig = wallpaperConfig,
            onWallpaperConfigChanged = { wallpaperConfig = it },
            onDismiss = { showWallpaperEngineDialog = false }
          )
        }

        // Environmental Engine Dialog
        if (showEnvironmentalDialog) {
          EnvironmentalEngineDialog(
            config = environmentalConfig,
            onConfigChanged = {
              environmentalConfig = it
              EnvironmentalPreferences.saveConfig(context, it)
            },
            onDismiss = { showEnvironmentalDialog = false }
          )
        }

        // Welcome Setup & Personalization Wizard Dialog
        if (showWelcomeWizardDialog) {
          WelcomeWizardDialog(
            initialTheme = themeMode,
            initialWallpaper = wallpaperConfig,
            onComplete = { _, _, _, selectedTheme, selectedWallpaper ->
              themeMode = selectedTheme
              wallpaperConfig = selectedWallpaper
              showWelcomeWizardDialog = false
              // Force first launch tutorial to be enabled immediately post-setup wizard
              com.jackattackk246.files.util.DashboardPreferences.setFirstLaunchTutorialEnabled(context, true)
              launcherPrefs.edit().putBoolean("setup_completed", true).apply()
              setupCompleted = true
              refreshDirectoryFiles()
            }
          )
        }

        if (showTutorialOverlay) {
          TutorialOverlay(
            themeMode = themeMode,
            customAccentColor = customAccentColor,
            onDismiss = {
              showTutorialOverlay = false
            }
          )
        }

        // Protected Path Intercept Gate Modal
        protectedPathTarget?.let { target ->
          ProtectedPathDialog(
            path = target.absolutePath,
            themeMode = themeMode,
            customAccentColor = customAccentColor,
            onOpenOtherApp = {
              FileManager.openPathSAFBackdoor(context, target.absolutePath)
              protectedPathTarget = null
            },
            onOpenAnyway = {
              if (currentDirectory != target) {
                directoryHistory.add(currentDirectory)
              }
              currentDirectory = target
              searchQuery = ""
              refreshDirectoryFiles()
              protectedPathTarget = null
            },
            onDismiss = {
              protectedPathTarget = null
            }
          )
        }

        // Media Player Studio
        activeMediaFileItem?.let { mediaFile ->
          MediaPlayerStudio(
            mediaFile = mediaFile,
            onClose = { activeMediaFileItem = null }
          )
        }

        // Delete Confirmation Dialog
        deleteConfirmationTarget?.let { target ->
          DeleteConfirmationDialog(
            targetName = target.name,
            accentColor = customAccentColor ?: MaterialTheme.colorScheme.primary,
            onConfirm = {
              coroutineScope.launch {
                val targetFile = target.file
                val targetName = target.name
                val res = FileManager.delete(targetFile)
                if (res.isSuccess) {
                  HapticFeedbackHelper.performToggleFeedback(context)
                  RecentFilesTracker.removeAll { it.path == target.path }
                  refreshDirectoryFiles()
                  storageMetrics = FileManager.getStorageMetrics()
                  deleteConfirmationTarget = null

                  // Show Snackbar notification with interactive 'Undo' action
                  val snackbarResult = snackbarHostState.showSnackbar(
                    message = "Moved '$targetName' to Recycle Bin",
                    actionLabel = "Undo",
                    duration = SnackbarDuration.Short
                  )

                  if (snackbarResult == SnackbarResult.ActionPerformed) {
                    val restored = RecycleBinEngine.restoreMostRecentItem()
                    if (restored) {
                      HapticFeedbackHelper.performTransferSuccessFeedback(context)
                      refreshDirectoryFiles()
                      storageMetrics = FileManager.getStorageMetrics()
                      Toast.makeText(context, "Restored '$targetName'", Toast.LENGTH_SHORT).show()
                    } else {
                      Toast.makeText(context, "Unable to restore '$targetName'", Toast.LENGTH_SHORT).show()
                    }
                  }
                } else {
                  HapticFeedbackHelper.performErrorFeedback(context)
                  Toast.makeText(context, "Delete failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
              }
            },
            onDismiss = { deleteConfirmationTarget = null }
          )
        }
      }
    }
  }
}

@Composable
private fun MainContentPane(
  selectedNavNode: NavigationNode,
  currentDirectory: File,
  filesList: List<FileItem>,
  highlightFilePath: String?,
  recentFiles: List<RecentFileItem>,
  searchQuery: String,
  onSearchQueryChanged: (String) -> Unit,
  searchOptions: SearchOptions,
  onSearchOptionsChanged: (SearchOptions) -> Unit,
  themeMode: AppThemeMode,
  onThemeModeChanged: (AppThemeMode) -> Unit,
  customAccentColor: Color?,
  onCustomAccentColorChanged: (Color?) -> Unit,
  storageMetrics: FileManager.StorageMetrics,
  onRefreshStorage: () -> Unit,
  onNavigateToExplorer: (File?, String?) -> Unit,
  onNavigateToRecycleBin: () -> Unit = {},
  onNavigateToSettings: () -> Unit,
  onNavigateToDirectory: (File) -> Unit,
  onFileItemClick: (FileItem) -> Unit,
  onFileItemLongClick: (FileItem) -> Unit,
  onRecentItemClick: (FileItem) -> Unit,
  onClearRecentHistory: () -> Unit,
  onBatchZipRequest: (List<FileItem>) -> Unit,
  onCreateFolderRequest: () -> Unit,
  season: com.jackattackk246.files.model.EnvironmentalSeason = com.jackattackk246.files.model.EnvironmentalSeason.AUTO,
  onOpenSearchConfigDialog: () -> Unit,
  onOpenWallpaperEngineDialog: () -> Unit,
  onOpenEnvironmentalEngineDialog: () -> Unit,
  onOpenWelcomeWizard: () -> Unit = {},
  onStreamMediaItem: (FileItem) -> Unit = {},
  onReplayTutorial: () -> Unit = {}
) {
  when (selectedNavNode) {
    NavigationNode.DASHBOARD -> {
      DashboardScreen(
        storageMetrics = storageMetrics,
        currentDirectory = currentDirectory,
        themeMode = themeMode,
        customAccentColor = customAccentColor,
        season = season,
        onNavigateToExplorer = onNavigateToExplorer,
        onNavigateToRecycleBin = onNavigateToRecycleBin,
        onNavigateToSettings = onNavigateToSettings
      )
    }

    NavigationNode.EXPLORER -> {
      ExplorerScreen(
        currentDirectory = currentDirectory,
        filesList = filesList,
        highlightFilePath = highlightFilePath,
        searchQuery = searchQuery,
        onSearchQueryChanged = onSearchQueryChanged,
        searchOptions = searchOptions,
        onSearchOptionsChanged = onSearchOptionsChanged,
        onNavigateToDirectory = onNavigateToDirectory,
        onFileItemClick = onFileItemClick,
        onFileItemLongClick = onFileItemLongClick,
        onBatchZipRequest = onBatchZipRequest,
        onCreateFolderRequest = onCreateFolderRequest,
        themeMode = themeMode,
        customAccentColor = customAccentColor
      )
    }

    NavigationNode.RECENTS -> {
      RecentFilesScreen(
        onFileSelected = onRecentItemClick,
        themeMode = themeMode,
        customAccentColor = customAccentColor
      )
    }

    NavigationNode.SEARCH -> {
      ExplorerScreen(
        currentDirectory = currentDirectory,
        filesList = filesList,
        highlightFilePath = highlightFilePath,
        searchQuery = searchQuery,
        onSearchQueryChanged = onSearchQueryChanged,
        searchOptions = searchOptions,
        onSearchOptionsChanged = onSearchOptionsChanged,
        onNavigateToDirectory = onNavigateToDirectory,
        onFileItemClick = onFileItemClick,
        onFileItemLongClick = onFileItemLongClick,
        onBatchZipRequest = onBatchZipRequest,
        onCreateFolderRequest = onCreateFolderRequest,
        themeMode = themeMode,
        customAccentColor = customAccentColor
      )
    }

    NavigationNode.SETTINGS -> {
      SettingsScreen(
        currentThemeMode = themeMode,
        onThemeModeChanged = onThemeModeChanged,
        customAccentColor = customAccentColor,
        onCustomAccentColorChanged = onCustomAccentColorChanged,
        storageMetrics = storageMetrics,
        onRefreshStorage = onRefreshStorage,
        onOpenSearchConfigDialog = onOpenSearchConfigDialog,
        onOpenWallpaperEngineDialog = onOpenWallpaperEngineDialog,
        onOpenEnvironmentalEngineDialog = onOpenEnvironmentalEngineDialog,
        onOpenWelcomeWizard = onOpenWelcomeWizard,
        onReplayTutorial = onReplayTutorial
      )
    }

    NavigationNode.RECYCLE_BIN -> {
      RecycleBinScreen(
        themeMode = themeMode,
        customAccentColor = customAccentColor,
        season = season
      )
    }

    NavigationNode.NEARBY_DEVICES -> {
      NearbyDevicesScreen(
        themeMode = themeMode,
        customAccentColor = customAccentColor,
        season = season,
        onNavigateBack = {},
        onStreamMediaUrl = onStreamMediaItem
      )
    }
  }
}

@Composable
fun FpsCounterOverlay() {
  var fps by remember { mutableIntStateOf(60) }
  var frameTimeMs by remember { mutableFloatStateOf(16.6f) }

  LaunchedEffect(Unit) {
    var lastTime = System.nanoTime()
    var frameCount = 0
    var accumulatedTime = 0L

    while (true) {
      withFrameNanos { currentTime ->
        val delta = currentTime - lastTime
        lastTime = currentTime
        frameCount++
        accumulatedTime += delta

        if (accumulatedTime >= 1_000_000_000L) {
          fps = frameCount
          frameTimeMs = (accumulatedTime / 1_000_000f) / frameCount
          frameCount = 0
          accumulatedTime = 0L
        }
      }
    }
  }

  Surface(
    color = Color.Black.copy(alpha = 0.85f),
    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    border = BorderStroke(1.dp, Color(0xFF00FF66)),
    modifier = Modifier
      .padding(12.dp)
      .testTag("fps_counter_overlay")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .background(Color(0xFF00FF66), CircleShape)
      )
      Text(
        text = "FPS: $fps (${"%.1f".format(frameTimeMs)}ms)",
        color = Color(0xFF00FF66),
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
      )
    }
  }
}

@Composable
fun DesktopTaskbarDock(
  selectedNavNode: NavigationNode,
  onNodeSelected: (NavigationNode) -> Unit,
  onOpenStartMenu: () -> Unit,
  accentColor: Color
) {
  Surface(
    color = Color(0xFF14151C).copy(alpha = 0.92f),
    shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.40f)),
    modifier = Modifier
      .fillMaxWidth()
      .height(56.dp)
      .testTag("desktop_taskbar_dock")
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      IconButton(
        onClick = onOpenStartMenu,
        modifier = Modifier
          .size(40.dp)
          .background(accentColor.copy(alpha = 0.20f), CircleShape)
      ) {
        Icon(
          imageVector = Icons.Default.Apps,
          contentDescription = "Start Menu Launcher",
          tint = accentColor
        )
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
      ) {
        val navItems = listOf(
          Triple(NavigationNode.DASHBOARD, Icons.Default.Dashboard, "Dashboard"),
          Triple(NavigationNode.EXPLORER, Icons.Default.Folder, "Explorer"),
          Triple(NavigationNode.RECENTS, Icons.Default.Schedule, "Recents"),
          Triple(NavigationNode.SEARCH, Icons.Default.Search, "Search"),
          Triple(NavigationNode.RECYCLE_BIN, Icons.Default.Delete, "Recycle Bin"),
          Triple(NavigationNode.NEARBY_DEVICES, Icons.Default.Devices, "Nearby Devices"),
          Triple(NavigationNode.SETTINGS, Icons.Default.Settings, "Settings")
        )

        for ((node, icon, label) in navItems) {
          val isSelected = selectedNavNode == node
          IconButton(
            onClick = { onNodeSelected(node) },
            modifier = Modifier
              .size(38.dp)
              .background(
                if (isSelected) accentColor.copy(alpha = 0.25f) else Color.Transparent,
                androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
              )
          ) {
            Icon(
              imageVector = icon,
              contentDescription = label,
              tint = if (isSelected) accentColor else Color.White.copy(alpha = 0.8f)
            )
          }
        }
      }

      Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .background(Color(0xFF00E676), CircleShape)
        )
        Text(
          text = "Desktop Active",
          color = Color.White.copy(alpha = 0.7f),
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
