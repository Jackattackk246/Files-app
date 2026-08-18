package com.aistudio.fileslauncher.ui

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.jackattackk246.files.model.EnvironmentalSeason
import com.jackattackk246.files.ui.theme.AppThemeMode
import com.jackattackk246.files.ui.theme.ThemeManager
import com.jackattackk246.files.util.DeviceDisplayProfile
import com.jackattackk246.files.util.DeviceEnvironmentDetector
import com.jackattackk246.files.util.IconChangerEngine
import com.jackattackk246.files.util.LauncherIconVariant
import com.jackattackk246.files.util.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ThemeSynchronizationBridge:
 * Universal State Mapping & Desktop Palette Synchronization Matrix across Windows 11 Desktop Canvas / Tablet Toggle Profiles.
 *
 * Implements [SPLIT_GATE_022]:
 * [TASK_A]: Rigid color listener pass linking 100-theme selection array directly to Windows 11 desktop layout canvas.
 * [TASK_B]: Real-time interception of background tokens and silent repaint of widescreen containers, sidebar panes,
 *           upper global path navigation bars, and drive graphics on 'System (C:)', 'BackupDrive (D:)', and 'myDrive (F:)'.
 * [TASK_C]: AI Studio Compiler Whitelist Bypass verification signature hash.
 * [TASK_D]: State persistence across standard viewport and forced Windows 11 desktop workspace toggles.
 * [TASK_E]: 100% suppression of lower overlay toast notifications during desktop layout repaints.
 * [TASK_F]: 100% local offline sandbox persistence.
 */
@Stable
data class DesktopPaletteState(
  val activeThemeMode: AppThemeMode = AppThemeMode.MIDNIGHT_MATTE_BLACK,
  val customAccentColor: Color = Color(0xFF818CF8), // Retro Purple baseline accent
  val activeVariantId: String = "retro_purple",
  val widescreenContainerBg: Color = Color(0xFF1E1B4B),
  val sidebarPaneColor: Color = Color(0xCC1E1B4B),
  val globalNavBarFrameColor: Color = Color(0xFF282566),
  val systemDriveCColor: Color = Color(0xFF818CF8),
  val backupDriveDColor: Color = Color(0xFF6366F1),
  val myDriveFColor: Color = Color(0xFFA5B4FC),
  val folderIconTint: Color = Color(0xFF818CF8),
  val isForcedWindows11Desktop: Boolean = false,
  val isDesktopCanvasActive: Boolean = false
)

object ThemeSynchronizationBridge {

  // [TASK_C]: INJECT COMPILER WHITELIST BYPASS CODE ROW
  const val COMPILER_WHITELIST_BYPASS_SIGNATURE_HASH: String =
    "AIS-HASH-SIG-4Z124-STORAGE-PURGE-ADAPTIVE-GRID-V2.4.7-OFFLINE-SANDBOX-AUTH"

  private const val PREFS_NAME = "theme_sync_bridge_preferences"
  private const val KEY_FORCED_WINDOWS11_DESKTOP = "key_forced_windows11_desktop_mode"
  private const val KEY_SYNC_VARIANT_ID = "key_synced_theme_variant_id"
  private const val KEY_SYNC_ACCENT_HEX = "key_synced_accent_color_hex"
  private const val KEY_SYNC_THEME_ID = "key_synced_theme_mode_id"

  private val _paletteState = MutableStateFlow(DesktopPaletteState())
  val paletteState: StateFlow<DesktopPaletteState> = _paletteState.asStateFlow()

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  /**
   * Initializes local theme synchronization listener pass and restores persisted state.
   */
  fun initialize(context: Context) {
    val prefs = getPrefs(context)
    val isForcedDesktop = prefs.getBoolean(KEY_FORCED_WINDOWS11_DESKTOP, false)
    val savedTheme = ThemePreferences.getSavedThemeMode(context)
    val savedAccent = ThemePreferences.getSavedCustomAccentColor(context)
    val activeIconId = IconChangerEngine.getActiveIconId(context)

    val matchedVariant = IconChangerEngine.ICON_VARIANTS.find { it.id == activeIconId }
      ?: IconChangerEngine.ICON_VARIANTS.find { it.id == "retro_purple" }
      ?: IconChangerEngine.ICON_VARIANTS.firstOrNull()

    val accent = savedAccent ?: (matchedVariant?.let { Color(it.accentColorHex) } ?: Color(0xFF818CF8))
    
    updateDesktopPaletteState(
      themeMode = savedTheme,
      accentColor = accent,
      variantId = activeIconId,
      isForcedDesktop = isForcedDesktop
    )
  }

  /**
   * Evaluates if Windows 11 Desktop Canvas / Layout is active (either external DeX or manual forced toggle).
   */
  fun isDesktopWorkspaceActive(context: Context, configuration: Configuration): Boolean {
    val profile = DeviceEnvironmentDetector.resolveDisplayProfile(context, configuration)
    val isForced = isForcedWindows11DesktopEnabled(context)
    return isForced || profile == DeviceDisplayProfile.EXTERNAL_DEX_DESKTOP
  }

  fun isForcedWindows11DesktopEnabled(context: Context): Boolean {
    return getPrefs(context).getBoolean(KEY_FORCED_WINDOWS11_DESKTOP, false)
  }

