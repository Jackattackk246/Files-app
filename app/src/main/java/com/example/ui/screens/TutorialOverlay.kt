package com.jackattackk246.files.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jackattackk246.files.ui.theme.AppThemeMode
import com.jackattackk246.files.ui.theme.ThemeManager
import com.jackattackk246.files.util.DashboardPreferences
import com.jackattackk246.files.util.isolateInputLayer

@Composable
fun TutorialOverlay(
  themeMode: AppThemeMode,
  customAccentColor: Color? = null,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var currentStep by remember { mutableStateOf(1) }

  val accentColor = ThemeManager.getThemeAccentColor(themeMode, customAccentColor)
  val primaryTextColor = ThemeManager.getAdaptivePrimaryTextColor(themeMode)
  val secondaryTextColor = ThemeManager.getAdaptiveSecondaryTextColor(themeMode)
  val cardContainer = ThemeManager.getAdaptiveCardContainerColor(themeMode)
  val cardBorder = ThemeManager.getAdaptiveCardBorderColor(themeMode)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .testTag("tutorial_overlay_container")
  ) {
    // 1. Full screen background scrim layer with strict pointer input isolation
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.85f))
        .isolateInputLayer(enabled = true)
        .testTag("tutorial_isolated_scrim")
    )

    // 2. Interactive Foreground Skip Button (with generous target size)
    TextButton(
      onClick = {
        DashboardPreferences.setFirstLaunchTutorialEnabled(context, false)
        onDismiss()
      },
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(top = 28.dp, end = 28.dp)
        .testTag("tutorial_skip_button")
    ) {
      Text(
        text = "Skip Tour",
        color = accentColor,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
      )
    }

    // 3. Step Walkthrough Onboarding Card (interactive elements remain fully active)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.Center)
        .padding(24.dp)
        .testTag("tutorial_step_card"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = cardContainer),
      border = BorderStroke(1.5.dp, cardBorder)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
      ) {
        // Step Indicator Icon Container
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.15f))
            .border(1.5.dp, accentColor, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = when (currentStep) {
              1 -> Icons.Default.FolderZip
              2 -> Icons.Default.DeleteSweep
              3 -> Icons.Default.SettingsInputAntenna
              else -> Icons.Default.Widgets
            },
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(28.dp)
          )
        }

        // Title text block
        Text(
          text = when (currentStep) {
            1 -> "1. Secure File Explorer"
            2 -> "Recycle Bin"
            3 -> "Nearby Devices"
            else -> "4. Core System Utilities"
          },
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = primaryTextColor,
            textAlign = TextAlign.Center
          )
        )

        // Subtitle text block
        Text(
          text = when (currentStep) {
            1 -> "Secure local workspace sandboxing"
            2 -> "Safe offline deletion & recovery"
            3 -> "Direct high-speed media discovery"
            else -> "Offline high-performance toolbox"
          },
          style = MaterialTheme.typography.bodySmall.copy(
            color = secondaryTextColor,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
          )
        )

        // Description text block
        Text(
          text = when (currentStep) {
            1 -> "A full-featured local workspace with secure internal partitioning. Create, organize, read, write, and manage folders and files entirely offline."
            2 -> "Recover accidental deletions or permanently empty your storage. Keeps your files safely isolated until you choose to restore or clear them."
            3 -> "Discovery portal for local device connections. High-speed, hardware-isolated media synchronization and wireless fast transfers."
            else -> "Supercharge storage with five utilities: dynamic seasons, customizable wallpaper canvases, archive zipping/extraction, native media views, and storage analytics."
          },
          style = MaterialTheme.typography.bodyMedium.copy(
            color = secondaryTextColor,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
          ),
          modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Dynamic Dot Indicator Row
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          for (i in 1..4) {
            Box(
              modifier = Modifier
                .size(if (i == currentStep) 10.dp else 7.dp)
                .clip(CircleShape)
                .background(
                  color = if (i == currentStep) accentColor else secondaryTextColor.copy(alpha = 0.35f)
                )
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Navigation actions Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          if (currentStep > 1) {
            Button(
              onClick = { currentStep-- },
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("tutorial_prev_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = cardBorder,
                contentColor = primaryTextColor
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text("Back", fontWeight = FontWeight.Bold)
            }
          }

          Button(
            onClick = {
              if (currentStep < 4) {
                currentStep++
              } else {
                DashboardPreferences.setFirstLaunchTutorialEnabled(context, false)
                onDismiss()
              }
            },
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("tutorial_next_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = accentColor,
              contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = if (currentStep == 4) "Get Started" else "Next",
              fontWeight = FontWeight.Bold,
              color = Color.Black
            )
          }
        }
      }
    }
  }
}
