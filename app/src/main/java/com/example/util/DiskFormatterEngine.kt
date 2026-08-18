package com.jackattackk246.files.util

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.Settings
import java.io.File

object DiskFormatterEngine {

  data class ExternalVolumeInfo(
    val id: String,
    val name: String,
    val path: String,
    val totalBytes: Long,
    val freeBytes: Long,
    val fsType: String,
    val isRemovable: Boolean
  )

  enum class TargetFileSystem(val label: String, val description: String) {
    EXFAT("exFAT", "Optimized for large files (>4GB) & cross-platform portability"),
    FAT32("FAT32", "Universal legacy compatibility across all devices (4GB limit)"),
    EXT4("ext4", "High-performance Linux native journaled filesystem")
  }

  fun getExternalStorageVolumes(context: Context): List<ExternalVolumeInfo> {
    val list = mutableListOf<ExternalVolumeInfo>()
    try {
      val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
      val storageVolumes = storageManager.storageVolumes

      for (vol in storageVolumes) {
        if (vol.isRemovable) {
          val desc = vol.getDescription(context)
          val uuid = vol.uuid ?: "USB"
          val dir = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            vol.directory?.absolutePath ?: "/storage/$uuid"
          } else {
            "/storage/$uuid"
          }

          val file = File(dir)
          val total = if (file.exists()) file.totalSpace else 0L
          val free = if (file.exists()) file.freeSpace else 0L

          list.add(
            ExternalVolumeInfo(
              id = uuid,
              name = desc.ifEmpty { "USB Storage ($uuid)" },
              path = dir,
              totalBytes = total,
              freeBytes = free,
              fsType = "exFAT/FAT32",
              isRemovable = true
            )
          )
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }

    // Fallback detection from /storage/
    if (list.isEmpty()) {
      try {
        val storageDir = File("/storage")
        storageDir.listFiles()?.forEach { child ->
          if (child.isDirectory && child.name != "emulated" && child.name != "self") {
            list.add(
              ExternalVolumeInfo(
                id = child.name,
                name = "External USB Drive (${child.name})",
                path = child.absolutePath,
                totalBytes = child.totalSpace,
                freeBytes = child.freeSpace,
                fsType = "exFAT",
                isRemovable = true
              )
            )
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    return list
  }

  /**
   * Format action trigger
   */
  fun openSystemFormatSettings(context: Context) {
    try {
      val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
      context.startActivity(intent)
    } catch (e: Exception) {
      try {
        val fallback = Intent(Settings.ACTION_STORAGE_VOLUME_ACCESS_SETTINGS)
        context.startActivity(fallback)
      } catch (ex: Exception) {
        ex.printStackTrace()
      }
    }
  }
}
