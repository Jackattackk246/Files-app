package com.jackattackk246.files.util

import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.StatFs
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import com.jackattackk246.files.model.FileItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

data class UsbDeviceState(
  val isConnected: Boolean = false,
  val deviceName: String = "OTG USB Storage",
  val volumeLabel: String = "USB Drive",
  val mountPath: File? = null,
  val safTreeUri: Uri? = null,
  val isSafAuthorized: Boolean = false,
  val totalBytes: Long = 0L,
  val freeBytes: Long = 0L,
  val usedBytes: Long = 0L
) {
  val totalGbFormatted: String
    get() = if (totalBytes > 0) "%.1f GB".format(totalBytes / (1024.0 * 1024.0 * 1024.0)) else "OTG Mass Storage"

  val freeGbFormatted: String
    get() = if (freeBytes > 0) "%.1f GB Free".format(freeBytes / (1024.0 * 1024.0 * 1024.0)) else "Ready"

  val usedRatio: Float
    get() = if (totalBytes > 0) ((totalBytes - freeBytes).toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
}

data class UsbFileNode(
  val name: String,
  val uri: Uri,
  val isDirectory: Boolean,
  val size: Long,
  val lastModified: Long,
  val documentFile: DocumentFile
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

  val extension: String
    get() = name.substringAfterLast('.', "").lowercase()

  val isImage: Boolean
    get() = extension in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "svg", "heic")

  val isVideo: Boolean
    get() = extension in listOf("mp4", "mkv", "webm", "avi", "mov", "3gp", "flv", "wmv")

  val isAudio: Boolean
    get() = extension in listOf("mp3", "wav", "flac", "m4a", "ogg", "aac", "opus", "wma")

  val isArchive: Boolean
    get() = extension in listOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz")

  val isDocument: Boolean
    get() = extension in listOf("pdf", "doc", "docx", "txt", "rtf", "md", "json", "xml", "csv", "xlsx", "pptx", "html", "epub", "log")
}

object UsbStorageManager {

  private const val PREFS_NAME = "usb_storage_engine_prefs"
  private const val KEY_SAF_URI = "key_persisted_saf_tree_uri"
  private const val BUFFER_SIZE = 64 * 1024 // 64 KB high-throughput streaming buffer

  private val _usbState = MutableStateFlow(UsbDeviceState())
  val usbState: StateFlow<UsbDeviceState> = _usbState.asStateFlow()

  // Lifecycle execution scope for streaming operations
  private var managerJob = SupervisorJob()
  private var managerScope = CoroutineScope(managerJob + Dispatchers.IO)

  fun initialize(context: Context) {
    scanAttachedDrives(context)
  }

  
  fun handlePermissionGranted(context: Context, device: UsbDevice) {
    try {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as android.os.storage.StorageManager
        val volumes = storageManager.storageVolumes
        for (volume in volumes) {
            if (volume.isRemovable && volume.state == android.os.Environment.MEDIA_MOUNTED) {
                // Dynamically extract the root directory URI of the external USB volume
                val intent = volume.createOpenDocumentTreeIntent()
                val uri = intent.getParcelableExtra<android.net.Uri>(android.provider.DocumentsContract.EXTRA_INITIAL_URI)
                if (uri != null) {
                    savePersistedTreeUri(context, uri)
                }
            }
        }
    } catch (e: Exception) {
        // Fallback
    }
    scanAttachedDrives(context)
  }

  fun handleDeviceAttached(context: Context, intent: Intent?) {
    scanAttachedDrives(context)
  }

  fun handleDeviceDetached(context: Context, intent: Intent?) {
    terminateUsbJobs()
    scanAttachedDrives(context)
  }

  /**
   * Resets and cancels active streaming coroutine threads immediately.
   */
  fun terminateUsbJobs() {
    managerJob.cancel()
    managerJob = SupervisorJob()
    managerScope = CoroutineScope(managerJob + Dispatchers.IO)
  }

