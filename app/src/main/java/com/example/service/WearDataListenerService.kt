package com.example.service

import android.util.Log
import com.example.util.WearSyncManager
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

class WearDataListenerService : WearableListenerService() {

  companion object {
    private const val TAG = "WearDataListenerService"
  }

  override fun onDataChanged(dataEvents: DataEventBuffer) {
    super.onDataChanged(dataEvents)
    for (event in dataEvents) {
      if (event.dataItem.uri.path == WearSyncManager.PATH_WORKSPACE_SYNC) {
        Log.d(TAG, "Workspace sync profile data changed on Wear OS node: ${event.dataItem.uri}")
      }
    }
  }

  override fun onMessageReceived(messageEvent: MessageEvent) {
    super.onMessageReceived(messageEvent)
    Log.d(TAG, "Message received on path: ${messageEvent.path}")

    if (messageEvent.path == WearSyncManager.PATH_REQUEST_WATCH_DIR) {
      val requestedPath = String(messageEvent.data, Charsets.UTF_8).ifEmpty { "/watch/root/storage" }
      Log.i(TAG, "Watch storage directory requested: $requestedPath")

      // Sample watch directory using java.io.File API / Watch sampler and reply
      val jsonResponse = WearSyncManager.buildWatchDirectoryJson(requestedPath)
      val responseBytes = jsonResponse.toByteArray(Charsets.UTF_8)

      val senderNodeId = messageEvent.sourceNodeId
      Wearable.getMessageClient(this)
        .sendMessage(senderNodeId, WearSyncManager.PATH_RETURN_WATCH_DIR, responseBytes)
        .addOnSuccessListener {
          Log.d(TAG, "Successfully returned watch directory JSON tree back to node $senderNodeId")
        }
        .addOnFailureListener { e ->
          Log.e(TAG, "Failed to send watch directory JSON tree back to node $senderNodeId", e)
        }
    }
  }
}
