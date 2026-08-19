package com.jackattackk246.files.ui

import android.text.format.Formatter
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jackattackk246.files.model.FileItem
import com.jackattackk246.files.model.FileSortOrder
import com.jackattackk246.files.model.SearchLocation
import com.jackattackk246.files.model.SearchOptions
import com.jackattackk246.files.model.SearchStyle
import com.jackattackk246.files.ui.theme.AppThemeMode
import com.jackattackk246.files.ui.theme.ThemeManager
import com.jackattackk246.files.util.FileManager
import com.jackattackk246.files.util.HapticManager
import com.jackattackk246.files.util.HapticFeedbackHelper
import com.jackattackk246.files.util.LocalFileQueryEngine
import com.jackattackk246.files.util.RecycleBinEngine
import com.jackattackk246.files.util.RecycledFileRecord
import com.jackattackk246.files.ui.dialog.QuickPreviewDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExplorerScreen(
  currentDirectory: File,
  filesList: List<FileItem>,
  highlightFilePath: String?,
  searchQuery: String,
  onSearchQueryChanged: (String) -> Unit,
  searchOptions: SearchOptions,
  onSearchOptionsChanged: (SearchOptions) -> Unit,
  onNavigateToDirectory: (File) -> Unit,
  onFileItemClick: (FileItem) -> Unit,
  onFileItemLongClick: (FileItem) -> Unit,
  onBatchZipRequest: (List<FileItem>) -> Unit,
  onCreateFolderRequest: () -> Unit,
  themeMode: AppThemeMode = AppThemeMode.MIDNIGHT_MATTE_BLACK,
  customAccentColor: Color? = null
) {
  val context = LocalContext.current
  val selectedFiles = remember { mutableStateListOf<FileItem>() }
  var isMultiSelectMode by remember { mutableStateOf(false) }
  var quickPreviewItem by remember { mutableStateOf<FileItem?>(null) }

  // Sorting state using LocalFileQueryEngine with 100% offline memory Comparator
  var currentSortOrder by remember { mutableStateOf(FileSortOrder.DEFAULT) }
  var isSortDropdownExpanded by remember { mutableStateOf(false) }

  // Dedicated Recycle Bin state and detection
  val isRecycleBinMode = remember(currentDirectory) {
    currentDirectory.name == ".recycle_bin" || currentDirectory.name == ".jack_recycle_bin" ||
        currentDirectory.absolutePath == RecycleBinEngine.getRecycleRootDirectory().absolutePath
  }

  var recycleRefreshCounter by remember { mutableStateOf(0) }
  val recycledRecords = remember(recycleRefreshCounter, filesList, isRecycleBinMode) {
    if (isRecycleBinMode) {
      RecycleBinEngine.getRecycledItems()
    } else {
      emptyList()
    }
  }

  val totalRecycledWastedBytes = remember(recycledRecords, filesList, isRecycleBinMode) {
    if (recycledRecords.isNotEmpty()) {
      recycledRecords.sumOf { it.fileSize }
    } else {
      filesList.filter { it.name != "recycle_manifest.json" }.sumOf { it.size }
    }
  }

  // Integrate local offline AI vector similarity search if search query is active
  val sortedFilesList = remember(filesList, currentSortOrder, searchQuery, searchOptions.deepTextSearch, searchOptions.isSmartSearch, isRecycleBinMode) {
    val base = if (isRecycleBinMode) {
      filesList.filter { it.name != "recycle_manifest.json" }
    } else {
      filesList
    }
    val sorted = LocalFileQueryEngine.sortFiles(base, currentSortOrder)
    if (searchQuery.isNotBlank()) {
      val isSmart = searchOptions.isSmartSearch || searchOptions.deepTextSearch
      if (isSmart) {
        com.jackattackk246.files.ai.LocalOfflineAiModule.querySemanticVectorSimilarity(sorted, searchQuery)
      } else {
        sorted.filter { it.name.lowercase().contains(searchQuery.trim().lowercase()) }
      }
    } else {
      sorted
    }
  }

  val isLight = ThemeManager.isLightBackgroundProfile(themeMode)
  val primaryTextColor = ThemeManager.getAdaptivePrimaryTextColor(themeMode)
  val secondaryTextColor = ThemeManager.getAdaptiveSecondaryTextColor(themeMode)
  val accentColor = ThemeManager.getThemeAccentColor(themeMode, customAccentColor)
  val cardContainer = ThemeManager.getAdaptiveCardContainerColor(themeMode)
  val cardBorder = ThemeManager.getAdaptiveCardBorderColor(themeMode)

  quickPreviewItem?.let { item ->
    QuickPreviewDialog(
      fileItem = item,
      themeMode = themeMode,
      customAccentColor = customAccentColor,
      onDismiss = { quickPreviewItem = null },
      onOpenWithSystemDefault = {
        try {
          com.jackattackk246.files.util.RecentFilesTracker.recordAccess(item.file)
        } catch (_: Exception) {}
        FileManager.openWithSystemDefault(context, item.file)
      },
      onOpenManageDialog = {
        onFileItemLongClick(item)
      }
    )
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("explorer_screen")
  ) {
    // Top Search Header if SearchLocation == TOP_TOOLBAR (hidden in Recycle Bin to keep view focused)
    if (!isRecycleBinMode && searchOptions.location == SearchLocation.TOP_TOOLBAR) {
      SearchHeaderSection(
        searchQuery = searchQuery,
        onSearchQueryChanged = onSearchQueryChanged,
        searchOptions = searchOptions,
        onSearchOptionsChanged = onSearchOptionsChanged,
        themeMode = themeMode,
        customAccentColor = customAccentColor,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
      )
    }

    // Path Breadcrumbs Navigation Bar
    PathBreadcrumbsBar(
      currentDirectory = currentDirectory,
      onNavigateToDirectory = onNavigateToDirectory,
      themeMode = themeMode,
      customAccentColor = customAccentColor,
      isRecycleBin = isRecycleBinMode,
      onOpenSafBackdoor = {
        FileManager.openPathSAFBackdoor(context, currentDirectory.absolutePath)
      }
    )

    if (isRecycleBinMode) {
      // ==========================================
      // RECYCLE BIN DEDICATED HUB VIEW
      // ==========================================
      // 1. Top Recycle Bin Header & Utility Metrics
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp)
          .testTag("recycle_bin_metrics_banner"),
        color = cardContainer,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, cardBorder)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          val itemCount = if (recycledRecords.isNotEmpty()) recycledRecords.size else sortedFilesList.size
          val spaceText = Formatter.formatFileSize(context, totalRecycledWastedBytes)

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Total Deleted Items",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                color = secondaryTextColor
              )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "$itemCount Items | $spaceText Wasted",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
              )
            )
          }

          if (itemCount > 0) {
            Spacer(modifier = Modifier.width(12.dp))

            // Prominent "Empty Recycle Bin" Action Button
            Button(
              onClick = {
                HapticManager.errorPulse(context)
                val success = RecycleBinEngine.emptyRecycleBin()
                if (success) {
                  Toast.makeText(context, "Recycle Bin Emptied", Toast.LENGTH_SHORT).show()
                  recycleRefreshCounter++
                  onNavigateToDirectory(currentDirectory)
                } else {
                  Toast.makeText(context, "Recycle Bin is already empty", Toast.LENGTH_SHORT).show()
                }
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = if (isLight) Color.White else Color.Black
              ),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
              modifier = Modifier
                .defaultMinSize(minHeight = 40.dp)
                .testTag("empty_recycle_bin_button")
            ) {
              Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = "Empty Recycle Bin",
                tint = if (isLight) Color.White else Color.Black,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Empty Bin",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isLight) Color.White else Color.Black
              )
            }
          }
        }
      }

      // 2. Deleted Items List or Stylized Empty State
      val isBinEmpty = recycledRecords.isEmpty() && sortedFilesList.isEmpty()

      if (isBinEmpty) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(32.dp)
            .testTag("recycle_bin_empty_state"),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Box(
              modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Empty Trash",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(42.dp)
              )
            }
            Text(
              text = "Recycle Bin is empty",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
              )
            )
            Text(
              text = "Files moved to the Recycle Bin will appear here and can be restored or erased permanently.",
              style = MaterialTheme.typography.bodySmall.copy(
                color = secondaryTextColor,
                textAlign = TextAlign.Center
              ),
              modifier = Modifier.padding(horizontal = 28.dp)
            )
          }
        }
      } else {
        // Render authentic deleted item card views
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .testTag("recycle_bin_items_list"),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 40.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          if (recycledRecords.isNotEmpty()) {
            items(
              items = recycledRecords,
              key = { it.id }
            ) { record ->
              val formattedDate = remember(record.deletedTimestamp) {
                SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(record.deletedTimestamp))
              }
              val formattedSize = remember(record.fileSize) {
                Formatter.formatFileSize(context, record.fileSize)
              }

              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .testTag("recycled_card_${record.id}"),
                colors = CardDefaults.cardColors(containerColor = cardContainer),
                border = BorderStroke(1.dp, cardBorder)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                  // Left: Thumbnail Icon
                  Box(
                    modifier = Modifier
                      .size(44.dp)
                      .clip(RoundedCornerShape(10.dp))
                      .background(accentColor.copy(alpha = 0.16f))
                      .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = if (record.isDirectory) {
                        Icons.Default.Folder
                      } else {
                        when (File(record.fileName).extension.lowercase()) {
                          "jpg", "jpeg", "png", "webp", "gif" -> Icons.Default.Image
                          "mp4", "mkv", "mov", "webm" -> Icons.Default.Videocam
                          "mp3", "wav", "flac", "ogg" -> Icons.Default.Audiotrack
                          "pdf", "doc", "docx", "txt" -> Icons.Default.Description
                          "zip", "rar", "tar", "gz" -> Icons.Default.FolderZip
                          "apk" -> Icons.Default.Android
                          else -> Icons.Default.InsertDriveFile
                        }
                      },
                      contentDescription = null,
                      tint = accentColor,
                      modifier = Modifier.size(24.dp)
                    )
                  }

                  // Center: Original File Name, Original Location Path, and Deleted Timestamp
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = record.fileName,
                      style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor
                      ),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = "From: ${record.originalPath}",
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = secondaryTextColor
                      ),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = "Deleted: $formattedDate • $formattedSize",
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = secondaryTextColor.copy(alpha = 0.75f)
                      ),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }

                  // Right: Dedicated Restore & Delete Permanently Action Buttons
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    // Restore Button
                    IconButton(
                      onClick = {
                        val success = RecycleBinEngine.restoreItem(record.id)
                        if (success) {
                          HapticFeedbackHelper.performTransferSuccessFeedback(context)
                          Toast.makeText(context, "Restored: ${record.fileName}", Toast.LENGTH_SHORT).show()
                          recycleRefreshCounter++
                          onNavigateToDirectory(currentDirectory)
                        } else {
                          Toast.makeText(context, "Failed to restore item", Toast.LENGTH_SHORT).show()
                        }
                      },
                      modifier = Modifier
                        .size(40.dp)
                        .testTag("restore_button_${record.id}")
                    ) {
                      Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Restore File",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(22.dp)
                      )
                    }

                    // Delete Permanently Trash Button
                    IconButton(
                      onClick = {
                        val success = RecycleBinEngine.deletePermanently(record.id)
                        if (success) {
                          HapticManager.errorPulse(context)
                          Toast.makeText(context, "Permanently deleted: ${record.fileName}", Toast.LENGTH_SHORT).show()
                          recycleRefreshCounter++
                          onNavigateToDirectory(currentDirectory)
                        } else {
                          Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show()
                        }
                      },
                      modifier = Modifier
                        .size(40.dp)
                        .testTag("delete_permanently_button_${record.id}")
                    ) {
                      Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Delete Permanently",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(22.dp)
                      )
                    }
                  }
                }
              }
            }
          } else {
            // Fallback for direct files in the recycle directory without manifest records
            items(
              items = sortedFilesList,
              key = { it.path }
            ) { item ->
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .testTag("recycled_card_${item.name}"),
                colors = CardDefaults.cardColors(containerColor = cardContainer),
                border = BorderStroke(1.dp, cardBorder)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(44.dp)
                      .clip(RoundedCornerShape(10.dp))
                      .background(accentColor.copy(alpha = 0.16f))
                      .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                      contentDescription = null,
                      tint = accentColor,
                      modifier = Modifier.size(24.dp)
                    )
                  }

                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = item.name,
                      style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor
                      ),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = "From: Recycle Bin Storage",
                      style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = "${item.formattedSize} • ${item.extension.uppercase()}",
                      style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor.copy(alpha = 0.75f))
                    )
                  }

                  IconButton(
                    onClick = {
                      val success = item.file.deleteRecursively()
                      if (success) {
                        HapticManager.errorPulse(context)
                        Toast.makeText(context, "Permanently deleted: ${item.name}", Toast.LENGTH_SHORT).show()
                        onNavigateToDirectory(currentDirectory)
                      }
                    },
                    modifier = Modifier
                      .size(40.dp)
                      .testTag("delete_permanently_raw_${item.name}")
                  ) {
                    Icon(
                      imageVector = Icons.Default.DeleteForever,
                      contentDescription = "Delete Permanently",
                      tint = Color(0xFFEF4444),
                      modifier = Modifier.size(22.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    } else {
      // ==========================================
      // STANDARD DIRECTORY EXPLORER VIEW
      // ==========================================
      // Sorting & Quick Filter Toolbar with Native Spinner / Dropdown Menu
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cardContainer.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, cardBorder.copy(alpha = 0.40f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Folder,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "${sortedFilesList.size} items",
              style = MaterialTheme.typography.labelMedium.copy(
                color = secondaryTextColor,
                fontWeight = FontWeight.Medium
              )
            )
          }

          // Lightweight Standard Native Spinner Dropdown List
          Box {
            OutlinedButton(
              onClick = {
                HapticFeedbackHelper.performToggleFeedback(context)
                isSortDropdownExpanded = true
              },
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)),
              colors = ButtonDefaults.outlinedButtonColors(
                containerColor = cardContainer
              ),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier
                .defaultMinSize(minWidth = 56.dp, minHeight = 36.dp)
                .testTag("sort_spinner_button")
            ) {
              Icon(
                imageVector = Icons.Default.Sort,
                contentDescription = "Sort Files",
                tint = accentColor,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = currentSortOrder.shortName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
              )
              Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = primaryTextColor,
                modifier = Modifier.size(16.dp)
              )
            }

            DropdownMenu(
              expanded = isSortDropdownExpanded,
              onDismissRequest = { isSortDropdownExpanded = false },
              modifier = Modifier
                .background(cardContainer)
                .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                .testTag("sort_dropdown_menu")
            ) {
              FileSortOrder.values().forEach { orderOption ->
                val isSelected = orderOption == currentSortOrder
                DropdownMenuItem(
                  text = {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = orderOption.label,
                        style = MaterialTheme.typography.bodySmall.copy(
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                          color = if (isSelected) accentColor else primaryTextColor
                        )
                      )
                      if (isSelected) {
                        Icon(
                          imageVector = Icons.Default.Check,
                          contentDescription = "Selected",
                          tint = accentColor,
                          modifier = Modifier.size(16.dp)
                        )
                      }
                    }
                  },
                  onClick = {
                    HapticFeedbackHelper.performToggleFeedback(context)
                    currentSortOrder = orderOption
                    isSortDropdownExpanded = false
                  },
                  modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("sort_option_${orderOption.name}")
                )
              }
            }
          }
        }
      }

      // Batch Multi-Select Action Bar if active
      if (isMultiSelectMode && selectedFiles.isNotEmpty()) {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = accentColor.copy(alpha = 0.25f),
          border = BorderStroke(1.dp, accentColor)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${selectedFiles.size} Items Selected",
              fontWeight = FontWeight.Bold,
              color = primaryTextColor
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = {
                  onBatchZipRequest(selectedFiles.toList())
                  selectedFiles.clear()
                  isMultiSelectMode = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
              ) {
                Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Zip Batch", color = if (isLight) Color.White else Color.Black)
              }
              TextButton(
                onClick = {
                  selectedFiles.clear()
                  isMultiSelectMode = false
                }
              ) {
                Text("Cancel", color = primaryTextColor)
              }
            }
          }
        }
      }

      // Directory Tree File Items List
      if (sortedFilesList.isEmpty()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
              imageVector = Icons.Default.FolderOpen,
              contentDescription = null,
              tint = secondaryTextColor.copy(alpha = 0.5f),
              modifier = Modifier.size(64.dp)
            )
            Text(
              text = if (searchQuery.isNotEmpty()) "No files match '$searchQuery'" else "Folder is empty",
              style = MaterialTheme.typography.titleMedium.copy(color = secondaryTextColor)
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.weight(1f),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(
            items = sortedFilesList,
            key = { it.path }
          ) { item ->
            val isHighlighted = highlightFilePath != null && item.path == highlightFilePath
            val isSelected = selectedFiles.contains(item)

            // Visual Flash / Highlight animation
            val animatedBgColor by animateColorAsState(
              targetValue = when {
                isSelected -> accentColor.copy(alpha = 0.25f)
                isHighlighted -> accentColor.copy(alpha = 0.40f)
                else -> cardContainer
              },
              animationSpec = tween(durationMillis = if (isHighlighted) 300 else 400),
              label = "file_row_flash_animation"
            )

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .combinedClickable(
                  onClick = {
                    HapticManager.selectionTick(context)
                    if (isMultiSelectMode) {
                      if (isSelected) selectedFiles.remove(item) else selectedFiles.add(item)
                      if (selectedFiles.isEmpty()) isMultiSelectMode = false
                    } else {
                      onFileItemClick(item)
                    }
                  },
                  onLongClick = {
                    HapticManager.longPress(context)
                    if (isMultiSelectMode) {
                      if (isSelected) selectedFiles.remove(item) else selectedFiles.add(item)
                      if (selectedFiles.isEmpty()) isMultiSelectMode = false
                    } else {
                      quickPreviewItem = item
                    }
                  }
                )
                .testTag("file_row_${item.name}"),
              colors = CardDefaults.cardColors(containerColor = animatedBgColor),
              border = BorderStroke(1.dp, if (isSelected || isHighlighted) accentColor else cardBorder)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                if (isMultiSelectMode) {
                  Checkbox(
                    checked = isSelected,
                    onCheckedChange = {
                      if (it) selectedFiles.add(item) else selectedFiles.remove(item)
                    },
                    colors = CheckboxDefaults.colors(
                      checkedColor = accentColor,
                      checkmarkColor = if (isLight) Color.White else Color.Black
                    )
                  )
                } else {
                  Box(
                    modifier = Modifier
                      .size(40.dp)
                      .clip(RoundedCornerShape(10.dp))
                      .background(accentColor.copy(alpha = 0.20f))
                      .border(1.dp, accentColor.copy(alpha = 0.40f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = when {
                        item.isDirectory -> Icons.Default.Folder
                        item.isMinecraftFile -> Icons.Default.SportsEsports
                        item.extension == "zip" -> Icons.Default.FolderZip
                        else -> Icons.Default.InsertDriveFile
                      },
                      contentDescription = null,
                      tint = accentColor,
                      modifier = Modifier.size(22.dp)
                    )
                  }
                }

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = primaryTextColor),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = if (item.isDirectory) "Directory" else "${item.formattedSize} • ${item.extension.uppercase()}",
                    style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
                  )
                }

                IconButton(
                  onClick = { onFileItemLongClick(item) },
                  modifier = Modifier.testTag("file_options_${item.name}")
                ) {
                  Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = secondaryTextColor
                  )
                }
              }
            }
          }
        }
      }

      // Bottom Search Header if SearchLocation == BOTTOM_BAR
      if (searchOptions.location == SearchLocation.BOTTOM_BAR) {
        SearchHeaderSection(
          searchQuery = searchQuery,
          onSearchQueryChanged = onSearchQueryChanged,
          searchOptions = searchOptions,
          onSearchOptionsChanged = onSearchOptionsChanged,
          themeMode = themeMode,
          customAccentColor = customAccentColor,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
      }
    }
  }
}

