package com.jackattackk246.files.util

import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import java.io.File

data class PhysicalStorageMetrics(
  val totalHardwareBytes: Long,
  val usedHardwareBytes: Long,
  val freeHardwareBytes: Long,
  val formattedTotal: String,
  val formattedUsed: String,
  val formattedFree: String,
  val usedRatio: Float,
  val percentageUsed: Float
)

object SystemStorageStatsEngine {

  fun getPhysicalStorageMetrics(context: Context): PhysicalStorageMetrics {
    var totalBytes = 0L
    var freeBytes = 0L

    // 1. Query StorageStatsManager on Android O+ for UUID_DEFAULT
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      try {
        val statsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
        if (statsManager != null && storageManager != null) {
          val uuid = StorageManager.UUID_DEFAULT
          val totalStats = statsManager.getTotalBytes(uuid)
          val freeStats = statsManager.getFreeBytes(uuid)
          if (totalStats > 0L) {
            totalBytes = totalStats
            freeBytes = freeStats
          }
        }
      } catch (e: Exception) {
        // Fallback to low-level block allocation tables
      }
    }

    // 2. Fallback / supplementary query combining Data + System partitions
    if (totalBytes <= 0L) {
      try {
        val dataPath = Environment.getDataDirectory()
        val dataStat = StatFs(dataPath.path)
        val dataTotal = dataStat.blockCountLong * dataStat.blockSizeLong
        val dataFree = dataStat.availableBlocksLong * dataStat.blockSizeLong

        val rootPath = Environment.getRootDirectory()
        val rootStat = StatFs(rootPath.path)
        val rootTotal = rootStat.blockCountLong * rootStat.blockSizeLong

        // Hardware flash total calculation (rounding to nearest standard flash tier: 32, 64, 128, 256, 512 GB)
        val rawTotal = dataTotal + rootTotal
        totalBytes = roundToStandardFlashCapacity(rawTotal)
        freeBytes = dataFree
      } catch (e: Exception) {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        totalBytes = stat.blockCountLong * stat.blockSizeLong
        freeBytes = stat.availableBlocksLong * stat.blockSizeLong
      }
    }

    val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
    val ratio = if (totalBytes > 0L) (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
    val percentageUsed = if (totalBytes > 0L) ((totalBytes - freeBytes).toFloat() / totalBytes.toFloat()) * 100f else 0f

    return PhysicalStorageMetrics(
      totalHardwareBytes = totalBytes,
      usedHardwareBytes = usedBytes,
      freeHardwareBytes = freeBytes,
      formattedTotal = formatBytes(totalBytes),
      formattedUsed = formatBytes(usedBytes),
      formattedFree = formatBytes(freeBytes),
      usedRatio = ratio,
      percentageUsed = percentageUsed
    )
  }

  private fun roundToStandardFlashCapacity(rawBytes: Long): Long {
    val gb = rawBytes / (1024.0 * 1024.0 * 1024.0)
    val standardTiers = listOf(16, 32, 64, 128, 256, 512, 1024)
    for (tier in standardTiers) {
      if (gb <= tier * 1.05 && gb >= tier * 0.70) {
        return tier.toLong() * 1024L * 1024L * 1024L
      }
    }
    return rawBytes
  }

  fun formatBytes(bytes: Long): String {
    return when {
      bytes < 1024 -> "$bytes B"
      bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
      bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
      else -> "%.1f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
    }
  }
}
