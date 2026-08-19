package com.jackattackk246.files.model

import java.io.File

data class FileItem(
  val file: File,
  val name: String = file.name,
  val path: String = file.absolutePath,
  val isDirectory: Boolean = file.isDirectory,
  val size: Long = if (file.isFile) file.length() else 0L,
  val lastModified: Long = file.lastModified(),
  val extension: String = file.extension.lowercase(),
  val customStreamUrl: String? = null
) {
  val formattedSize: String
    get() {
      if (isDirectory) return "Folder"
      return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "%.1f KB".format(size / 1024.0)
        size < 1024 * 1024 * 1024 -> "%.1f MB".format(size / (1024.0 * 1024.0))
        else -> "%.2f GB".format(size / (1024.0 * 1024.0 * 1024.0))
      }
    }

  val isMinecraftFile: Boolean
    get() = extension in listOf("mcpack", "mcaddon", "mcworld")

  val isImage: Boolean
    get() = extension in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "svg", "heic")

  val isVideo: Boolean
    get() = extension in listOf("mp4", "mkv", "webm", "avi", "mov", "3gp", "flv", "wmv")

  val isAudio: Boolean
    get() = extension in listOf("mp3", "wav", "flac", "m4a", "ogg", "aac", "opus", "wma")

  val isApk: Boolean
    get() = extension in listOf("apk", "xapk", "apks", "aab")

  val isDocument: Boolean
    get() = extension in listOf("pdf", "doc", "docx", "txt", "rtf", "md", "json", "xml", "csv", "xlsx", "pptx", "html", "epub", "log")

  val isArchive: Boolean
    get() = extension in listOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz")
}

data class RecentFileItem(
  val path: String,
  val name: String = File(path).name,
  val timestamp: Long = System.currentTimeMillis(),
  val isDirectory: Boolean = File(path).isDirectory,
  val size: Long = if (File(path).isFile) File(path).length() else 0L
)

enum class SearchLocation {
  TOP_TOOLBAR,
  BOTTOM_BAR
}

enum class SearchStyle {
  EXPANDED_BOX,
  MINIMAL_ICON
}

data class SearchOptions(
  val location: SearchLocation = SearchLocation.TOP_TOOLBAR,
  val style: SearchStyle = SearchStyle.EXPANDED_BOX,
  val currentDirOnly: Boolean = false,
  val deepTextSearch: Boolean = false,
  val isSmartSearch: Boolean = true
)

data class FolderAnalytics(
  val folderName: String,
  val totalBytes: Long,
  val totalFilesCount: Long,
  val totalFoldersCount: Long
) {
  val formattedTotalSize: String
    get() = when {
      totalBytes < 1024 -> "$totalBytes B"
      totalBytes < 1024 * 1024 -> "%.1f KB".format(totalBytes / 1024.0)
      totalBytes < 1024 * 1024 * 1024 -> "%.2f MB".format(totalBytes / (1024.0 * 1024.0))
      else -> "%.2f GB".format(totalBytes / (1024.0 * 1024.0 * 1024.0))
    }
}

enum class BuiltInWallpaper(val id: String, val title: String, val subtitle: String) {
  CYBER_GRID("cyber_grid", "Cyber Grid", "Monochromatic grid lines over deep slate."),
  SYNTH_WAVEFRONT("synth_wavefront", "Synth Wavefront", "Muted geometric mountain paths."),
  ORBIT_VOID("orbit_void", "Orbit Void", "Dark concentric wireframe planetary orbits."),
  ECHO_LINES("echo_lines", "Echo Lines", "Low-poly audio waveform patterns."),
  MATRIX_STREAM("matrix_stream", "Matrix Stream", "Vertical technical code block strings."),
  SAGE_GEOMETRY("sage_geometry", "Sage Geometry", "Earthy pale olive overlapping triangles."),
  OBSIDIAN_SHARDS("obsidian_shards", "Obsidian Shards", "Angular dark charcoal crystal planes."),
  COPPER_FUSE("copper_fuse", "Copper Fuse", "Industrial metallic circuit traces."),
  SOLAR_CORONA("solar_corona", "Solar Corona", "Minimalist abstract orange vector arches."),
  FROST_POLYGON("frost_polygon", "Frost Polygon", "Crisp low-poly icy crystal mesh patterns.")
}

data class WallpaperConfig(
  val imageUri: android.net.Uri? = null,
  val builtInPattern: BuiltInWallpaper? = null,
  val blurRadiusDp: Float = 0f,
  val darkOverlayOpacity: Float = 0.4f
) {
  val hasWallpaper: Boolean
    get() = imageUri != null || builtInPattern != null
}

/**
 * File Sorting Mode definitions for 100% offline Comparator sorting
 */
enum class FileSortOrder(val label: String, val shortName: String) {
  DEFAULT("Default (Name A-Z)", "Default"),
  NAME_ASC("Name (A to Z)", "Name ↑"),
  NAME_DESC("Name (Z to A)", "Name ↓"),
  SIZE_ASC("Size (Small to Large)", "Size ↑"),
  SIZE_DESC("Size (Large to Small)", "Size ↓"),
  DATE_DESC("Date (Newest First)", "Newest"),
  DATE_ASC("Date (Oldest First)", "Oldest")
}

