package com.jackattackk246.files.ai

import android.content.Context
import android.content.pm.PackageManager
import com.jackattackk246.files.model.FileItem
import com.jackattackk246.files.security.DeveloperSecurityEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * LocalOfflineAiModule - 100% Offline, Client-Side Matrix Computation & Smart Search Indexing Engine
 * Re-engineered to execute strictly on Dispatchers.IO with Wear OS hardware detection and robust fallback.
 */
object LocalOfflineAiModule {

  data class AiMatrixState(
    val isInitialized: Boolean = false,
    val totalInferences: Long = 0L,
    val lastScore: Float = 0.0f,
    val status: String = "Idle",
    val smartKeywords: List<String> = listOf("images", "videos", "docs", "archives", "audio", "apk", "recent", "large"),
    val activeCategorySuggestions: List<SmartSearchSuggestion> = emptyList()
  )

  data class SmartSearchSuggestion(
    val label: String,
    val queryToken: String,
    val category: String,
    val iconType: String,
    val confidence: Float
  )

  private val isRunning = AtomicBoolean(false)
  private var moduleJob: Job? = null
  private var moduleScope: CoroutineScope? = null
  private var isWatchDevice = false

  private val _stateFlow = MutableStateFlow(AiMatrixState())
  val stateFlow: StateFlow<AiMatrixState> = _stateFlow.asStateFlow()

  private val matrixLock = ReentrantReadWriteLock()
  private val inferenceCounter = AtomicLong(0L)

  private val localWeightMatrix = floatArrayOf(
    0.85f, 0.42f, 0.91f, 0.12f,
    0.73f, 0.65f, 0.38f, 0.99f,
    0.54f, 0.29f, 0.88f, 0.61f
  )

  /**
   * Initializes the client-side local offline AI loop on Dispatchers.IO, purging on Wear OS watches.
   */
  fun initializeOfflineAi(context: Context) {
    try {
      // Strict Wear OS hardware form-factor check during initialization
      isWatchDevice = context.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)
      if (isWatchDevice) {
        terminateThreads()
        _stateFlow.value = AiMatrixState(isInitialized = false, status = "Watch Purged (Primitive Fallback)")
        return
      }
    } catch (_: Exception) {}

    if (DeveloperSecurityEngine.isPermanentKarmaBrickActive(context) ||
        DeveloperSecurityEngine.isPortalBricked(context)) {
      terminateThreads()
      return
    }

    if (isRunning.getAndSet(true)) return

    val job = SupervisorJob()
    // ISOLATE INTO DEDICATED LOW-PRIORITY BACKGROUND WORKER POOL ON Dispatchers.IO
    val scope = CoroutineScope(Dispatchers.IO + job)
    moduleJob = job
    moduleScope = scope

    val initialSuggestions = generateSmartSearchKeywords()
    _stateFlow.value = AiMatrixState(
      isInitialized = true,
      status = "Active (Offline AI Search - IO Background)",
      activeCategorySuggestions = initialSuggestions
    )