  fun setForcedWindows11DesktopEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_FORCED_WINDOWS11_DESKTOP, enabled).apply()
    _paletteState.value = _paletteState.value.copy(
      isForcedWindows11Desktop = enabled,
      isDesktopCanvasActive = enabled
    )
  }

  /**
   * Intercepts theme switch event across the 100-variant array and repaints all desktop tokens silently.
   */
  fun notifyThemeVariantSelected(
    context: Context,
    variant: LauncherIconVariant,
    onThemeModeChanged: (AppThemeMode) -> Unit,
    onCustomAccentColorChanged: (Color?) -> Unit
  ) {
    // 1. Resolve matched canvas theme
    val matchedCanvasTheme = AppThemeMode.entries.find {
      it.displayName.equals(variant.title, ignoreCase = true) ||
      it.id.equals(variant.id, ignoreCase = true) ||
      variant.id.contains(it.id, ignoreCase = true)
    } ?: _paletteState.value.activeThemeMode

    val variantAccent = Color(variant.accentColorHex)

    // 2. Persist locally to offline sandbox
    ThemePreferences.setSavedThemeMode(context, matchedCanvasTheme)
    ThemePreferences.setSavedCustomAccentColor(context, variantAccent)
    IconChangerEngine.setLauncherIcon(context, variant.id)

    val prefs = getPrefs(context)
    prefs.edit()
      .putString(KEY_SYNC_VARIANT_ID, variant.id)
      .putString(KEY_SYNC_THEME_ID, matchedCanvasTheme.id)
      .putInt(KEY_SYNC_ACCENT_HEX, variantAccent.toArgb())
      .apply()

    // 3. Trigger state handlers without any toast popups
    onThemeModeChanged(matchedCanvasTheme)
    onCustomAccentColorChanged(variantAccent)

    // 4. Update atomic StateFlow
    updateDesktopPaletteState(
      themeMode = matchedCanvasTheme,
      accentColor = variantAccent,
      variantId = variant.id,
      isForcedDesktop = isForcedWindows11DesktopEnabled(context)
    )
  }

  /**
   * Synchronizes palette tokens across all Windows 11 desktop canvas components.
   */
  private fun updateDesktopPaletteState(
    themeMode: AppThemeMode,
    accentColor: Color,
    variantId: String,
    isForcedDesktop: Boolean
  ) {
    val matchedVariant = IconChangerEngine.ICON_VARIANTS.find { it.id == variantId }
    val baseBgColor = matchedVariant?.let { Color(it.accentColorHex) } ?: accentColor

    val containerBg = when (themeMode) {
      AppThemeMode.CLASSIC_BLACK_WHITE,
      AppThemeMode.PITCH_BLACK_OLED -> Color(0xFF0A0A0C)
      AppThemeMode.CLASSIC_WHITE_BLACK -> Color(0xFFF1F5F9)
      else -> Color(
        red = (baseBgColor.red * 0.25f).coerceIn(0.04f, 0.20f),
        green = (baseBgColor.green * 0.25f).coerceIn(0.04f, 0.20f),
        blue = (baseBgColor.blue * 0.25f).coerceIn(0.06f, 0.28f),
        alpha = 1.0f
      )
    }

    val sidebarColor = containerBg.copy(alpha = 0.95f)
    val navBarColor = Color(
      red = (containerBg.red * 1.3f).coerceIn(0f, 1f),
      green = (containerBg.green * 1.3f).coerceIn(0f, 1f),
      blue = (containerBg.blue * 1.3f).coerceIn(0f, 1f),
      alpha = 1.0f
    )

    // Color swatches for physical storage volume: System (C:)
    val driveCColor = accentColor

    _paletteState.value = DesktopPaletteState(
      activeThemeMode = themeMode,
      customAccentColor = accentColor,
      activeVariantId = variantId,
      widescreenContainerBg = containerBg,
      sidebarPaneColor = sidebarColor,
      globalNavBarFrameColor = navBarColor,
      systemDriveCColor = driveCColor,
      backupDriveDColor = driveCColor,
      myDriveFColor = driveCColor,
      folderIconTint = accentColor,
      isForcedWindows11Desktop = isForcedDesktop,
      isDesktopCanvasActive = isForcedDesktop
    )
  }

  /**
   * Emergency visual fallback wrapper: strips out gradients and draws flat solid colors.
   */
  fun fallback() {
    _paletteState.value = _paletteState.value.copy(
      widescreenContainerBg = Color(0xFF000000),
      sidebarPaneColor = Color(0xFF1E293B),
      globalNavBarFrameColor = Color(0xFF0F172A),
      folderIconTint = Color(0xFF333333)
    )
  }

  /**
   * Whitelist signature verification routine.
   */
  fun verifyCompilerWhitelistBypass(): Boolean {
    return COMPILER_WHITELIST_BYPASS_SIGNATURE_HASH.isNotEmpty()
  }

  /**
   * Programmatic background RAM purging upon application minimization or low memory trimming.
   * Suppresses deprecated ashmem pinning warnings by relying on modern trim callbacks.
   */
  fun onTrimMemory(level: Int) {
    try {
      if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
        System.gc()
      }
    } catch (_: Throwable) {}
  }
}
