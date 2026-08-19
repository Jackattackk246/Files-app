package com.jackattackk246.files.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import com.jackattackk246.files.model.FileItem
import com.jackattackk246.files.model.FolderAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object FileManager {

  // Live 256GB Hardware Partition Capacity Metrics
  const val HARDWARE_CAPACITY_BYTES: Long = 256L * 1024L * 1024L * 1024L // 256 GB

  data class StorageMetrics(
    val realTotalBytes: Long,
    val realFreeBytes: Long,
    val usedBytes: Long,
    val usedRatio: Float
  ) {
    val totalGbFormatted: String = "%.1f GB".format(realTotalBytes / (1024.0 * 1024.0 * 1024.0))
    val usedGbFormatted: String = "%.1f GB".format(usedBytes / (1024.0 * 1024.0 * 1024.0))
    val freeGbFormatted: String = "%.1f GB".format(realFreeBytes / (1024.0 * 1024.0 * 1024.0))
  }

  fun getStorageMetrics(): StorageMetrics {
    return try {
      val path = Environment.getExternalStorageDirectory().path
      val stat = StatFs(path)
      val blockSize = stat.blockSizeLong
      val totalBlocks = stat.blockCountLong
      val availableBlocks = stat.availableBlocksLong

      val realTotal = totalBlocks * blockSize
      val realFree = availableBlocks * blockSize
      val used = (realTotal - realFree).coerceAtLeast(0L)
      val ratio = if (realTotal > 0) (used.toFloat() / realTotal.toFloat()).coerceIn(0f, 1f) else 0f

      StorageMetrics(
        realTotalBytes = realTotal,
        realFreeBytes = realFree,
        usedBytes = used,
        usedRatio = ratio
      )
    } catch (_: Exception) {
      val fallbackTotal = 64L * 1024L * 1024L * 1024L
      val fallbackFree = 32L * 1024L * 1024L * 1024L
      StorageMetrics(
        realTotalBytes = fallbackTotal,
        realFreeBytes = fallbackFree,
        usedBytes = fallbackTotal - fallbackFree,
        usedRatio = 0.5f
      )
    }
  }

  // Get Root / Starting Directory
  fun getRootDirectory(): File {
    val sdCard = Environment.getExternalStorageDirectory()
    return if (sdCard.exists() && sdCard.canRead()) sdCard else File("/sdcard")
  }

  data class UsbDriveDetails(
    val name: String,
    val path: File,
    val totalGbFormatted: String,
    val freeGbFormatted: String,
    val isConnected: Boolean
  )

  fun detectUsbDrive(context: Context): UsbDriveDetails? {
    return try {
      val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager ?: return null
      val volumes = storageManager.storageVolumes
      for (volume in volumes) {
        if (volume.isRemovable && volume.state == Environment.MEDIA_MOUNTED) {
          val pathFile = try {
            val getPathMethod = volume.javaClass.getMethod("getPath")
            val path = getPathMethod.invoke(volume) as String
            File(path)
          } catch (e: Exception) {
            try {
              val getDirectoryMethod = volume.javaClass.getMethod("getDirectory")
              getDirectoryMethod.invoke(volume) as? File
            } catch (ex: Exception) {
              null
            }
          }
          if (pathFile != null && pathFile.exists() && pathFile.canRead()) {
            val name = volume.getDescription(context) ?: "Removable Drive"
            val stat = StatFs(pathFile.path)
            val totalBytes = stat.blockCountLong * stat.blockSizeLong
            val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
            val totalGb = "%.1f GB".format(totalBytes / (1024.0 * 1024.0 * 1024.0))
            val freeGb = "%.1f GB".format(freeBytes / (1024.0 * 1024.0 * 1024.0))
            return UsbDriveDetails(
              name = name,
              path = pathFile,
              totalGbFormatted = totalGb,
              freeGbFormatted = freeGb,
              isConnected = true
            )
          }
        }
      }
      null
    } catch (_: Exception) {
      null
    }
  }

  // List directory contents safely
  fun listFiles(dir: File): List<FileItem> {
    if (!dir.exists() || !dir.isDirectory) return emptyList()
    val files = dir.listFiles() ?: return emptyList()
    return files
      .filter { !it.name.equals(".recycle_bin", ignoreCase = true) && !it.name.equals(".jack_recycle_bin", ignoreCase = true) && !it.name.equals("recycle_manifest.json", ignoreCase = true) }
      .map { FileItem(it) }
      .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
  }

  // Rename File / Folder
  suspend fun rename(target: File, newName: String): Result<File> = withContext(Dispatchers.IO) {
    try {
      if (newName.isBlank()) return@withContext Result.failure(IllegalArgumentException("Name cannot be empty"))
      val parent = target.parentFile ?: return@withContext Result.failure(IllegalStateException("No parent directory"))
      val destination = File(parent, newName)
      if (destination.exists()) {
        return@withContext Result.failure(IllegalStateException("A file named $newName already exists"))
      }
      val success = target.renameTo(destination)
      if (success) Result.success(destination) else Result.failure(IOException("Failed to rename file"))
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  // Copy File or Folder recursively
  suspend fun copy(source: File, destinationDir: File): Result<File> = withContext(Dispatchers.IO) {
    try {
      if (!destinationDir.exists()) destinationDir.mkdirs()
      val target = File(destinationDir, source.name)
      if (source.isDirectory) {
        source.copyRecursively(target, overwrite = true)
      } else {
        source.copyTo(target, overwrite = true)
      }
      Result.success(target)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  // Move File or Folder
  suspend fun move(source: File, destinationDir: File): Result<File> = withContext(Dispatchers.IO) {
    try {
      if (!destinationDir.exists()) destinationDir.mkdirs()
      val target = File(destinationDir, source.name)
      val moved = source.renameTo(target)
      if (moved) {
        Result.success(target)
      } else {
        // Fallback: Copy then delete
        if (source.isDirectory) {
          source.copyRecursively(target, overwrite = true)
        } else {
          source.copyTo(target, overwrite = true)
        }
        deleteInternal(source)
        Result.success(target)
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  // Robust recursive deletion bypassing isolation locks
  private fun deleteInternal(target: File): Boolean {
    if (!target.exists()) return true
    try {
      target.setWritable(true)
    } catch (_: Exception) {}

    if (target.isDirectory) {
      val children = target.listFiles()
      if (children != null) {
        for (child in children) {
          deleteInternal(child)
        }
      }
    }
    val deleted = target.delete()
    return deleted || !target.exists()
  }

  // Delete File or Folder recursively by routing to Recycle Bin
  suspend fun delete(target: File): Result<Boolean> = withContext(Dispatchers.IO) {
    try {
      if (!target.exists()) return@withContext Result.success(true)
      val movedToTrash = RecycleBinEngine.moveToRecycleBin(target)
      if (movedToTrash) {
        Result.success(true)
      } else {
        val success = deleteInternal(target)
        if (success) Result.success(true) else Result.failure(IOException("Failed to delete ${target.name}"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  // Zip Archive Engine with Progress Callback
  suspend fun compressToZip(
    sources: List<File>,
    zipOutputFile: File,
    onProgress: (progressRatio: Float, statusMessage: String) -> Unit
  ): Result<File> = withContext(Dispatchers.IO) {
    try {
      val allFilesToCompress = mutableListOf<Pair<File, String>>()

      fun collectFiles(file: File, baseName: String) {
        if (file.isDirectory) {
          val children = file.listFiles() ?: return
          for (child in children) {
            collectFiles(child, "$baseName/${child.name}")
          }
        } else {
          allFilesToCompress.add(file to baseName)
        }
      }

      for (source in sources) {
        collectFiles(source, source.name)
      }

      val totalCount = allFilesToCompress.size.coerceAtLeast(1)
      var processed = 0

      FileOutputStream(zipOutputFile).use { fos ->
        ZipOutputStream(BufferedOutputStream(fos)).use { zos ->
          val buffer = ByteArray(8192)
          for ((file, entryPath) in allFilesToCompress) {
            processed++
            val ratio = processed.toFloat() / totalCount.toFloat()
            onProgress(ratio, "Compressing: ${file.name} ($processed/$totalCount)")

            val entry = ZipEntry(entryPath)
            zos.putNextEntry(entry)
            FileInputStream(file).use { fis ->
              var len: Int
              while (fis.read(buffer).also { len = it } > 0) {
                zos.write(buffer, 0, len)
              }
            }
            zos.closeEntry()
          }
        }
      }
      onProgress(1.0f, "Compression Complete!")
      Result.success(zipOutputFile)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  // Unzip Extraction Engine with Progress Callback
  suspend fun extractZip(
    zipFile: File,
    targetDir: File,
    onProgress: (progressRatio: Float, statusMessage: String) -> Unit
  ): Result<Boolean> = withContext(Dispatchers.IO) {
    try {
      if (!targetDir.exists()) targetDir.mkdirs()

      // Count entries first for progress estimation
      var totalEntries = 0
      ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
        while (zis.nextEntry != null) totalEntries++
      }
      totalEntries = totalEntries.coerceAtLeast(1)

      var extracted = 0
      ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
        var entry: ZipEntry?
        val buffer = ByteArray(8192)
        while (zis.nextEntry.also { entry = it } != null) {
          val currentEntry = entry ?: break
          val newFile = File(targetDir, currentEntry.name)

          // Security check against zip slip
          if (!newFile.canonicalPath.startsWith(targetDir.canonicalPath)) {
            throw SecurityException("Zip entry attempt outside target dir: ${currentEntry.name}")
          }

          if (currentEntry.isDirectory) {
            newFile.mkdirs()
          } else {
            newFile.parentFile?.mkdirs()
            FileOutputStream(newFile).use { fos ->
              var len: Int
              while (zis.read(buffer).also { len = it } > 0) {
                fos.write(buffer, 0, len)
              }
            }
          }
          extracted++
          val ratio = extracted.toFloat() / totalEntries.toFloat()
          onProgress(ratio, "Extracting: ${currentEntry.name}")
        }
      }
      onProgress(1.0f, "Extraction Complete!")
      Result.success(true)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  // Asynchronous Folder Properties Analyzer
  suspend fun analyzeFolder(folder: File): FolderAnalytics = withContext(Dispatchers.IO) {
    var totalBytes = 0L
    var fileCount = 0L
    var folderCount = 0L

    fun traverse(dir: File) {
      val children = dir.listFiles() ?: return
      for (child in children) {
        if (child.isDirectory) {
          folderCount++
          traverse(child)
        } else {
          fileCount++
          totalBytes += child.length()
        }
      }
    }

    if (folder.isDirectory) {
      traverse(folder)
    } else {
      fileCount = 1
      totalBytes = folder.length()
    }

    FolderAnalytics(
      folderName = folder.name,
      totalBytes = totalBytes,
      totalFilesCount = fileCount,
      totalFoldersCount = folderCount
    )
  }

  // Deep Recursive Scrapers with Vector Similarity / Literal Fallback
  suspend fun searchFiles(
    rootFolder: File,
    query: String,
    currentDirOnly: Boolean,
    deepTextIndexing: Boolean,
    isSmartSearch: Boolean = true
  ): List<FileItem> = withContext(Dispatchers.IO) {
    if (query.isBlank()) return@withContext emptyList()
    val results = mutableListOf<FileItem>()
    val cleanQuery = query.trim().lowercase()

    val textExtensions = setOf("txt", "log", "json", "xml", "html", "md", "csv", "properties", "smali", "kt", "java", "gradle", "kts", "py", "js", "css", "conf")

    fun inspectFileContent(file: File): Boolean {
      if (!deepTextIndexing || !file.isFile || file.length() > 5L * 1024L * 1024L) return false
      val ext = file.extension.lowercase()
      if (ext !in textExtensions) return false

      return try {
        BufferedReader(InputStreamReader(FileInputStream(file), Charsets.UTF_8)).use { reader ->
          var line: String?
          var lineNum = 0
          while (reader.readLine().also { line = it } != null) {
            lineNum++
            if (lineNum > 2000) break // limit scan depth per file for speed
            if (line != null && line!!.lowercase().contains(cleanQuery)) {
              return true
            }
          }
          false
        }
      } catch (_: Exception) {
        false
      }
    }

    fun scanDirectory(dir: File, recursive: Boolean) {
      if (dir.name.equals(".recycle_bin", ignoreCase = true) || dir.name.equals(".jack_recycle_bin", ignoreCase = true)) return
      val children = dir.listFiles() ?: return
      for (child in children) {
        if (child.name.equals(".recycle_bin", ignoreCase = true) || child.name.equals(".jack_recycle_bin", ignoreCase = true) || child.name.equals("recycle_manifest.json", ignoreCase = true)) continue
        val matchesName = child.name.lowercase().contains(cleanQuery)
        val matchesContent = if (!matchesName && child.isFile) inspectFileContent(child) else false

        if (matchesName || matchesContent || isSmartSearch) {
          results.add(FileItem(child))
        }

        if (recursive && child.isDirectory) {
          scanDirectory(child, recursive = true)
        }
      }
    }

    scanDirectory(rootFolder, recursive = !currentDirOnly)
    if (isSmartSearch) {
      // Offline AI Vector Similarity & Cosine Matching Query
      com.jackattackk246.files.ai.LocalOfflineAiModule.querySemanticVectorSimilarity(results, cleanQuery)
    } else {
      // Literal character/filename string matching filter
      results.filter { it.name.lowercase().contains(cleanQuery) }
        .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }
  }

  // Open with System Default Selector & Minecraft Specialty Deployment
  fun openWithSystemDefault(context: Context, file: File) {
    try {
      val uri: Uri = try {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
      } catch (_: Exception) {
        Uri.fromFile(file)
      }

      val ext = file.extension.lowercase()
      val isMinecraft = ext in listOf("mcpack", "mcaddon", "mcworld")

      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, if (isMinecraft) "application/octet-stream" else getMimeType(file))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
      }

      if (isMinecraft) {
        intent.setPackage("com.mojang.minecraftpe")
      }

      val chooser = Intent.createChooser(intent, "Open ${file.name} with...")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
    } catch (e: ActivityNotFoundException) {
      if (file.extension.lowercase() in listOf("mcpack", "mcaddon", "mcworld")) {
        Toast.makeText(context, "Mojang Minecraft (com.mojang.minecraftpe) is not installed on this device.", Toast.LENGTH_LONG).show()
      } else {
        Toast.makeText(context, "No app available to open this file format.", Toast.LENGTH_SHORT).show()
      }
    } catch (e: Exception) {
      Toast.makeText(context, "Error launching viewer: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }

  // Converts any filesystem path to a valid Document Tree URI for SAF deep-linking
  fun getSafDocumentUriForPath(path: String): Uri {
    val clean = path.replace("/sdcard", "/storage/emulated/0").trimEnd('/')
    val emulatedPrefix = "/storage/emulated/0"

    return if (clean.startsWith(emulatedPrefix)) {
      val relative = clean.removePrefix(emulatedPrefix).removePrefix("/")
      if (relative.isEmpty()) {
        Uri.parse("content://com.android.externalstorage.documents/document/primary%3A")
      } else {
        val encodedRelative = Uri.encode(relative)
        Uri.parse("content://com.android.externalstorage.documents/document/primary%3A$encodedRelative")
      }
    } else if (clean.startsWith("/storage/")) {
      val afterStorage = clean.removePrefix("/storage/").removePrefix("/")
      val parts = afterStorage.split("/", limit = 2)
      val volumeId = parts.getOrNull(0) ?: "primary"
      val subPath = parts.getOrNull(1) ?: ""
      if (subPath.isEmpty()) {
        Uri.parse("content://com.android.externalstorage.documents/document/${Uri.encode(volumeId)}%3A")
      } else {
        Uri.parse("content://com.android.externalstorage.documents/document/${Uri.encode(volumeId)}%3A${Uri.encode(subPath)}")
      }
    } else {
      Uri.parse("content://com.android.externalstorage.documents/document/primary%3A")
    }
  }

  // SAF System Backdoor: Launch DocumentsUI in exact active directory path
  fun openPathSAFBackdoor(context: Context, targetPath: String) {
    try {
      val docUri = getSafDocumentUriForPath(targetPath)

      // 1. Primary BROWSE action with INITIAL_URI
      val browseIntent = Intent("android.provider.action.BROWSE").apply {
        data = docUri
        putExtra("android.provider.extra.INITIAL_URI", docUri)
        putExtra("android.provider.extra.SHOW_ADVANCED", true)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
      }
      context.startActivity(browseIntent)
    } catch (_: Exception) {
      try {
        val docUri = getSafDocumentUriForPath(targetPath)
        val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
          setDataAndType(docUri, "*/*")
          putExtra("android.provider.extra.INITIAL_URI", docUri)
          setPackage("com.google.android.documentsui")
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(fallbackIntent)
      } catch (_: Exception) {
        try {
          val settingsIntent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
          context.startActivity(settingsIntent)
        } catch (_: Exception) {
          Toast.makeText(context, "Unable to launch system DocumentsUI", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  private fun getMimeType(file: File): String {
    val ext = file.extension.lowercase()
    return when (ext) {
      "pdf" -> "application/pdf"
      "txt", "log", "json", "xml", "html", "csv" -> "text/plain"
      "jpg", "jpeg", "png", "webp", "gif" -> "image/*"
      "mp3", "wav", "ogg", "flac" -> "audio/*"
      "mp4", "mkv", "avi", "webm" -> "video/*"
      "zip", "rar", "7z", "tar", "gz" -> "application/zip"
      "apk", "xapk", "apks", "aab" -> "application/vnd.android.package-archive"
      else -> "*/*"
    }
  }

  suspend fun scanCategoryFiles(category: String): List<FileItem> = withContext(Dispatchers.IO) {
    val root = getRootDirectory()
    val results = mutableListOf<FileItem>()
    val extensions = when (category.lowercase()) {
      "images" -> setOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "svg")
      "audio" -> setOf("mp3", "wav", "flac", "m4a", "ogg", "aac", "opus", "wma")
      "videos" -> setOf("mp4", "mkv", "webm", "avi", "mov", "3gp", "flv", "wmv")
      "apks", "apk" -> setOf("apk", "xapk", "apks", "aab")
      "docs", "documents" -> setOf("pdf", "doc", "docx", "txt", "rtf", "md", "json", "xml", "csv", "xlsx", "pptx", "epub", "log")
      else -> emptySet()
    }

    if (extensions.isEmpty()) return@withContext emptyList()

    fun scan(dir: File) {
      val files = dir.listFiles() ?: return
      for (file in files) {
        if (file.isDirectory) {
          if (!file.name.startsWith(".")) {
            scan(file)
          }
        } else {
          val ext = file.extension.lowercase()
          if (ext in extensions) {
            results.add(FileItem(file))
          }
        }
      }
    }

    scan(root)
    results.sortedByDescending { it.lastModified }
  }
}

