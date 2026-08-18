package com.jackattackk246.files.util

import androidx.compose.runtime.mutableStateListOf
import com.jackattackk246.files.model.RecentFileItem
import java.io.File

object RecentFilesTracker {
  private val recents = mutableStateListOf<RecentFileItem>()

  fun getRecents(): List<RecentFileItem> {
    return recents
  }

  fun recordAccess(file: File) {
    if (!file.exists()) return
    // Remove if already in list to move it to top
    recents.removeAll { it.path == file.absolutePath }
    recents.add(0, RecentFileItem(path = file.absolutePath))
    if (recents.size > 50) {
      recents.removeAt(recents.lastIndex)
    }
  }

  fun removeAll(predicate: (RecentFileItem) -> Boolean) {
    recents.removeAll(predicate)
  }

  fun clear() {
    recents.clear()
  }
}