@Composable
private fun PathBreadcrumbsBar(
  currentDirectory: File,
  onNavigateToDirectory: (File) -> Unit,
  themeMode: AppThemeMode = AppThemeMode.MIDNIGHT_MATTE_BLACK,
  customAccentColor: Color? = null,
  isRecycleBin: Boolean = false,
  onOpenSafBackdoor: () -> Unit
) {
  val root = FileManager.getRootDirectory()
  val pathSegments = remember(currentDirectory, isRecycleBin) {
    if (isRecycleBin) {
      listOf(root, currentDirectory)
    } else {
      val list = mutableListOf<File>()
      var curr: File? = currentDirectory
      while (curr != null && curr.path.startsWith(root.path)) {
        list.add(0, curr)
        if (curr.path == root.path) break
        curr = curr.parentFile
      }
      if (list.isEmpty()) list.add(root)
      list
    }
  }

  val desktopPalette by com.aistudio.fileslauncher.ui.ThemeSynchronizationBridge.paletteState.collectAsState()
  val primaryTextColor = ThemeManager.getAdaptivePrimaryTextColor(themeMode)
  val secondaryTextColor = ThemeManager.getAdaptiveSecondaryTextColor(themeMode)
  val accentColor = ThemeManager.getThemeAccentColor(themeMode, customAccentColor)
  val cardContainer = if (desktopPalette.isDesktopCanvasActive) desktopPalette.globalNavBarFrameColor else ThemeManager.getAdaptiveCardContainerColor(themeMode)
  val cardBorder = ThemeManager.getAdaptiveCardBorderColor(themeMode)

  Surface(
    modifier = Modifier.fillMaxWidth(),
    color = cardContainer,
    border = BorderStroke(1.dp, cardBorder)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (isRecycleBin || (currentDirectory.path != root.path && currentDirectory.parentFile != null)) {
        IconButton(
          onClick = {
            if (isRecycleBin) {
              onNavigateToDirectory(root)
            } else {
              onNavigateToDirectory(currentDirectory.parentFile!!)
            }
          },
          modifier = Modifier.size(32.dp).testTag("up_directory_button")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Up", tint = primaryTextColor, modifier = Modifier.size(18.dp))
        }
      }

      Row(
        modifier = Modifier
          .weight(1f)
          .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        pathSegments.forEachIndexed { index, seg ->
          val segmentLabel = when {
            index == 0 -> "Device Storage"
            isRecycleBin && index == pathSegments.lastIndex -> "Recycle Bin"
            else -> seg.name
          }
          Text(
            text = segmentLabel,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = if (index == pathSegments.lastIndex) FontWeight.Bold else FontWeight.Medium,
              color = if (index == pathSegments.lastIndex) accentColor else primaryTextColor
            ),
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .clickable { onNavigateToDirectory(seg) }
              .padding(horizontal = 6.dp, vertical = 4.dp)
          )
          if (index < pathSegments.lastIndex) {
            Text("/", color = secondaryTextColor, fontSize = 12.sp)
          }
        }
      }

      IconButton(
        onClick = onOpenSafBackdoor,
        modifier = Modifier.size(32.dp).testTag("saf_backdoor_header_button")
      ) {
        Icon(Icons.Default.OpenInNew, contentDescription = "Open Files", tint = accentColor, modifier = Modifier.size(18.dp))
      }
    }
  }
}

