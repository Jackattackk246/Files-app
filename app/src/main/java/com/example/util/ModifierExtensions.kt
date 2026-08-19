package com.jackattackk246.files.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Reusable input isolation modifier that swallows all tap gestures, clicks, 
 * touch drags, and gestures to prevent background layout interaction.
 */
fun Modifier.isolateInputLayer(enabled: Boolean): Modifier {
  return if (enabled) {
    this.pointerInput(Unit) {
      awaitPointerEventScope {
        while (true) {
          val event = awaitPointerEvent(PointerEventPass.Initial)
          event.changes.forEach { it.consume() }
        }
      }
    }
  } else {
    this
  }
}