  fun scanAttachedDrives(context: Context) {
    managerScope.launch {
      try {
        if (DeveloperToolsManager.isSimulatedOtgEnabled(context)) {
          val simDir = File(context.filesDir, ".virtual_usb_sandbox").apply {
            if (!exists()) mkdirs()
          }
          var usedBytes = 0L
          fun calculateDirSize(f: File) {
            if (f.isFile) {
              usedBytes += f.length()
            } else if (f.isDirectory) {
              f.listFiles()?.forEach { calculateDirSize(it) }
            }
          }
          calculateDirSize(simDir)

          val totalBytes = 16L * 1024L * 1024L * 1024L
          val freeBytes = (totalBytes - usedBytes).coerceAtLeast(0L)

          _usbState.value = UsbDeviceState(
            isConnected = true,
            deviceName = "Simulated OTG Storage",
            volumeLabel = "Simulated OTG Drive",
            mountPath = simDir,
            safTreeUri = null,
            isSafAuthorized = true,
            totalBytes = totalBytes,
            freeBytes = freeBytes,
            usedBytes = usedBytes
          )
          return@launch
        }

        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
        val deviceList = usbManager?.deviceList ?: emptyMap()
        
        var hasMassStorageDevice = false
        var deviceName = "OTG Flash Drive"
        for ((_, device) in deviceList) {
          if (isMassStorageDevice(device)) {
            hasMassStorageDevice = true
            deviceName = device.productName ?: device.deviceName ?: "USB Mass Storage"
            break
          }
        }

        // Also inspect /storage directory mount nodes
        val detectedMount = FileManager.detectUsbDrive(context)
        val persistedUri = getPersistedSafUri(context)
        val isSafValid = persistedUri != null && isUriPermissionActive(context, persistedUri)

        var totalBytes = 0L
        var freeBytes = 0L
        var mountFile: File? = null
        var label = "USB Drive"

        if (detectedMount != null) {
          mountFile = detectedMount.path
          label = detectedMount.name
          try {
            val stat = StatFs(detectedMount.path.path)
            totalBytes = stat.blockCountLong * stat.blockSizeLong
            freeBytes = stat.availableBlocksLong * stat.blockSizeLong
          } catch (_: Exception) {}
        }

        val isConnected = hasMassStorageDevice || detectedMount != null || isSafValid

        _usbState.value = UsbDeviceState(
          isConnected = isConnected,
          deviceName = deviceName,
          volumeLabel = label,
          mountPath = mountFile,
          safTreeUri = persistedUri,
          isSafAuthorized = isSafValid,
          totalBytes = totalBytes,
          freeBytes = freeBytes,
          usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
        )
      } catch (_: Exception) {
        // Fallback safe state
      }
    }
  }

  private fun isMassStorageDevice(device: UsbDevice): Boolean {
    if (device.deviceClass == UsbConstants.USB_CLASS_MASS_STORAGE) return true
    for (i in 0 until device.interfaceCount) {
      val iface = device.getInterface(i)
      if (iface.interfaceClass == UsbConstants.USB_CLASS_MASS_STORAGE) return true
    }
    return false
  }

