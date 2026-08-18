package com.jackattackk246.files.ui.dialog

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aistudio.fileslauncher.ui.eastereggs.HolidayGameState
import com.jackattackk246.files.util.HapticFeedbackHelper

data class MiniGameItem(
  val id: String,
  val title: String,
  val season: String,
  val description: String,
  val gameState: HolidayGameState
)

@Composable
fun OutOSeasonGameLauncherDialog(
  onDismiss: () -> Unit,
  onLaunchGame: (HolidayGameState) -> Unit
) {
  val context = LocalContext.current
  val games = listOf(
    MiniGameItem("ghost", "Ghost Folder Hunter", "Halloween (Oct 31)", "Sweep hidden spooky orphaned cache files across directory sectors.", HolidayGameState.GHOST_FOLDER_HUNTER),
    MiniGameItem("gift", "Gift Sorting Queue", "Christmas (Dec 25)", "Organize inbound payload files into encrypted archive partitions.", HolidayGameState.GIFT_SORTING_QUEUE),
    MiniGameItem("egg", "Egg Binary Decoder", "Spring Easter (Apr 1-20)", "Decode hexadecimal archive headers under time-warp pressure.", HolidayGameState.EGG_BINARY_DECODER),
    MiniGameItem("tetris", "Christmas Tetris Arcade", "All-Season Custom", "Stack descending block packages onto retro matrix buffers.", HolidayGameState.GIFT_SORTING_QUEUE)
  )

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 500.dp)
        .padding(16.dp)
        .testTag("dialog_out_of_season_games"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF141416)),
      border = BorderStroke(1.5.dp, Color(0xFF8B5CF6))
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .background(Color(0x338B5CF6), RoundedCornerShape(10.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
            }
            Text(
              text = "Out-of-Season Games Launchpad",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
          }
        }

        Text(
          text = "Bypass system date locks and instantly launch standalone seasonal mini-game containers.",
          style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
        )

        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(games) { game ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  HapticFeedbackHelper.performTransferSuccessFeedback(context)
                  Toast.makeText(context, "Launching: ${game.title} (Bypassing Season Lock)", Toast.LENGTH_SHORT).show()
                  onLaunchGame(game.gameState)
                  onDismiss()
                },
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
              border = BorderStroke(1.dp, Color(0x338B5CF6))
            ) {
              Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(game.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0x338B5CF6)
                  ) {
                    Text(
                      text = game.season,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                      color = Color(0xFFC084FC),
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Medium
                    )
                  }
                }
                Text(game.description, color = Color(0xFF9CA3AF), fontSize = 12.sp)
              }
            }
          }
        }
      }
    }
  }
}