@Composable
private fun SearchHeaderSection(
  searchQuery: String,
  onSearchQueryChanged: (String) -> Unit,
  searchOptions: SearchOptions,
  onSearchOptionsChanged: (SearchOptions) -> Unit,
  themeMode: AppThemeMode = AppThemeMode.MIDNIGHT_MATTE_BLACK,
  customAccentColor: Color? = null,
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(searchOptions.style == SearchStyle.EXPANDED_BOX) }
  val primaryTextColor = ThemeManager.getAdaptivePrimaryTextColor(themeMode)
  val secondaryTextColor = ThemeManager.getAdaptiveSecondaryTextColor(themeMode)
  val accentColor = ThemeManager.getThemeAccentColor(themeMode, customAccentColor)
  val cardContainer = ThemeManager.getAdaptiveCardContainerColor(themeMode)
  val isLight = ThemeManager.isLightBackgroundProfile(themeMode)

  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
    if (searchOptions.style == SearchStyle.MINIMAL_ICON && !isExpanded && searchQuery.isEmpty()) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        IconButton(
          onClick = { isExpanded = true },
          modifier = Modifier
            .background(cardContainer, CircleShape)
            .testTag("minimal_search_icon_button")
        ) {
          Icon(Icons.Default.Search, contentDescription = "Search", tint = accentColor)
        }
      }
    } else {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("search_text_input_box"),
        placeholder = { Text("Search files or deep text...", color = secondaryTextColor) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = accentColor) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchQueryChanged("") }) {
              Icon(Icons.Default.Close, contentDescription = "Clear", tint = primaryTextColor)
            }
          }
        },
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = primaryTextColor,
          unfocusedTextColor = primaryTextColor,
          focusedContainerColor = cardContainer,
          unfocusedContainerColor = cardContainer,
          focusedBorderColor = accentColor,
          unfocusedBorderColor = ThemeManager.getAdaptiveCardBorderColor(themeMode)
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true
      )

      // Offline AI Smart Search Input Suggestions Array
      val currentContext = LocalContext.current
      val aiState by com.jackattackk246.files.ai.LocalOfflineAiModule.stateFlow.collectAsState()
      if (aiState.isInitialized && aiState.activeCategorySuggestions.isNotEmpty()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          aiState.activeCategorySuggestions.forEach { suggestion ->
            val isSelected = searchQuery.contains(suggestion.queryToken, ignoreCase = true) ||
                searchQuery.equals(suggestion.label, ignoreCase = true)
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                  HapticFeedbackHelper.performToggleFeedback(currentContext)
                  if (isSelected) {
                    onSearchQueryChanged("")
                  } else {
                    onSearchQueryChanged(suggestion.label.lowercase())
                  }
                }
                .testTag("smart_search_chip_${suggestion.label.lowercase()}"),
              color = if (isSelected) accentColor.copy(alpha = 0.35f) else cardContainer,
              border = BorderStroke(1.dp, if (isSelected) accentColor else ThemeManager.getAdaptiveCardBorderColor(themeMode)),
              shape = RoundedCornerShape(12.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = when (suggestion.iconType) {
                    "image" -> Icons.Default.Image
                    "video" -> Icons.Default.Videocam
                    "audio" -> Icons.Default.Audiotrack
                    "doc" -> Icons.Default.Description
                    "zip" -> Icons.Default.FolderZip
                    "apk" -> Icons.Default.Android
                    "text" -> Icons.Default.FindInPage
                    else -> Icons.Default.AutoAwesome
                  },
                  contentDescription = null,
                  tint = if (isSelected) accentColor else secondaryTextColor,
                  modifier = Modifier.size(13.dp)
                )
                Text(
                  text = suggestion.label,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) primaryTextColor else secondaryTextColor
                )
              }
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(
            checked = searchOptions.currentDirOnly,
            onCheckedChange = { onSearchOptionsChanged(searchOptions.copy(currentDirOnly = it)) },
            modifier = Modifier.testTag("current_dir_search_checkbox"),
            colors = CheckboxDefaults.colors(
              checkedColor = accentColor,
              checkmarkColor = if (isLight) Color.White else Color.Black
            )
          )
          Text("Search solely in current dir", fontSize = 12.sp, color = secondaryTextColor)
        }

        if (searchOptions.deepTextSearch) {
          Text("Deep Text Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }
      }
    }
  }
}
