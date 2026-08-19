package com.jackattackk246.files.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DeveloperToolsManager {
  private const val PREFS_NAME = "developer_tools_prefs"
  
  private const val KEY_ORIGINAL_SETTINGS = "dev_original_settings_enabled"
  private const val KEY_FPS_OVERLAY = "dev_fps_overlay_enabled"
  private const val KEY_POWER_PROFILE = "dev_power_gating_profile"
  private const val KEY_UNIVERSAL_TEXT_OVERRIDE = "dev_universal_text_override_enabled"
  private const val KEY_CUSTOM_STRING_MAP = "dev_custom_string_map_"
  private const val KEY_PERSIST_HARDWARE_PROFILE = "dev_persist_hardware_profile"
  private const val KEY_SIMULATED_HARDWARE_PROFILE = "dev_simulated_hardware_profile"
  private const val KEY_SIMULATED_OTG = "dev_simulated_otg_enabled"

  val HARDWARE_PROFILES = listOf(
    "Default (Native Hardware Detection)",
    "Force Mobile Layout (Compact)",
    "Force Tablet Layout (Expanded)",
    "Force Wearable Layout (Ultra-Compact)"
  )

  private val _simulatedHardwareProfileState = MutableStateFlow("Default (Native Hardware Detection)")
  val simulatedHardwareProfileState: StateFlow<String> = _simulatedHardwareProfileState.asStateFlow()

  private val _persistHardwareProfileState = MutableStateFlow(false)
  val persistHardwareProfileState: StateFlow<Boolean> = _persistHardwareProfileState.asStateFlow()

  private val _simulatedOtgState = MutableStateFlow(false)
  val simulatedOtgState: StateFlow<Boolean> = _simulatedOtgState.asStateFlow()

  private val _originalSettingsState = MutableStateFlow(true)
  val originalSettingsState: StateFlow<Boolean> = _originalSettingsState.asStateFlow()

  private val _fpsOverlayState = MutableStateFlow(false)
  val fpsOverlayState: StateFlow<Boolean> = _fpsOverlayState.asStateFlow()

  private val _powerProfileState = MutableStateFlow("Standard Balance Profile")
  val powerProfileState: StateFlow<String> = _powerProfileState.asStateFlow()

  private val _universalTextOverrideState = MutableStateFlow(false)
  val universalTextOverrideState: StateFlow<Boolean> = _universalTextOverrideState.asStateFlow()

  private val _simulatedPowerChoice = MutableStateFlow("Normal Power Status")
  val simulatedPowerChoice: StateFlow<String> = _simulatedPowerChoice.asStateFlow()

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  fun isOriginalSettingsEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_ORIGINAL_SETTINGS, true)
  fun setOriginalSettingsEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_ORIGINAL_SETTINGS, enabled).apply()
    _originalSettingsState.value = enabled
  }

  fun isFpsOverlayEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_FPS_OVERLAY, false)
  fun setFpsOverlayEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_FPS_OVERLAY, enabled).apply()
    _fpsOverlayState.value = enabled
  }

  fun getPowerProfile(context: Context): String = getPrefs(context).getString(KEY_POWER_PROFILE, "Standard Balance Profile") ?: "Standard Balance Profile"
  fun setPowerProfile(context: Context, profile: String) {
    getPrefs(context).edit().putString(KEY_POWER_PROFILE, profile).apply()
    _powerProfileState.value = profile
  }

  fun isUniversalTextOverrideEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_UNIVERSAL_TEXT_OVERRIDE, false)
  fun setUniversalTextOverrideEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_UNIVERSAL_TEXT_OVERRIDE, enabled).apply()
    _universalTextOverrideState.value = enabled
  }

  fun getSimulatedPowerChoice(context: Context): String = _simulatedPowerChoice.value

  fun setSimulatedPowerChoice(context: Context, choice: String) {
    _simulatedPowerChoice.value = choice
    val level = when (choice) {
      "Force Low Battery Warning (15%)" -> 15
      "Critical Cutoff State (5%)" -> 5
      else -> null
    }
    NearbyDevicesEngine.setSimulatedPowerState(level)
  }

  fun getCustomText(context: Context, originalText: String, defaultReturn: String): String {
    if (!isUniversalTextOverrideEnabled(context)) return defaultReturn
    return getPrefs(context).getString(KEY_CUSTOM_STRING_MAP + originalText.hashCode(), defaultReturn) ?: defaultReturn
  }

  fun setCustomText(context: Context, originalText: String, newText: String) {
    getPrefs(context).edit().putString(KEY_CUSTOM_STRING_MAP + originalText.hashCode(), newText).apply()
  }

  fun initHardwareProfile(context: Context) {
    val prefs = getPrefs(context)
    val shouldPersist = prefs.getBoolean(KEY_PERSIST_HARDWARE_PROFILE, false)
    _persistHardwareProfileState.value = shouldPersist
    if (shouldPersist) {
      _simulatedHardwareProfileState.value = prefs.getString(KEY_SIMULATED_HARDWARE_PROFILE, HARDWARE_PROFILES[0]) ?: HARDWARE_PROFILES[0]
    } else {
      _simulatedHardwareProfileState.value = HARDWARE_PROFILES[0]
    }
    _simulatedOtgState.value = prefs.getBoolean(KEY_SIMULATED_OTG, false)
    if (_simulatedOtgState.value) {
      UsbStorageManager.scanAttachedDrives(context)
    }
  }

  fun isSimulatedOtgEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_SIMULATED_OTG, false)

  fun setSimulatedOtgEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_SIMULATED_OTG, enabled).apply()
    _simulatedOtgState.value = enabled
    UsbStorageManager.scanAttachedDrives(context)
  }

  fun setPersistHardwareProfile(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_PERSIST_HARDWARE_PROFILE, enabled).apply()
    _persistHardwareProfileState.value = enabled
    if (!enabled) {
      getPrefs(context).edit().remove(KEY_SIMULATED_HARDWARE_PROFILE).apply()
    } else {
      getPrefs(context).edit().putString(KEY_SIMULATED_HARDWARE_PROFILE, _simulatedHardwareProfileState.value).apply()
    }
  }

  fun setSimulatedHardwareProfile(context: Context, profile: String) {
    _simulatedHardwareProfileState.value = profile
    if (_persistHardwareProfileState.value) {
      getPrefs(context).edit().putString(KEY_SIMULATED_HARDWARE_PROFILE, profile).apply()
    }
  }

  fun clearAllCaches(context: Context) {
    try {
      context.cacheDir.deleteRecursively()
      context.externalCacheDir?.deleteRecursively()
      Runtime.getRuntime().gc()
    } catch (_: Exception) {}
  }
}
