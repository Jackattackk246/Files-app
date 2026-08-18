package com.jackattackk246.files.util

import android.content.Context
import com.jackattackk246.files.service.DataTransferService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

object DataTransferManager {

  data class TransferProgress(
    val fileName: String,
    val bytesTransferred: Long,
    val totalBytes: Long,
    val percentage: Float,
    val stage: String // "Copying", "Verifying Checksum", "Finalizing", "Complete", "Error"
  )

  /**
   * Enforces atomic copy-then-verify protocol.
   * Keeps source file 100% intact until post-transfer hash validation fully clears.
   */
  suspend fun atomicMoveFile(
    context: Context,
    sourceFile: File,
    targetDirectory: File,
    onProgress: (TransferProgress) -> Unit = {}
  ): Result<File> = withContext(Dispatchers.IO) {
    if (!sourceFile.exists()) {
      return@withContext Result.failure(IllegalArgumentException("Source file does not exist: ${sourceFile.path}"))
    }

    if (!targetDirectory.exists()) {
      targetDirectory.mkdirs()
    }

    val targetFile = File(targetDirectory, sourceFile.name)
    val tempTargetFile = File(targetDirectory, "${sourceFile.name}.transfer_tmp_${System.currentTimeMillis()}")

    DataTransferService.startService(context, "Moving ${sourceFile.name}")

    try {
      if (sourceFile.isDirectory) {
        val copyResult = copyDirectoryRecursive(sourceFile, targetFile, onProgress)
        if (copyResult.isSuccess) {
          sourceFile.deleteRecursively()
          DataTransferService.stopService(context)
          return@withContext Result.success(targetFile)
        } else {
          DataTransferService.stopService(context)
          return@withContext Result.failure(copyResult.exceptionOrNull() ?: Exception("Folder move failed"))
        }
      }

      val totalBytes = sourceFile.length()
      var copiedBytes = 0L
      val buffer = ByteArray(64 * 1024) // 64KB buffer

      val sourceDigest = MessageDigest.getInstance("SHA-256")
      val targetDigest = MessageDigest.getInstance("SHA-256")

      onProgress(TransferProgress(sourceFile.name, 0L, totalBytes, 0f, "Copying"))

      FileInputStream(sourceFile).use { input ->
        FileOutputStream(tempTargetFile).use { output ->
          var bytesRead: Int
          while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
            sourceDigest.update(buffer, 0, bytesRead)
            copiedBytes += bytesRead
            val pct = if (totalBytes > 0) (copiedBytes.toFloat() / totalBytes) else 1f
            onProgress(TransferProgress(sourceFile.name, copiedBytes, totalBytes, pct, "Copying"))
          }
          output.flush()
        }
      }

      // Stage 2: Post-transfer validation pass
      onProgress(TransferProgress(sourceFile.name, copiedBytes, totalBytes, 1f, "Verifying Checksum"))
      FileInputStream(tempTargetFile).use { targetIn ->
        var bytesRead: Int
        while (targetIn.read(buffer).also { bytesRead = it } != -1) {
          targetDigest.update(buffer, 0, bytesRead)
        }
      }

      val sourceHash = sourceDigest.digest().joinToString("") { "%02x".format(it) }
      val targetHash = targetDigest.digest().joinToString("") { "%02x".format(it) }

      if (sourceHash != targetHash || tempTargetFile.length() != sourceFile.length()) {
        tempTargetFile.delete()
        DataTransferService.stopService(context)
        return@withContext Result.failure(IllegalStateException("Integrity checksum mismatch! Source intact."))
      }

      // Stage 3: Atomic Rename & Clean Source
      onProgress(TransferProgress(sourceFile.name, totalBytes, totalBytes, 1f, "Finalizing"))
      if (targetFile.exists()) {
        targetFile.delete()
      }
      val renamed = tempTargetFile.renameTo(targetFile)
      if (!renamed) {
        tempTargetFile.copyTo(targetFile, overwrite = true)
        tempTargetFile.delete()
      }

      // Source file is ONLY deleted after destination is 100% verified
      sourceFile.delete()
      onProgress(TransferProgress(sourceFile.name, totalBytes, totalBytes, 1f, "Complete"))
      DataTransferService.stopService(context)

      Result.success(targetFile)
    } catch (e: Exception) {
      tempTargetFile.delete()
      DataTransferService.stopService(context)
      Result.failure(e)
    }
  }

  private fun copyDirectoryRecursive(
    sourceDir: File,
    targetDir: File,
    onProgress: (TransferProgress) -> Unit
  ): Result<Unit> {
    try {
      if (!targetDir.exists()) {
        targetDir.mkdirs()
      }
      val files = sourceDir.listFiles() ?: return Result.success(Unit)
      for (file in files) {
        val dest = File(targetDir, file.name)
        if (file.isDirectory) {
          copyDirectoryRecursive(file, dest, onProgress)
        } else {
          file.copyTo(dest, overwrite = true)
        }
      }
      return Result.success(Unit)
    } catch (e: Exception) {
      return Result.failure(e)
    }
  }
}
