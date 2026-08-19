package com.jackattackk246.files.util

import android.content.Context
import android.os.Environment
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class RecycledFileRecord(
  val id: String,
  val fileName: String,
  val originalPath: String,
  val recycledPath: String,
  val deletedTimestamp: Long,
  val fileSize: Long,
  val isDirectory: Boolean
)

object RecycleBinEngine {

  const val RECYCLE_DIR_NAME = ".recycle_bin"
  private const val METADATA_FILE = "recycle_manifest.json"
  private const val THIRTY_DAYS_MILLIS = 30L * 24 * 60 * 60 * 1000L

  fun getRecycleRootDirectory(): File {
    val base = Environment.getExternalStorageDirectory()
    val recycleDir = File(base, RECYCLE_DIR_NAME)
    if (!recycleDir.exists()) {
      recycleDir.mkdirs()
    }
    return recycleDir
  }

  private fun getManifestFile(): File {
    return File(getRecycleRootDirectory(), METADATA_FILE)
  }

  @Synchronized
  fun getRecycledItems(): List<RecycledFileRecord> {
    val manifest = getManifestFile()
    if (!manifest.exists()) return emptyList()

    val list = mutableListOf<RecycledFileRecord>()
    try {
      val content = manifest.readText()
      val jsonArray = JSONArray(content)
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val id = obj.getString("id")
        val recycledPath = obj.getString("recycledPath")
        val recFile = File(recycledPath)

        // Only include if actual chunk still exists in .jack_recycle_bin
        if (recFile.exists()) {
          list.add(
            RecycledFileRecord(
              id = id,
              fileName = obj.getString("fileName"),
              originalPath = obj.getString("originalPath"),
              recycledPath = recycledPath,
              deletedTimestamp = obj.getLong("deletedTimestamp"),
              fileSize = obj.getLong("fileSize"),
              isDirectory = obj.optBoolean("isDirectory", false)
            )
          )
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return list.sortedByDescending { it.deletedTimestamp }
  }

  @Synchronized
  private fun saveManifest(items: List<RecycledFileRecord>) {
    try {
      val jsonArray = JSONArray()
      for (item in items) {
        val obj = JSONObject().apply {
          put("id", item.id)
          put("fileName", item.fileName)
          put("originalPath", item.originalPath)
          put("recycledPath", item.recycledPath)
          put("deletedTimestamp", item.deletedTimestamp)
          put("fileSize", item.fileSize)
          put("isDirectory", item.isDirectory)
        }
        jsonArray.put(obj)
      }
      getManifestFile().writeText(jsonArray.toString(2))
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  @Synchronized
  fun getItemCount(): Int {
    val items = getRecycledItems()
    return items.size
  }

  @Synchronized
  fun moveToRecycleBin(file: File): Boolean {
    if (!file.exists()) return false

    val recycleRoot = getRecycleRootDirectory()
    val id = "rec_${System.currentTimeMillis()}_${file.name.hashCode()}"
    val targetFile = File(recycleRoot, "${id}_${file.name}")

    val originalPath = file.absolutePath
    val fileSize = if (file.isDirectory) file.walkTopDown().sumOf { it.length() } else file.length()
    val isDir = file.isDirectory

    val moved = file.renameTo(targetFile) || run {
      try {
        if (isDir) {
          file.copyRecursively(targetFile, overwrite = true)
          file.deleteRecursively()
        } else {
          file.copyTo(targetFile, overwrite = true)
          file.delete()
        }
        true
      } catch (e: Exception) {
        false
      }
    }

    if (moved) {
      val currentItems = getRecycledItems().toMutableList()
      currentItems.add(
        RecycledFileRecord(
          id = id,
          fileName = file.name,
          originalPath = originalPath,
          recycledPath = targetFile.absolutePath,
          deletedTimestamp = System.currentTimeMillis(),
          fileSize = fileSize,
          isDirectory = isDir
        )
      )
      saveManifest(currentItems)
      return true
    }
    return false
  }

  @Synchronized
  fun restoreItem(id: String): Boolean {
    val items = getRecycledItems().toMutableList()
    val record = items.find { it.id == id } ?: return false
    val recycledFile = File(record.recycledPath)

    if (!recycledFile.exists()) {
      items.removeAll { it.id == id }
      saveManifest(items)
      return false
    }

    val originalFile = File(record.originalPath)
    val parent = originalFile.parentFile
    if (parent != null && !parent.exists()) {
      parent.mkdirs()
    }

    val restored = recycledFile.renameTo(originalFile) || run {
      try {
        if (record.isDirectory) {
          recycledFile.copyRecursively(originalFile, overwrite = true)
          recycledFile.deleteRecursively()
        } else {
          recycledFile.copyTo(originalFile, overwrite = true)
          recycledFile.delete()
        }
        true
      } catch (e: Exception) {
        false
      }
    }

    if (restored) {
      items.removeAll { it.id == id }
      saveManifest(items)
      return true
    }
    return false
  }

  @Synchronized
  fun restoreMostRecentItem(): Boolean {
    val items = getRecycledItems()
    if (items.isEmpty()) return false
    val mostRecent = items.first()
    return restoreItem(mostRecent.id)
  }

  @Synchronized
  fun deletePermanently(id: String): Boolean {
    val items = getRecycledItems().toMutableList()
    val record = items.find { it.id == id } ?: return false
    val recycledFile = File(record.recycledPath)

    if (recycledFile.exists()) {
      if (recycledFile.isDirectory) {
        recycledFile.deleteRecursively()
      } else {
        recycledFile.delete()
      }
    }

    items.removeAll { it.id == id }
    saveManifest(items)
    return true
  }

  @Synchronized
  fun emptyRecycleBin(): Boolean {
    val recycleRoot = getRecycleRootDirectory()
    val files = recycleRoot.listFiles() ?: return true
    for (f in files) {
      if (f.name != METADATA_FILE) {
        if (f.isDirectory) f.deleteRecursively() else f.delete()
      }
    }
    getManifestFile().writeText("[]")
    return true
  }

  /**
   * 30-Day Auto-Purge Loop
   * Isolated STRICTLY to .jack_recycle_bin repository directory.
   * Terminating immediately if repository contains 0 files to avoid CPU wake locks.
   */
  @Synchronized
  fun runIsolated30DayAutoPurge(): Int {
    val recycleRoot = getRecycleRootDirectory()
    val files = recycleRoot.listFiles()?.filter { it.name != METADATA_FILE } ?: return 0
    if (files.isEmpty()) {
      // Empty-state bypass
      return 0
    }

    val now = System.currentTimeMillis()
    val items = getRecycledItems().toMutableList()
    var purgedCount = 0

    val iterator = items.iterator()
    while (iterator.hasNext()) {
      val item = iterator.next()
      val age = now - item.deletedTimestamp
      if (age > THIRTY_DAYS_MILLIS) {
        val f = File(item.recycledPath)
        if (f.exists()) {
          if (f.isDirectory) f.deleteRecursively() else f.delete()
        }
        iterator.remove()
        purgedCount++
      }
    }

    if (purgedCount > 0) {
      saveManifest(items)
    }
    return purgedCount
  }
}
