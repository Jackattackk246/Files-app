package com.jackattackk246.files.util

import android.content.Context
import android.content.SharedPreferences
import com.jackattackk246.files.model.DashboardWidgetConfig
import com.jackattackk246.files.model.DashboardWidgetId
import com.jackattackk246.files.model.WidgetSizeMode
import org.json.JSONArray
import org.json.JSONObject

object DashboardPreferences {
  private const val PREFS_NAME = "files_dashboard_preferences"
  private const val KEY_WIDGET_LAYOUT = "dashboard_widget_layout_order"
  private const val KEY_EDIT_MODE = "dashboard_edit_mode_unlocked"

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  fun getDefaultWidgetList(): List<DashboardWidgetConfig> {
    return listOf(
      DashboardWidgetConfig(DashboardWidgetId.DEVICE_STORAGE_METER, WidgetSizeMode.FULL, true),
      DashboardWidgetConfig(DashboardWidgetId.LOCAL_STORAGE_HUBS, WidgetSizeMode.FULL, true),
      DashboardWidgetConfig(DashboardWidgetId.APKS_INSTALLER_CENTER, WidgetSizeMode.FULL, true),
      DashboardWidgetConfig(DashboardWidgetId.QUICK_FILE_ACTIONS, WidgetSizeMode.FULL, true)
    )
  }

  fun getWidgetLayoutOrder(context: Context): List<DashboardWidgetConfig> {
    val prefs = getPrefs(context)
    val rawJson = prefs.getString(KEY_WIDGET_LAYOUT, null) ?: return getDefaultWidgetList()
    return try {
      val jsonArray = JSONArray(rawJson)
      val list = mutableListOf<DashboardWidgetConfig>()
      val foundIds = mutableSetOf<DashboardWidgetId>()

      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val idStr = obj.optString("id")
        val sizeStr = obj.optString("size", "full")
        val visible = obj.optBoolean("visible", true)

        val widgetId = DashboardWidgetId.fromId(idStr)
        if (widgetId != null && !foundIds.contains(widgetId)) {
          foundIds.add(widgetId)
          list.add(
            DashboardWidgetConfig(
              widgetId = widgetId,
              sizeMode = WidgetSizeMode.fromId(sizeStr),
              isVisible = visible
            )
          )
        }
      }

      // Add any missing widgets that might have been added in app updates
      DashboardWidgetId.entries.forEach { id ->
        if (!foundIds.contains(id)) {
          list.add(DashboardWidgetConfig(id, WidgetSizeMode.FULL, true))
        }
      }

      list
    } catch (_: Exception) {
      getDefaultWidgetList()
    }
  }

  fun saveWidgetLayoutOrder(context: Context, configs: List<DashboardWidgetConfig>) {
    try {
      val jsonArray = JSONArray()
      configs.forEach { config ->
        val obj = JSONObject().apply {
          put("id", config.widgetId.id)
          put("size", config.sizeMode.id)
          put("visible", config.isVisible)
        }
        jsonArray.put(obj)
      }
      getPrefs(context).edit().putString(KEY_WIDGET_LAYOUT, jsonArray.toString()).apply()
    } catch (_: Exception) {}
  }

  fun isEditModeUnlocked(context: Context): Boolean {
    return getPrefs(context).getBoolean(KEY_EDIT_MODE, false)
  }

  fun setEditModeUnlocked(context: Context, unlocked: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_EDIT_MODE, unlocked).apply()
  }

  fun isFirstLaunchTutorialEnabled(context: Context): Boolean {
    return getPrefs(context).getBoolean("is_first_launch_tutorial_enabled", true)
  }

  fun setFirstLaunchTutorialEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean("is_first_launch_tutorial_enabled", enabled).apply()
  }
}
