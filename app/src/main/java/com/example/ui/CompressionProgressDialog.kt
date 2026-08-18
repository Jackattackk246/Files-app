package com.jackattackk246.files.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CompressionProgressDialog(
  title: String,
  progressRatio: Float,
  statusMessage: String,
  onCancel: (() -> Unit)? = null
) {
  AlertDialog(
    onDismissRequest = { /* Prevent dismiss while processing */ },
    shape = RoundedCornerShape(20.dp),
    title = {
      Text(title, fontWeight = FontWeight.Bold)
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(statusMessage, style = MaterialTheme.typography.bodyMedium, maxLines = 2)

        LinearProgressIndicator(
          progress = { progressRatio.coerceIn(0f, 1f) },
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp),
          color = MaterialTheme.colorScheme.primary,
          trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          Text(
            text = "${(progressRatio * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              fontSize = 18.sp
            )
          )
        }
      }
    },
    confirmButton = {
      if (onCancel != null) {
        TextButton(onClick = onCancel) {
          Text("Cancel")
        }
      }
    }
  )
}
