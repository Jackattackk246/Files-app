package com.jackattackk246.files.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun TutorialOverlay(
  onDismiss: () -> Unit
) {
  var currentStep by remember { mutableStateOf(1) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.80f))
      .testTag("tutorial_overlay_container")
  ) {
    // Skip Button in upper right corner with generous padding
    TextButton(
      onClick = onDismiss,
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(top = 24.dp, end = 24.dp)
        .testTag("tutorial_skip_button")
    ) {
      Text(
        text = "Skip",
        color = Color(0xFF00E5FF),
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
      )
    }

    // Step Panel Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.Center)
        .padding(24.dp)
        .testTag("tutorial_step_card"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1D22)),
      border = BorderStroke(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.8f))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
      ) {
        // Step Indicator Icon/Header
        Box(
          modifier = Modifier
            .size(60.dp)
            .background(Color(0xFF00E5FF).copy(alpha = 0.15f), CircleShape)
            .border(1.5.dp, Color(0xFF00E5FF), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = when (currentStep) {
              1 -> Icons.Default.Storage
              2 -> Icons.Default.AspectRatio
              else -> Icons.Default.EditCalendar
            },
            contentDescription = null,
            tint = Color(0xFF00E5FF),
            modifier = Modifier.size(28.dp)
          )
        }

        // Title text
        Text(
          text = when (currentStep) {
            1 -> "1. Storage Telemetry Hub"
            2 -> "2. Windows Phone Style Modular Sizing"
            else -> "3. Interactive Edit Mode Customization"
          },
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
          )
        )

        // Description text
        Text(
          text = when (currentStep) {
            1 -> "Welcome to Files. Monitor your physical device capacities, paths, and block allocations seamlessly in real time."
            2 -> "Customized Viewports: Toggle your layout view profiles between Small, Medium, or Wide rows instantly to scale data density to your preference."
            else -> "Dynamic Layout Control: Activate Edit Mode to unlock the bottom '+' menu button, pin secondary cloud network hubs, or long-press and hold any storage card to drag and shuffle item orders on the fly."
          },
          style = MaterialTheme.typography.bodyMedium.copy(
            color = Color(0xFFA1A1AA),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
          ),
          modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Navigation indicators
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          for (i in 1..3) {
            Box(
              modifier = Modifier
                .size(if (i == currentStep) 10.dp else 8.dp)
                .background(
                  color = if (i == currentStep) Color(0xFF00E5FF) else Color(0xFF2C2D35),
                  shape = CircleShape
                )
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Action Buttons Row
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
                containerColor = Color(0xFF2C2D35),
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Back", fontWeight = FontWeight.Bold)
            }
          }

          Button(
            onClick = {
              if (currentStep < 3) {
                currentStep++
              } else {
                onDismiss()
              }
            },
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("tutorial_next_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF00E5FF),
              contentColor = Color.Black
            ),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(
              text = if (currentStep == 3) "Get Started" else "Next",
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