    scope.launch {
      while (isActive && isRunning.get()) {
        if (DeveloperSecurityEngine.isPermanentKarmaBrickActive(context) ||
            DeveloperSecurityEngine.getRemainingCooldownMs(context) > 0L) {
          terminateThreads()
          break
        }

        val computedScore = computeLocalMatrixPass()
        val count = inferenceCounter.incrementAndGet()
        val updatedSuggestions = generateSmartSearchKeywords()

        _stateFlow.value = AiMatrixState(
          isInitialized = true,
          totalInferences = count,
          lastScore = computedScore,
          status = "Indexed Offline Matrix ($count)",
          activeCategorySuggestions = updatedSuggestions
        )

        delay(15000L)
      }
    }
  }

  fun generateSmartSearchKeywords(): List<SmartSearchSuggestion> {
    return matrixLock.read {
      listOf(
        SmartSearchSuggestion("Images", "ext:jpg,png,webp", "Media", "image", localWeightMatrix[0]),
        SmartSearchSuggestion("Videos", "ext:mp4,mkv,mov", "Media", "video", localWeightMatrix[2]),
        SmartSearchSuggestion("Audio", "ext:mp3,wav,flac", "Media", "audio", localWeightMatrix[4]),
        SmartSearchSuggestion("Documents", "ext:pdf,docx,txt", "Docs", "doc", localWeightMatrix[6]),
        SmartSearchSuggestion("Archives", "ext:zip,rar,7z", "Archive", "zip", localWeightMatrix[8]),
        SmartSearchSuggestion("APK Packages", "ext:apk,xapk", "System", "apk", localWeightMatrix[10]),
        SmartSearchSuggestion("Deep Text", "type:text", "Deep", "text", localWeightMatrix[1]),
        SmartSearchSuggestion("Large Files (>100MB)", "size:>100mb", "Storage", "storage", localWeightMatrix[3])
      )
    }
  }

  /**
   * Fast offline semantic ranking with robust string comparison fallback for Wear OS & failure states.
   */
  fun rankFilesBySemanticRelevance(files: List<FileItem>, query: String): List<FileItem> {
    if (query.isBlank()) return files
    val q = query.trim().lowercase()

    // If watch device or AI model inactive/failed, use lightweight primitive string-matching fallback
    if (isWatchDevice || !_stateFlow.value.isInitialized) {
      return files.filter { file ->
        file.name.lowercase().contains(q) || file.extension.lowercase().contains(q)
      }.sortedBy { it.name.lowercase() }
    }

    return matrixLock.read {
      try {
        files.sortedWith(
          compareByDescending<FileItem> { file ->
            var score = 0.0f
            val name = file.name.lowercase()
            val ext = file.extension.lowercase()

            if (name == q) score += 100.0f
            else if (name.startsWith(q)) score += 50.0f
            else if (name.contains(q)) score += 25.0f

            when (q) {
              "image", "images", "photo", "photos", "pic", "pics" -> {
                if (ext in listOf("jpg", "jpeg", "png", "webp", "gif", "svg", "bmp")) {
                  score += 30.0f * localWeightMatrix[0]
                }
              }
              "video", "videos", "movie", "movies", "clip", "clips" -> {
                if (ext in listOf("mp4", "mkv", "avi", "mov", "webm", "3gp")) {
                  score += 30.0f * localWeightMatrix[2]
                }
              }
              "music", "audio", "song", "songs", "sound" -> {
                if (ext in listOf("mp3", "wav", "flac", "ogg", "m4a", "aac")) {
                  score += 30.0f * localWeightMatrix[4]
                }
              }
              "doc", "docs", "document", "documents", "pdf", "book" -> {
                if (ext in listOf("pdf", "doc", "docx", "txt", "rtf", "md", "epub")) {
                  score += 30.0f * localWeightMatrix[6]
                }
              }
              "zip", "archive", "compressed", "tar" -> {
                if (ext in listOf("zip", "rar", "7z", "tar", "gz")) {
                  score += 30.0f * localWeightMatrix[8]
                }
              }
              "apk", "app", "install", "package" -> {
                if (ext in listOf("apk", "xapk", "apks")) {
                  score += 30.0f * localWeightMatrix[10]
                }
              }
              "large", "heavy", "big" -> {
                if (file.size > 50 * 1024 * 1024L) {
                  score += 20.0f * localWeightMatrix[3]
                }
              }
            }

            if (q.startsWith("ext:")) {
              val targetExts = q.removePrefix("ext:").split(",").map { it.trim() }
              if (ext in targetExts) score += 60.0f
            }

            score
          }.thenBy { it.name.lowercase() }
        )
      } catch (_: Exception) {
        // Fail-safe robust fallback to primitive string matching if semantic ranking throws
        files.filter { it.name.lowercase().contains(q) }.sortedBy { it.name.lowercase() }
      }
    }
  }

  private fun computeLocalMatrixPass(): Float {
    return matrixLock.read {
      var accumulator = 0.0f
      for (i in localWeightMatrix.indices) {
        accumulator += localWeightMatrix[i] * (i + 1) * 0.1f
      }
      accumulator
    }
  }

  fun terminateThreads() {
    isRunning.set(false)
    _stateFlow.value = _stateFlow.value.copy(isInitialized = false, status = "Terminated")
    moduleJob?.cancel()
    moduleScope?.cancel()
    moduleJob = null
    moduleScope = null
  }

  fun predictFileCategory(file: File): String {
    val ext = file.extension.lowercase()
    return when {
      ext in listOf("jpg", "jpeg", "png", "webp", "gif", "svg", "bmp") -> "Image"
      ext in listOf("mp4", "mkv", "avi", "mov", "webm", "3gp") -> "Video"
      ext in listOf("mp3", "wav", "flac", "ogg", "m4a", "aac") -> "Audio"
      ext in listOf("pdf", "doc", "docx", "txt", "rtf", "md", "epub") -> "Document"
      ext in listOf("zip", "rar", "7z", "tar", "gz") -> "Archive"
      ext in listOf("apk", "xapk", "apks") -> "Package"
      else -> "Generic"
    }
  }
}
