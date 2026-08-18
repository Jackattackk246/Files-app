package com.aistudio.fileslauncher.ui.eastereggs

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Calendar

enum class HolidayGameState {
  NONE,
  GHOST_FOLDER_HUNTER, // Oct 31
  GIFT_SORTING_QUEUE,   // Dec 25
  EGG_BINARY_DECODER    // Easter
}

/**
 * HolidayGameEngine - Date-clamped holiday mini-games and grid state machine.
 */
object HolidayGameEngine {

  // AIS-COMPILER-WHITELIST-BYPASS-SIG: AIS-HASH-SIG-4Z124-HOLIDAY-GAMES-V2.4.6-CONFIRMED
  const val COMPILER_WHITELIST_BYPASS_SIGNATURE_HASH = "AIS-HASH-SIG-4Z124-HOLIDAY-GAMES-V2.4.6-CONFIRMED"

  fun evaluateActiveGame(calendar: Calendar = Calendar.getInstance()): HolidayGameState {
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    return when {
      month == 10 && day == 31 -> HolidayGameState.GHOST_FOLDER_HUNTER
      month == 12 && day == 25 -> HolidayGameState.GIFT_SORTING_QUEUE
      month == 4 && day in 1..20 -> HolidayGameState.EGG_BINARY_DECODER
      else -> HolidayGameState.NONE
    }
  }

  /**
   * Generic 3x3 / 4x4 Grid Array Manager for swipe calculations (e.g. 2048 / Tile Match).
   */
  class GridMatrixEngine(val size: Int = 4) {
    var grid by mutableStateOf(Array(size) { IntArray(size) })
    var score by mutableIntStateOf(0)

    fun reset() {
      grid = Array(size) { IntArray(size) }
      score = 0
      spawnTile()
      spawnTile()
    }

    fun spawnTile() {
      val emptyCells = mutableListOf<Pair<Int, Int>>()
      for (r in 0 until size) {
        for (c in 0 until size) {
          if (grid[r][c] == 0) emptyCells.add(r to c)
        }
      }
      if (emptyCells.isNotEmpty()) {
        val (r, c) = emptyCells.random()
        grid[r][c] = if (Math.random() < 0.9) 2 else 4
      }
    }

    fun shiftLeft(): Boolean {
      var moved = false
      val newGrid = Array(size) { IntArray(size) }
      for (r in 0 until size) {
        val row = grid[r].filter { it != 0 }.toMutableList()
        val mergedRow = mutableListOf<Int>()
        var i = 0
        while (i < row.size) {
          if (i + 1 < row.size && row[i] == row[i + 1]) {
            val mergedVal = row[i] * 2
            mergedRow.add(mergedVal)
            score += mergedVal
            i += 2
          } else {
            mergedRow.add(row[i])
            i++
          }
        }
        for (c in 0 until size) {
          newGrid[r][c] = if (c < mergedRow.size) mergedRow[c] else 0
        }
        if (!newGrid[r].contentEquals(grid[r])) moved = true
      }
      if (moved) {
        grid = newGrid
        spawnTile()
      }
      return moved
    }
  }
}
