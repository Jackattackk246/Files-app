package com.jackattackk246.files.ui

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
import java.io.File

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

  // Sorting state using LocalFileQueryEngine with 100% offline memory Comparator
  var currentSortOrder by remember { mutableStateOf(FileSortOrder.DEFAULT) }
  var isSortDropdownExpanded by remember { mutableStateOf(false) }

  // Integrate local offline AI semantic ranking if search query is active
  val sortedFilesList = remember(filesList, currentSortOrder, searchQuery) {
    val sorted = LocalFileQueryEngine.sortFiles(filesList, currentSortOrder)
    if (searchQuery.isNotBlank()) {
      com.jackattackk246.files.ai.LocalOfflineAiModule.rankFilesBySemanticRelevance(sorted, searchQuery)
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
  // Matching existing crimson theme border variable
  val crimsonBorderColor = ThemeManager.getAdaptiveCardBorderColor(AppThemeMode.CRIMSON_FURY)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("explorer_screen")
  ) {
    // Top Search Header if SearchLocation == TOP_TOOLBAR
    if (searchOptions.location == SearchLocation.TOP_TOOLBAR) {
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
      onOpenSafBackdoor = {
        FileManager.openPathSAFBackdoor(context, currentDirectory.absolutePath)
      }
    )

    // Sorting & Quick Filter Toolbar with Native Spinner / Dropdown Menu
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = cardContainer.copy(alpha = 0.65f),
      border = BorderStroke(1.dp, crimsonBorderColor.copy(alpha = 0.40f))
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
            border = BorderStroke(1.dp, crimsonBorderColor),
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
              tint = crimsonBorderColor,
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
              .border(1.dp, crimsonBorderColor, RoundedCornerShape(8.dp))
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
                        color = if (isSelected) crimsonBorderColor else primaryTextColor
                      )
                    )
                    if (isSelected) {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = crimsonBorderColor,
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
                  if (!isMultiSelectMode) {
                    isMultiSelectMode = true
                    selectedFiles.add(item)
                  }
                  onFileItemLongClick(item)
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

@Composable
private fun PathBreadcrumbsBar(
  currentDirectory: File,
  onNavigateToDirectory: (File) -> Unit,
  themeMode: AppThemeMode = AppThemeMode.MIDNIGHT_MATTE_BLACK,
  customAccentColor: Color? = null,
  onOpenSafBackdoor: () -> Unit
) {
  val root = FileManager.getRootDirectory()
  val pathSegments = remember(currentDirectory) {
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

  val desktopPalette by com.aistudio.fileslauncher.ui.ThemeSynchronizationBridge.paletteState.collectAsState()
  val primaryTextColor = ThemeManager.getAdaptivePrimaryTextColor(themeMode)
  val secondaryTextColor = ThemeManager.getAdaptiveSecondaryTextColor(themeMode)
  val accentColor = customAccentColor ?: desktopPalette.customAccentColor
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
      if (currentDirectory.path != root.path && currentDirectory.parentFile != null) {
        IconButton(
          onClick = { onNavigateToDirectory(currentDirectory.parentFile!!) },
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
          Text(
            text = if (seg.path == root.path) "Device Storage" else seg.name,
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
