package com.jackattackk246.files.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object NativeArchiveEngine {

  data class ArchiveProgress(
    val currentFileName: String,
    val processedCount: Int,
    val totalCount: Int,
    val stage: String // "Compressing", "Extracting", "Completed", "Error"
  )

  suspend fun compressFilesToZip(
    sourceFiles: List<File>,
    destinationZip: File,
    onProgress: (ArchiveProgress) -> Unit = {}
  ): Result<File> = withContext(Dispatchers.IO) {
    try {
      if (destinationZip.exists()) {
        destinationZip.delete()
      }

      val allFilesToZip = mutableListOf<Pair<File, String>>()
      for (src in sourceFiles) {
        if (src.isDirectory) {
          src.walkTopDown().forEach { f ->
            val relPath = src.name + "/" + f.relativeTo(src).path
            allFilesToZip.add(f to relPath)
          }
        } else {
          allFilesToZip.add(src to src.name)
        }
      }

      val total = allFilesToZip.size
      var count = 0

      ZipOutputStream(BufferedOutputStream(FileOutputStream(destinationZip))).use { out ->
        val buffer = ByteArray(32 * 1024)
        for ((file, entryName) in allFilesToZip) {
          count++
          onProgress(ArchiveProgress(file.name, count, total, "Compressing"))

          if (file.isDirectory) {
            val entry = ZipEntry(if (entryName.endsWith("/")) entryName else "$entryName/")
            out.putNextEntry(entry)
            out.closeEntry()
          } else {
            val entry = ZipEntry(entryName)
            out.putNextEntry(entry)
            FileInputStream(file).use { origin ->
              var len: Int
              while (origin.read(buffer).also { len = it } != -1) {
                out.write(buffer, 0, len)
              }
            }
            out.closeEntry()
          }
        }
      }

      onProgress(ArchiveProgress(destinationZip.name, total, total, "Completed"))
      Result.success(destinationZip)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun extractZipArchive(
    zipFile: File,
    targetDirectory: File,
    onProgress: (ArchiveProgress) -> Unit = {}
  ): Result<File> = withContext(Dispatchers.IO) {
    try {
      if (!targetDirectory.exists()) {
        targetDirectory.mkdirs()
      }

      var totalEntries = 0
      ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
        while (zis.nextEntry != null) {
          totalEntries++
        }
      }

      var extracted = 0
      val buffer = ByteArray(32 * 1024)

      ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
        var entry: ZipEntry? = zis.nextEntry
        while (entry != null) {
          extracted++
          val outFile = File(targetDirectory, entry.name)

          // Prevent zip slip vulnerability
          val canonicalTarget = targetDirectory.canonicalPath
          val canonicalOut = outFile.canonicalPath
          if (!canonicalOut.startsWith(canonicalTarget)) {
            throw SecurityException("Zip entry is attempting directory traversal: ${entry.name}")
          }

          onProgress(ArchiveProgress(entry.name, extracted, totalEntries, "Extracting"))

          if (entry.isDirectory) {
            outFile.mkdirs()
          } else {
            outFile.parentFile?.mkdirs()
            FileOutputStream(outFile).use { fos ->
              var len: Int
              while (zis.read(buffer).also { len = it } != -1) {
                fos.write(buffer, 0, len)
              }
            }
          }
          zis.closeEntry()
          entry = zis.nextEntry
        }
      }

      onProgress(ArchiveProgress(zipFile.name, totalEntries, totalEntries, "Completed"))
      Result.success(targetDirectory)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
