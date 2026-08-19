package com.jackattackk246.files.ui.dialog

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jackattackk246.files.security.DeveloperSecurityEngine
import com.jackattackk246.files.util.HapticFeedbackHelper
import com.jackattackk246.files.util.isolateInputLayer

@Composable
fun DeveloperPasswordAuthDialog(
  onSuccess: () -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var passwordInput by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  val remainingCooldown = remember { DeveloperSecurityEngine.getRemainingCooldownMs(context) }
  val isBricked = remember { DeveloperSecurityEngine.isPortalBricked(context) }
  val isInfinity = remember { DeveloperSecurityEngine.isInfinitySanctionActive(context) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
      usePlatformDefaultWidth = true,
      dismissOnBackPress = false,
      dismissOnClickOutside = false
    )
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .isolateInputLayer(enabled = true)
        .testTag("dialog_developer_password_auth"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF141416)),
      border = BorderStroke(1.5.dp, Color(0xFF202224))
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .background(Color(0x1A00E5FF), RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = Color(0xFF00E5FF),
            modifier = Modifier.size(28.dp)
          )
        }

        Text(
          text = "Developer Authorization",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        )

        if (isInfinity) {
          Text(
            text = "♾️ Permanent Lockdown Active",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = Color(0xFF9CA3AF),
              fontWeight = FontWeight.Bold
            )
          )
          Text(
            text = "Failed to add infinity to infinity error code id10t",
            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF9CA3AF))
          )
        } else if (remainingCooldown > 0L) {
          val minutes = (remainingCooldown / (1000 * 60)).coerceAtLeast(1)
          Text(
            text = "Cooldown in Progress: $minutes min remaining",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = Color(0xFF9CA3AF),
              fontWeight = FontWeight.Bold
            )
          )
        } else if (isBricked) {
          Text(
            text = "Portal locked after 10 failed attempts.",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = Color(0xFF9CA3AF)
            )
          )
        } else {
          Text(
            text = "Enter repository pass-phrase to unlock developer diagnostics & time-warp utilities.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFD1D5DB))
          )

          OutlinedTextField(
            value = passwordInput,
            onValueChange = {
              passwordInput = it
              errorMessage = null
            },
            label = { Text("Passcode", color = Color(0xFF9CA3AF)) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("developer_passcode_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              focusedBorderColor = Color(0xFF00E5FF),
              unfocusedBorderColor = Color(0x6600E5FF)
            )
          )

          errorMessage?.let {
            Text(text = it, color = Color(0xFF00E5FF), fontSize = 12.sp)
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            TextButton(
              onClick = {
                onDismiss()
              },
              modifier = Modifier.weight(1f)
            ) {
              Text("Cancel", color = Color.White)
            }

            Button(
              onClick = {
                val ok = DeveloperSecurityEngine.verifyPasscode(context, passwordInput.trim())
                if (ok) {
                  HapticFeedbackHelper.performTransferSuccessFeedback(context)
                  Toast.makeText(context, "Developer Mode Authorized", Toast.LENGTH_SHORT).show()
                  onSuccess()
                } else {
                  HapticFeedbackHelper.performErrorFeedback(context)
                  val left = 10 - DeveloperSecurityEngine.getFailedAttempts(context)
                  errorMessage = if (left > 0) "Invalid passcode ($left tries left)" else "10 failed attempts. Portal locked."
                }
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00E5FF),
                contentColor = Color.Black
              ),
              modifier = Modifier
                .weight(1f)
                .testTag("submit_developer_passcode_button")
            ) {
              Text("Unlock", color = Color.Black, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
