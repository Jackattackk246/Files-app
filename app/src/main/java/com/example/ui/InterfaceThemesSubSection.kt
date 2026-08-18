package com.jackattackk246.files.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jackattackk246.files.ui.theme.AppThemeMode
import com.jackattackk246.files.ui.theme.ThemeManager
import com.jackattackk246.files.util.HapticManager
import com.jackattackk246.files.util.IconChangerEngine
import com.jackattackk246.files.util.LauncherIconVariant
import com.jackattackk246.files.util.ThemePreferences
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Interface Themes Sub-Section with Master 100 Design Options Catalog:
 * - Direct index binding to the master compiler loop: Every tap activates both the launcher alias & the exact custom color profile.
 * - Compressed slim-list structure using wrap_content parameters and zero excessive padding.
 * - Integrated HapticManager providing tactile pulses on navigation, selection, and theme switching.
 */
@Composable
fun InterfaceThemesSubSection(
  currentThemeMode: AppThemeMode,
  onThemeModeChanged: (AppThemeMode) -> Unit,
  customAccentColor: Color?,
  onCustomAccentColorChanged: (Color?) -> Unit,
  onOpenColorPicker: () -> Unit,
  onOpenEnvironmentalEngineDialog: (() -> Unit)? = null,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val activeThemeAccent = ThemeManager.getThemeAccentColor(currentThemeMode, customAccentColor)
  val cardContainer = ThemeManager.getAdaptiveCardContainerColor(currentThemeMode)
  val cardBorder = ThemeManager.getAdaptiveCardBorderColor(currentThemeMode)
  val primaryTextColor = ThemeManager.getAdaptivePrimaryTextColor(currentThemeMode)
  val secondaryTextColor = ThemeManager.getAdaptiveSecondaryTextColor(currentThemeMode)

  var activeLauncherIconId by remember { mutableStateOf(IconChangerEngine.getActiveIconId(context)) }
  var selectedDesignCategoryFilter by remember { mutableStateOf("All") }
  val allVariants = remember { IconChangerEngine.ICON_VARIANTS }
  val designCategories = remember {
    listOf("All", "Canvas Themes", "Core Baselines", "Cyberpunk & Retro", "Industrial & Dev", "Premium Materials", "Pop-Culture & Special", "Specialty Grid", "Master Series")
  }

  // Rigid Pagination Setting: Exactly 10 items max per page -> 100 items = 10 pages
  val pageSize = 10
  var currentCatalogPage by remember { mutableIntStateOf(0) }

  val filteredVariants by remember(selectedDesignCategoryFilter, allVariants) {
    derivedStateOf {
      if (selectedDesignCategoryFilter == "All") {
        allVariants
      } else {
        allVariants.filter { it.category == selectedDesignCategoryFilter }
      }
    }
  }

  val totalPages by remember(filteredVariants.size) {
    derivedStateOf {
      max(1, ceil(filteredVariants.size.toDouble() / pageSize).toInt())
    }
  }

  // Ensure currentCatalogPage stays within bounds when filter changes
  val safeCurrentPage = min(currentCatalogPage, max(0, totalPages - 1))

  // Strict 10-Item Active Window Slice
  val activeWindowTenCardSlice by remember(filteredVariants, safeCurrentPage) {
    derivedStateOf {
      val startIndex = safeCurrentPage * pageSize
      val endIndex = min(startIndex + pageSize, filteredVariants.size)
      if (startIndex < filteredVariants.size) {
        filteredVariants.subList(startIndex, endIndex)
      } else {
        emptyList()
      }
    }
  }

  // Master Style Compiler Loop Activation Function
  fun activateThemeItem(variant: LauncherIconVariant) {
    activeLauncherIconId = variant.id
    HapticManager.themeSwitchPulse(context)

    // Bridge notification with rigid color listener pass across Windows 11 Desktop Canvas & Tablet Toggle Profiles
    com.aistudio.fileslauncher.ui.ThemeSynchronizationBridge.notifyThemeVariantSelected(
      context = context,
      variant = variant,
      onThemeModeChanged = onThemeModeChanged,
      onCustomAccentColorChanged = onCustomAccentColorChanged
    )
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 12.dp)
      .testTag("interface_themes_sub_screen"),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp)
  ) {
    // 1. Sub-Section Header with Back Button
    item {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight()
          .padding(vertical = 2.dp)
      ) {
        IconButton(
          onClick = {
            HapticManager.navigationClick(context)
            onBack()
          },
          modifier = Modifier.testTag("themes_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to Settings",
            tint = primaryTextColor
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Interface Themes",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
            color = primaryTextColor
          )
          Text(
            text = "Master 100 Design Options Catalog (Paginated 10/Page)",
            style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
          )
        }
      }
    }

    // 2. Environmental Engine Option Card
    if (onOpenEnvironmentalEngineDialog != null) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .testTag("environmental_engine_card"),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, cardBorder),
          colors = CardDefaults.cardColors(containerColor = cardContainer)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(activeThemeAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = activeThemeAccent, modifier = Modifier.size(18.dp))
              }
              Column {
                Text("Environmental Engine", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = primaryTextColor)
                Text("Real-time weather, day/night cycles & custom canvas particles", style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor, fontSize = 10.sp))
              }
            }
            Button(
              onClick = {
                HapticManager.navigationClick(context)
                onOpenEnvironmentalEngineDialog()
              },
              modifier = Modifier.testTag("open_environmental_engine_button"),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = activeThemeAccent),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text("Configure", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // 3. Custom Accent Color Picker Card (Slim compressed layout)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight()
          .testTag("custom_accent_color_card"),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, cardBorder),
        colors = CardDefaults.cardColors(containerColor = cardContainer)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(activeThemeAccent)
                .border(1.5.dp, primaryTextColor.copy(alpha = 0.4f), CircleShape)
            )
            Column {
              Text("Custom Accent Color", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = primaryTextColor)
              Text(
                if (customAccentColor != null) "Active: Custom Override" else "Default Preset Accent",
                style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor, fontSize = 10.sp)
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (customAccentColor != null) {
              OutlinedButton(
                onClick = {
                  HapticManager.selectionTick(context)
                  onCustomAccentColorChanged(null)
                  ThemePreferences.setSavedCustomAccentColor(context, null)
                },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, cardBorder),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
              ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(12.dp), tint = secondaryTextColor)
                Spacer(modifier = Modifier.width(3.dp))
                Text("Reset", fontSize = 10.sp, color = secondaryTextColor)
              }
            }

            Button(
              onClick = {
                HapticManager.navigationClick(context)
                onOpenColorPicker()
              },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = activeThemeAccent),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
              modifier = Modifier.height(28.dp)
            ) {
              Text("Pick Color", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // 4. Master 100 Design Options Catalog Card - Compressed Slim-List Structure (wrap_content)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight()
          .testTag("master_design_options_grid_card"),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, cardBorder),
        colors = CardDefaults.cardColors(containerColor = cardContainer)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Header Bar
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(Icons.Default.Apps, contentDescription = null, tint = activeThemeAccent, modifier = Modifier.size(18.dp))
              Text(
                "Master 100 Design Options",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = primaryTextColor
              )
            }

            Surface(
              shape = RoundedCornerShape(50.dp),
              color = activeThemeAccent.copy(alpha = 0.2f),
              border = BorderStroke(1.dp, activeThemeAccent.copy(alpha = 0.5f))
            ) {
              Text(
                text = "${filteredVariants.size} Styles",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = activeThemeAccent,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
              )
            }
          }

          // Compact Category Filter Pills
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            designCategories.take(3).forEach { cat ->
              FilterChip(
                selected = selectedDesignCategoryFilter == cat,
                onClick = {
                  HapticManager.selectionTick(context)
                  selectedDesignCategoryFilter = cat
                  currentCatalogPage = 0
                },
                label = { Text(cat, fontSize = 10.sp) },
                modifier = Modifier.height(28.dp)
              )
            }
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            designCategories.drop(3).take(3).forEach { cat ->
              FilterChip(
                selected = selectedDesignCategoryFilter == cat,
                onClick = {
                  HapticManager.selectionTick(context)
                  selectedDesignCategoryFilter = cat
                  currentCatalogPage = 0
                },
                label = { Text(cat, fontSize = 10.sp) },
                modifier = Modifier.height(28.dp)
              )
            }
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            designCategories.drop(6).forEach { cat ->
              FilterChip(
                selected = selectedDesignCategoryFilter == cat,
                onClick = {
                  HapticManager.selectionTick(context)
                  selectedDesignCategoryFilter = cat
                  currentCatalogPage = 0
                },
                label = { Text(cat, fontSize = 10.sp) },
                modifier = Modifier.height(28.dp)
              )
            }
          }

          HorizontalDivider(color = cardBorder.copy(alpha = 0.4f))

          // Compact Pagination Toolbar
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(cardBorder.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Page ${safeCurrentPage + 1} of $totalPages",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = activeThemeAccent,
                fontSize = 11.sp
              )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              OutlinedButton(
                onClick = {
                  if (safeCurrentPage > 0) {
                    HapticManager.navigationClick(context)
                    currentCatalogPage = safeCurrentPage - 1
                  }
                },
                enabled = safeCurrentPage > 0,
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.height(26.dp)
              ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Page", modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("Prev", fontSize = 10.sp)
              }

              OutlinedButton(
                onClick = {
                  if (safeCurrentPage < totalPages - 1) {
                    HapticManager.navigationClick(context)
                    currentCatalogPage = safeCurrentPage + 1
                  }
                },
                enabled = safeCurrentPage < totalPages - 1,
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.height(26.dp)
              ) {
                Text("Next", fontSize = 10.sp)
                Spacer(modifier = Modifier.width(2.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Page", modifier = Modifier.size(12.dp))
              }
            }
          }

          // Compressed Slim-List Item Structure with wrap_content parameters
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            activeWindowTenCardSlice.forEach { variant ->
              val isSelected = activeLauncherIconId == variant.id
              val variantPrimary = Color(variant.primaryColorHex)
              val variantAccent = Color(variant.accentColorHex)

              DisposableEffect(variant.id) {
                onDispose {
                  // Clean resource references on page unmount
                }
              }

              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .wrapContentHeight()
                  .clip(RoundedCornerShape(8.dp))
                  .border(
                    if (isSelected) 1.5.dp else 0.5.dp,
                    if (isSelected) variantAccent else cardBorder.copy(alpha = 0.4f),
                    RoundedCornerShape(8.dp)
                  )
                  .clickable {
                    activateThemeItem(variant)
                  }
                  .testTag("design_option_${variant.id}"),
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) variantAccent.copy(alpha = 0.16f) else cardContainer.copy(alpha = 0.4f)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  // Color swatch preview
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(RoundedCornerShape(6.dp))
                      .background(variantPrimary)
                      .border(1.dp, variantAccent, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                  ) {
                    Box(
                      modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(variantAccent)
                    )
                  }

                  Column(
                    modifier = Modifier
                      .weight(1f)
                      .wrapContentHeight()
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                      Text(
                        text = variant.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = primaryTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = variantAccent.copy(alpha = 0.15f)
                      ) {
                        Text(
                          text = variant.category,
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            color = variantAccent,
                            fontWeight = FontWeight.Bold
                          ),
                          modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                        )
                      }
                    }

                    Text(
                      text = variant.subtitle,
                      style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor, fontSize = 10.sp),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )

                    // Compact alias token chip
                    Text(
                      text = variant.aliasClass.substringAfterLast("."),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.5.sp,
                        color = variantAccent,
                        fontFamily = FontFamily.Monospace
                      ),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }

                  RadioButton(
                    selected = isSelected,
                    onClick = {
                      activateThemeItem(variant)
                    },
                    colors = RadioButtonDefaults.colors(
                      selectedColor = variantAccent,
                      unselectedColor = secondaryTextColor.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.size(24.dp)
                  )
                }
              }
            }
          }

          // Strict Tracking Footer: "Page X of 10" (defaults to "Page 1 of 10")
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedButton(
              onClick = {
                if (safeCurrentPage > 0) {
                  HapticManager.navigationClick(context)
                  currentCatalogPage = safeCurrentPage - 1
                }
              },
              enabled = safeCurrentPage > 0,
              shape = RoundedCornerShape(6.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier.height(28.dp)
            ) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Page", modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(3.dp))
              Text("Previous", fontSize = 10.sp)
            }

            Text(
              text = "Page ${safeCurrentPage + 1} of $totalPages",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
              ),
              modifier = Modifier.testTag("catalog_pagination_footer_label")
            )

            OutlinedButton(
              onClick = {
                if (safeCurrentPage < totalPages - 1) {
                  HapticManager.navigationClick(context)
                  currentCatalogPage = safeCurrentPage + 1
                }
              },
              enabled = safeCurrentPage < totalPages - 1,
              shape = RoundedCornerShape(6.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier.height(28.dp)
            ) {
              Text("Next", fontSize = 10.sp)
              Spacer(modifier = Modifier.width(3.dp))
              Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Page", modifier = Modifier.size(12.dp))
            }
          }
        }
      }
    }
  }
}