  fun savePersistedTreeUri(context: Context, uri: Uri) {
    try {
      val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
      context.contentResolver.takePersistableUriPermission(uri, takeFlags)
    } catch (_: Exception) {}

    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_SAF_URI, uri.toString()).apply()
    scanAttachedDrives(context)
  }

  fun getPersistedSafUri(context: Context): Uri? {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val uriStr = prefs.getString(KEY_SAF_URI, null) ?: return null
    return try {
      Uri.parse(uriStr)
    } catch (_: Exception) {
      null
    }
  }

  private fun isUriPermissionActive(context: Context, uri: Uri): Boolean {
    val persisted = context.contentResolver.persistedUriPermissions
    return persisted.any { it.uri == uri && (it.isReadPermission || it.isWritePermission) }
  }

  fun getRootDocumentFile(context: Context): DocumentFile? {
    val uri = getPersistedSafUri(context) ?: return null
    return try {
      DocumentFile.fromTreeUri(context, uri)
    } catch (_: Exception) {
      null
    }
  }

  /**
   * List files from a SAF DocumentFile directory node.
   */
  suspend fun listUsbFiles(
    context: Context,
    parentDoc: DocumentFile? = null
  ): List<UsbFileNode> = withContext(Dispatchers.IO) {
    val targetDoc = parentDoc ?: getRootDocumentFile(context) ?: return@withContext emptyList()
    if (!targetDoc.isDirectory || !targetDoc.canRead()) return@withContext emptyList()

    try {
      val children = targetDoc.listFiles()
      children.map { doc ->
        UsbFileNode(
          name = doc.name ?: "Unnamed",
          uri = doc.uri,
          isDirectory = doc.isDirectory,
          size = if (doc.isFile) doc.length() else 0L,
          lastModified = doc.lastModified(),
          documentFile = doc
        )
      }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    } catch (_: Exception) {
      emptyList()
    }
  }

  /**
   * Stream a local file into a USB SAF document tree destination.
   */
  suspend fun copyLocalFileToUsb(
    context: Context,
    sourceFile: File,
    targetDirectoryDoc: DocumentFile,
    onProgress: (Float) -> Unit = {}
  ): Result<DocumentFile> = withContext(Dispatchers.IO) {
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    try {
      if (!sourceFile.exists() || !sourceFile.canRead()) {
        return@withContext Result.failure(IllegalArgumentException("Source file unreadable"))
      }

      val mimeType = getMimeType(sourceFile.name)
      val targetDoc = targetDirectoryDoc.createFile(mimeType, sourceFile.name)
        ?: return@withContext Result.failure(IllegalStateException("Could not create target node on USB drive"))

      inputStream = FileInputStream(sourceFile)
      outputStream = context.contentResolver.openOutputStream(targetDoc.uri)
        ?: return@withContext Result.failure(IllegalStateException("Failed to open USB output stream"))

      val totalBytes = sourceFile.length()
      var copiedBytes = 0L
      val buffer = ByteArray(BUFFER_SIZE)
      var bytesRead: Int

      while (inputStream.read(buffer).also { bytesRead = it } != -1) {
        outputStream.write(buffer, 0, bytesRead)
        copiedBytes += bytesRead
        if (totalBytes > 0) {
          onProgress((copiedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f))
        }
      }
      outputStream.flush()
      Result.success(targetDoc)
    } catch (e: CancellationException) {
      Result.failure(e)
    } catch (e: Exception) {
      Result.failure(e)
    } finally {
      try { inputStream?.close() } catch (_: Exception) {}
      try { outputStream?.close() } catch (_: Exception) {}
    }
  }

  /**
   * Stream a USB SAF document node to a local filesystem destination.
   */
  suspend fun copyUsbFileToLocal(
    context: Context,
    sourceDoc: DocumentFile,
    targetLocalDirectory: File,
    onProgress: (Float) -> Unit = {}
  ): Result<File> = withContext(Dispatchers.IO) {
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    try {
      if (!targetLocalDirectory.exists()) {
        targetLocalDirectory.mkdirs()
      }

      val destFile = File(targetLocalDirectory, sourceDoc.name ?: "usb_file")
      inputStream = context.contentResolver.openInputStream(sourceDoc.uri)
        ?: return@withContext Result.failure(IllegalStateException("Failed to open USB input stream"))
      outputStream = FileOutputStream(destFile)

      val totalBytes = sourceDoc.length()
      var copiedBytes = 0L
      val buffer = ByteArray(BUFFER_SIZE)
      var bytesRead: Int

      while (inputStream.read(buffer).also { bytesRead = it } != -1) {
        outputStream.write(buffer, 0, bytesRead)
        copiedBytes += bytesRead
        if (totalBytes > 0) {
          onProgress((copiedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f))
        }
      }
      outputStream.flush()
      Result.success(destFile)
    } catch (e: CancellationException) {
      Result.failure(e)
    } catch (e: Exception) {
      Result.failure(e)
    } finally {
      try { inputStream?.close() } catch (_: Exception) {}
      try { outputStream?.close() } catch (_: Exception) {}
    }
  }

  /**
   * Shift / Move local file to USB drive.
   */
  suspend fun moveLocalFileToUsb(
    context: Context,
    sourceFile: File,
    targetDirectoryDoc: DocumentFile,
    onProgress: (Float) -> Unit = {}
  ): Result<DocumentFile> = withContext(Dispatchers.IO) {
    val copyResult = copyLocalFileToUsb(context, sourceFile, targetDirectoryDoc, onProgress)
    if (copyResult.isSuccess) {
      sourceFile.delete()
    }
    copyResult
  }

  /**
   * Shift / Move USB file to local directory.
   */
  suspend fun moveUsbFileToLocal(
    context: Context,
    sourceDoc: DocumentFile,
    targetLocalDirectory: File,
    onProgress: (Float) -> Unit = {}
  ): Result<File> = withContext(Dispatchers.IO) {
    val copyResult = copyUsbFileToLocal(context, sourceDoc, targetLocalDirectory, onProgress)
    if (copyResult.isSuccess) {
      sourceDoc.delete()
    }
    copyResult
  }

  /**
   * Delete USB SAF Document node.
   */
  suspend fun deleteUsbDocument(context: Context, targetDoc: DocumentFile): Boolean = withContext(Dispatchers.IO) {
    try {
      targetDoc.delete()
    } catch (_: Exception) {
      false
    }
  }

  private fun getMimeType(fileName: String): String {
    val extension = fileName.substringAfterLast('.', "")
    if (extension.isEmpty()) return "application/octet-stream"
    val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
    return mime ?: "application/octet-stream"
  }
}
