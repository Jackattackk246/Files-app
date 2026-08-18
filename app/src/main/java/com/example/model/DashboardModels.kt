package com.jackattackk246.files.model

enum class DashboardWidgetId(val id: String, val title: String, val description: String) {
  DEVICE_STORAGE_METER(
    id = "device_storage_meter",
    title = "Device Storage Meters",
    description = "Live device storage capacity, used/free storage bar & metrics"
  ),
  LOCAL_STORAGE_HUBS(
    id = "local_storage_hubs",
    title = "Local Storage Hubs",
    description = "Fast navigation hubs for Downloads, Documents, Root & USB"
  ),
  APKS_INSTALLER_CENTER(
    id = "apks_installer_center",
    title = "APKs Installer Center",
    description = "Package scanner to rapidly discover and install .apk files"
  ),
  QUICK_FILE_ACTIONS(
    id = "quick_file_actions",
    title = "Quick File Actions",
    description = "Document picker, tree navigation & storage shortcuts"
  );

  companion object {
    fun fromId(id: String?): DashboardWidgetId? {
      return entries.firstOrNull { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
    }
  }
}

enum class WidgetSizeMode(val id: String, val displayName: String) {
  FULL("full", "Full Size"),
  COMPACT("compact", "Compact");

  companion object {
    fun fromId(id: String?): WidgetSizeMode {
      return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: FULL
    }
  }
}

data class DashboardWidgetConfig(
  val widgetId: DashboardWidgetId,
  val sizeMode: WidgetSizeMode = WidgetSizeMode.FULL,
  val isVisible: Boolean = true
)
