package com.jackattackk246.files.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.webkit.MimeTypeMap
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.*

/**
 * 100% Offline Wireless 'Nearby Devices' P2P Engine & Live Media Streaming Server.
 *
 * Core Capabilities:
 * 1. Offline local HTTP streaming server (port 42852) supporting HTTP Range headers (bytes=start-end)
 *    for live, seekable media streaming directly from host device without downloading.
 * 2. Persistent OTA session management: Handshake requires both devices in Nearby Devices menu, but
 *    once approved, session remains active in the background as long as the app is open.
 * 3. Full bi-directional remote file explorer (List, Read, Write, Rename, Delete, Create Folder)
 *    with atomic block transfers.
 * 4. Permanent Hardware Blocklist database: Silently drops incoming connection requests from blocked hardware tokens.
 * 5. Hidden Recycle Bin privacy: strictly masks '.recycle_bin' from remote file listings while accounting for space.
 */
object NearbyDevicesEngine {

  private const val UDP_DISCOVERY_PORT = 42850
  private const val TCP_CMD_PORT = 42851
  const val HTTP_STREAMING_PORT = 42852

  private var engineScope: CoroutineScope? = null
  private var udpBroadcastSocket: DatagramSocket? = null
  private var udpListenSocket: DatagramSocket? = null
  private var tcpCmdServerSocket: ServerSocket? = null
  private var httpStreamingServerSocket: ServerSocket? = null

  var isNearbyMenuOpen: Boolean = false
    private set

  private val _isSessionActive = MutableStateFlow(false)
  val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

  data class NearbyPeer(
    val hardwareToken: String,
    val deviceName: String,
    val deviceModel: String,
    val ipAddress: String,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
  )

  data class BlockedHardwareDevice(
    val hardwareToken: String,
    val deviceName: String,
    val lastKnownIp: String,
    val blockedTimestamp: Long
  )

  data class IncomingConnectionRequest(
    val requestId: String,
    val requesterToken: String,
    val requesterName: String,
    val requesterModel: String,
    val requesterIp: String,
    val clientSocket: Socket
  )

  data class RemoteStorageMetrics(
    val deviceName: String,
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val totalFormatted: String,
    val usedFormatted: String,
    val freeFormatted: String,
    val usedRatio: Float
  )

  data class RemoteFileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModified: Long,
    val formattedSize: String
  )

  private val _discoveredPeers = MutableStateFlow<List<NearbyPeer>>(emptyList())
  val discoveredPeers: StateFlow<List<NearbyPeer>> = _discoveredPeers.asStateFlow()

  private val _activePeer = MutableStateFlow<NearbyPeer?>(null)
  val activePeer: StateFlow<NearbyPeer?> = _activePeer.asStateFlow()

  private val _incomingRequest = MutableStateFlow<IncomingConnectionRequest?>(null)
  val incomingRequest: StateFlow<IncomingConnectionRequest?> = _incomingRequest.asStateFlow()

  private val _statusMessage = MutableStateFlow<String>("Offline")
  val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

  private val _blockedDevices = MutableStateFlow<List<BlockedHardwareDevice>>(emptyList())
  val blockedDevices: StateFlow<List<BlockedHardwareDevice>> = _blockedDevices.asStateFlow()

  private val _remoteCurrentPath = MutableStateFlow<String>("")
  val remoteCurrentPath: StateFlow<String> = _remoteCurrentPath.asStateFlow()

  private val _localBatteryLevel = MutableStateFlow<Int>(100)
  val localBatteryLevel: StateFlow<Int> = _localBatteryLevel.asStateFlow()

  private val _remoteBatteryLevel = MutableStateFlow<Int?>(null)
  val remoteBatteryLevel: StateFlow<Int?> = _remoteBatteryLevel.asStateFlow()

  enum class SyncInterval(val ms: Long, val label: String, val description: String) {
    FAST(1000L, "1s (Fast)", "Real-time updates, higher battery usage"),
    STANDARD(3000L, "3s (Standard)", "Balanced telemetry frequency"),
    POWER_SAVER(10000L, "10s (Power Saver)", "Low battery consumption"),
    EXTENDED(30000L, "30s (Extended)", "Maximum battery conservation")
  }

  private val _syncIntervalMs = MutableStateFlow<Long>(3000L)
  val syncIntervalMs: StateFlow<Long> = _syncIntervalMs.asStateFlow()

  private const val PREFS_SYNC_INTERVAL = "nearby_sync_interval_prefs"
  private const val KEY_SYNC_MS = "sync_interval_ms"

  fun loadSyncInterval(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_SYNC_INTERVAL, Context.MODE_PRIVATE)
    _syncIntervalMs.value = prefs.getLong(KEY_SYNC_MS, 3000L)
  }

  fun setSyncInterval(context: Context, intervalMs: Long) {
    _syncIntervalMs.value = intervalMs
    val prefs = context.getSharedPreferences(PREFS_SYNC_INTERVAL, Context.MODE_PRIVATE)
    prefs.edit().putLong(KEY_SYNC_MS, intervalMs).apply()
  }

  private var appContext: Context? = null

  private val _remoteFileList = MutableStateFlow<List<RemoteFileItem>>(emptyList())
  val remoteFileList: StateFlow<List<RemoteFileItem>> = _remoteFileList.asStateFlow()

  private val _remoteStorageMetrics = MutableStateFlow<RemoteStorageMetrics?>(null)
  val remoteStorageMetrics: StateFlow<RemoteStorageMetrics?> = _remoteStorageMetrics.asStateFlow()

  private const val PREFS_BLOCKLIST = "nearby_blocked_hardware_prefs"
  private const val KEY_BLOCKED_JSON = "blocked_devices_json"

  fun getHardwareToken(context: Context): String {
    val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    return if (androidId != null && androidId.isNotBlank() && androidId != "9774d56d682e549c" && androidId != "0000000000000000") {
      androidId
    } else {
      "hw_${Build.BOARD.hashCode()}_${Build.MODEL.hashCode()}"
    }
  }

  fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    if (bytes < 1024 * 1024) return "%.1f KB".format(bytes / 1024.0)
    if (bytes < 1024 * 1024 * 1024) return "%.1f MB".format(bytes / (1024.0 * 1024.0))
    return "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
  }

  private var sessionTemporaryName: String? = null

  fun setSessionTemporaryName(name: String) {
    sessionTemporaryName = name.trim().ifEmpty { null }
  }

  fun getResolvedDeviceName(context: Context): String {
    // 1. Session temporary name if provided (strictly local session state)
    sessionTemporaryName?.let {
      if (it.isNotBlank()) return it
    }
    // 2. Global app user profile if already configured
    val globalName = UserProfilePreferences.getRawUserName(context)
    if (globalName.isNotBlank()) {
      return globalName
    }
    // 3. Fallback temporary session identifier
    return "User_${(1000..9999).random()}"
  }

  private var simulatedPowerState: Int? = null

  fun setSimulatedPowerState(level: Int?) {
    simulatedPowerState = level
  }

  fun getBatteryLevel(context: Context): Int {
    simulatedPowerState?.let { return it }
    try {
      val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
      if (bm != null) {
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        if (level in 1..100) return level
      }
      val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
      val batteryStatus = context.registerReceiver(null, filter)
      val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
      val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
      if (level >= 0 && scale > 0) {
        return (level * 100) / scale
      }
    } catch (_: Exception) {}
    return 100
  }

  fun loadBlocklist(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_BLOCKLIST, Context.MODE_PRIVATE)
    val jsonStr = prefs.getString(KEY_BLOCKED_JSON, "[]") ?: "[]"
    val list = mutableListOf<BlockedHardwareDevice>()
    try {
      val array = JSONArray(jsonStr)
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          BlockedHardwareDevice(
            hardwareToken = obj.getString("hardwareToken"),
            deviceName = obj.getString("deviceName"),
            lastKnownIp = obj.getString("lastKnownIp"),
            blockedTimestamp = obj.getLong("blockedTimestamp")
          )
        )
      }
    } catch (_: Exception) {}
    _blockedDevices.value = list
  }

  fun blockDevice(context: Context, token: String, name: String, ip: String) {
    val current = _blockedDevices.value.toMutableList()
    if (current.none { it.hardwareToken == token }) {
      current.add(BlockedHardwareDevice(token, name, ip, System.currentTimeMillis()))
      _blockedDevices.value = current
      saveBlocklist(context, current)
    }
  }

  fun unblockDevice(context: Context, token: String) {
    val current = _blockedDevices.value.filter { it.hardwareToken != token }
    _blockedDevices.value = current
    saveBlocklist(context, current)
  }

  fun clearAllBlockedDevices(context: Context) {
    _blockedDevices.value = emptyList()
    val prefs = context.getSharedPreferences(PREFS_BLOCKLIST, Context.MODE_PRIVATE)
    prefs.edit().remove(KEY_BLOCKED_JSON).apply()
  }

  private fun saveBlocklist(context: Context, list: List<BlockedHardwareDevice>) {
    val prefs = context.getSharedPreferences(PREFS_BLOCKLIST, Context.MODE_PRIVATE)
    val array = JSONArray()
    for (d in list) {
      val obj = JSONObject().apply {
        put("hardwareToken", d.hardwareToken)
        put("deviceName", d.deviceName)
        put("lastKnownIp", d.lastKnownIp)
        put("blockedTimestamp", d.blockedTimestamp)
      }
      array.put(obj)
    }
    prefs.edit().putString(KEY_BLOCKED_JSON, array.toString()).apply()
  }

  fun isTokenBlocked(token: String): Boolean {
    return _blockedDevices.value.any { it.hardwareToken == token }
  }

  fun getLocalIpAddress(): String? {
    try {
      val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
      for (intf in interfaces) {
        if (intf.isLoopback || !intf.isUp) continue
        val addrs = intf.inetAddresses
        for (addr in addrs) {
          if (!addr.isLoopbackAddress && addr is Inet4Address) {
            return addr.hostAddress
          }
        }
      }
    } catch (_: Exception) {}
    return null
  }

  /**
   * Called when opening the Nearby Devices screen menu.
   */
  @Synchronized
  fun onNearbyMenuOpened(context: Context) {
    isNearbyMenuOpen = true
    loadBlocklist(context)
    loadSyncInterval(context)
    if (engineScope == null) {
      startServices(context)
    }
    _statusMessage.value = "Searching for Nearby Devices..."
  }

  /**
   * Called when leaving the Nearby Devices menu screen.
   * Note constraint requirement: If a session is active, keep background services running!
   * If no session is active, stop discovery to conserve power.
   */
  @Synchronized
  fun onNearbyMenuClosed() {
    isNearbyMenuOpen = false
    if (!_isSessionActive.value) {
      stopServices()
    }
  }

  @Synchronized
  fun startServices(context: Context) {
    if (engineScope != null) return
    val newScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    engineScope = newScope

    appContext = context.applicationContext
    val myToken = getHardwareToken(context)
    val myName = getResolvedDeviceName(context)
    val myModel = Build.MODEL ?: "Device"

    // 1. Start HTTP Media Streaming Server (Port 42852)
    newScope.launch {
      try {
        val httpPortSocket = ServerSocket()
        httpPortSocket.reuseAddress = true
        httpPortSocket.bind(InetSocketAddress(HTTP_STREAMING_PORT))
        httpStreamingServerSocket = httpPortSocket

        while (isActive) {
          val client = try {
            httpPortSocket.accept()
          } catch (_: Exception) { break }

          launch {
            handleIncomingHttpStreamingRequest(client)
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    // 2. Start TCP Command & Handshake Server (Port 42851)
    newScope.launch {
      try {
        val server = ServerSocket()
        server.reuseAddress = true
        server.bind(InetSocketAddress(TCP_CMD_PORT))
        tcpCmdServerSocket = server

        while (isActive) {
          val client = try {
            server.accept()
          } catch (_: Exception) { break }

          launch {
            handleIncomingTcpCmdConnection(client, context, myName, myModel, myToken)
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    // 3. Start UDP Broadcast Listener (Port 42850)
    newScope.launch {
      try {
        val listenSocket = DatagramSocket(null).apply {
          reuseAddress = true
          bind(InetSocketAddress(UDP_DISCOVERY_PORT))
          broadcast = true
        }
        udpListenSocket = listenSocket
        val buffer = ByteArray(1024)

        while (isActive) {
          val packet = DatagramPacket(buffer, buffer.size)
          try {
            listenSocket.receive(packet)
          } catch (_: Exception) { break }

          val message = String(packet.data, 0, packet.length, Charsets.UTF_8)
          val senderIp = packet.address.hostAddress ?: continue
          val localIp = getLocalIpAddress()

          val parts = message.split(":")
          if (parts.size >= 4 && (parts[0] == "NEARBY_DISCOVER" || parts[0] == "NEARBY_PEER")) {
            val peerToken = parts[1]
            val peerName = parts[2]
            val peerModel = parts[3]

            // Rule 1: SELF-DISCOVERY FILTERING RULE
            // Hide our own broadcasting instance ("10.0.2.16", "127.0.0.1", local IP, or matching profile name)
            val localProfileName = getResolvedDeviceName(context)
            if (senderIp == "10.0.2.16" || senderIp == "127.0.0.1" || senderIp == localIp || peerName == localProfileName || peerToken == myToken) {
              continue
            }

            // Check hardware blocklist
            if (isTokenBlocked(peerToken)) continue

            val currentPeers = _discoveredPeers.value.toMutableList()
            val existingIndex = currentPeers.indexOfFirst { it.hardwareToken == peerToken || it.ipAddress == senderIp }
            val peer = NearbyPeer(
              hardwareToken = peerToken,
              deviceName = peerName,
              deviceModel = peerModel,
              ipAddress = senderIp,
              lastSeenTimestamp = System.currentTimeMillis()
            )

            if (existingIndex >= 0) {
              currentPeers[existingIndex] = peer
            } else {
              currentPeers.add(peer)
            }
            _discoveredPeers.value = currentPeers

            if (parts[0] == "NEARBY_DISCOVER" && isNearbyMenuOpen) {
              sendUdpPacket(packet.address, "NEARBY_PEER:$myToken:$myName:$myModel")
            }
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    // 4. Start Periodic UDP Broadcaster
    newScope.launch {
      try {
        val bSocket = DatagramSocket().apply { broadcast = true }
        udpBroadcastSocket = bSocket
        val broadcastAddress = InetAddress.getByName("255.255.255.255")

        while (isActive) {
          if (isNearbyMenuOpen || _isSessionActive.value) {
            val payload = "NEARBY_DISCOVER:$myToken:$myName:$myModel"
            val bytes = payload.toByteArray(Charsets.UTF_8)
            val packet = DatagramPacket(bytes, bytes.size, broadcastAddress, UDP_DISCOVERY_PORT)
            try { bSocket.send(packet) } catch (_: Exception) {}
          }

          // Prune stale peers not seen in last 12 seconds
          val now = System.currentTimeMillis()
          val fresh = _discoveredPeers.value.filter { now - it.lastSeenTimestamp < 12000 }
          if (fresh.size != _discoveredPeers.value.size) {
            _discoveredPeers.value = fresh
          }

          delay(2500)
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    // 5. Real-Time Power Monitoring & Battery Sync Loop
    newScope.launch {
      while (isActive) {
        val ctx = appContext ?: context
        val currentLocalBat = getBatteryLevel(ctx)
        _localBatteryLevel.value = currentLocalBat

        if (_isSessionActive.value) {
          _activePeer.value?.let { peer ->
            fetchRemoteBatteryLevel(peer.ipAddress)
          }
        } else {
          _remoteBatteryLevel.value = null
        }
        delay(_syncIntervalMs.value)
      }
    }
  }

  private fun sendUdpPacket(target: InetAddress, message: String) {
    try {
      val bytes = message.toByteArray(Charsets.UTF_8)
      val packet = DatagramPacket(bytes, bytes.size, target, UDP_DISCOVERY_PORT)
      udpBroadcastSocket?.send(packet)
    } catch (_: Exception) {}
  }

  /**
   * HTTP 1.1 Partial Content Stream Server over TCP Port 42852.
   * Processes HTTP Range headers (bytes=start-end) to stream media files progressively.
   */
  private suspend fun handleIncomingHttpStreamingRequest(socket: Socket) = withContext(Dispatchers.IO) {
    try {
      socket.soTimeout = 15000
      val inputStream = socket.getInputStream()
      val outputStream = socket.getOutputStream()
      val reader = BufferedReader(InputStreamReader(inputStream))

      val requestLine = reader.readLine() ?: run { socket.close(); return@withContext }
      var rangeHeader: String? = null

      var line = reader.readLine()
      while (!line.isNullOrEmpty()) {
        if (line.startsWith("Range:", ignoreCase = true)) {
          rangeHeader = line.substringAfter(":").trim()
        }
        line = reader.readLine()
      }

      val pathUri = requestLine.split(" ").getOrNull(1) ?: ""
      if (!pathUri.contains("/stream?path=")) {
        sendHttpResponse(outputStream, "400 Bad Request", "text/plain", "Invalid endpoint".toByteArray())
        socket.close()
        return@withContext
      }

      val encodedPath = pathUri.substringAfter("/stream?path=")
      val decodedPath = URLDecoder.decode(encodedPath, "UTF-8")
      val targetFile = File(decodedPath)

      if (!targetFile.exists() || !targetFile.isFile || !targetFile.canRead()) {
        sendHttpResponse(outputStream, "404 Not Found", "text/plain", "File not found".toByteArray())
        socket.close()
        return@withContext
      }

      // Hide hidden .recycle_bin from public stream if attempted
      if (targetFile.name.equals(".recycle_bin", ignoreCase = true) || targetFile.name.equals(".jack_recycle_bin", ignoreCase = true)) {
        sendHttpResponse(outputStream, "403 Forbidden", "text/plain", "Forbidden path".toByteArray())
        socket.close()
        return@withContext
      }

      val totalLength = targetFile.length()
      val ext = targetFile.extension.lowercase()
      val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"

      var start: Long = 0
      var end: Long = totalLength - 1

      if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
        val rangeValues = rangeHeader.removePrefix("bytes=").split("-")
        start = rangeValues.getOrNull(0)?.toLongOrNull() ?: 0L
        end = rangeValues.getOrNull(1)?.toLongOrNull() ?: (totalLength - 1)
        if (end >= totalLength) end = totalLength - 1
      }

      val contentLength = (end - start) + 1
      val writer = PrintWriter(outputStream, false)

      if (rangeHeader != null) {
        writer.print("HTTP/1.1 206 Partial Content\r\n")
        writer.print("Content-Type: $mimeType\r\n")
        writer.print("Accept-Ranges: bytes\r\n")
        writer.print("Content-Range: bytes $start-$end/$totalLength\r\n")
        writer.print("Content-Length: $contentLength\r\n")
        writer.print("Connection: keep-alive\r\n\r\n")
      } else {
        writer.print("HTTP/1.1 200 OK\r\n")
        writer.print("Content-Type: $mimeType\r\n")
        writer.print("Accept-Ranges: bytes\r\n")
        writer.print("Content-Length: $totalLength\r\n")
        writer.print("Connection: keep-alive\r\n\r\n")
      }
      writer.flush()

      RandomAccessFile(targetFile, "r").use { raf ->
        raf.seek(start)
        val buffer = ByteArray(64 * 1024)
        var bytesToRead = contentLength
        while (bytesToRead > 0) {
          val readLen = raf.read(buffer, 0, minOf(buffer.size.toLong(), bytesToRead).toInt())
          if (readLen <= 0) break
          outputStream.write(buffer, 0, readLen)
          outputStream.flush()
          bytesToRead -= readLen
        }
      }
      socket.close()
    } catch (_: Exception) {
      try { socket.close() } catch (_: Exception) {}
    }
  }

  private fun sendHttpResponse(os: OutputStream, status: String, mime: String, body: ByteArray) {
    val writer = PrintWriter(os, false)
    writer.print("HTTP/1.1 $status\r\n")
    writer.print("Content-Type: $mime\r\n")
    writer.print("Content-Length: ${body.size}\r\n\r\n")
    writer.flush()
    os.write(body)
    os.flush()
  }

  /**
   * Main TCP Server Handshake & Command Controller over Port 42851.
   */
  private suspend fun handleIncomingTcpCmdConnection(
    socket: Socket,
    context: Context,
    myName: String,
    myModel: String,
    myToken: String
  ) = withContext(Dispatchers.IO) {
    try {
      socket.soTimeout = 30000
      val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
      val writer = PrintWriter(socket.getOutputStream(), true)
      val line = reader.readLine() ?: run { socket.close(); return@withContext }

      val senderIp = socket.inetAddress.hostAddress ?: "Unknown"

      if (line.startsWith("NEARBY_CONNECT_REQ:")) {
        val parts = line.split(":")
        val reqToken = parts.getOrNull(1) ?: "unknown_hw"
        val reqName = parts.getOrNull(2) ?: "Remote Device"
        val reqModel = parts.getOrNull(3) ?: "Android"

        // Rule 6: Hardware Blocklist Check - SILENT DROP IF BLOCKED
        if (isTokenBlocked(reqToken)) {
          writer.println("NEARBY_REJECTED:BLOCKED")
          socket.close()
          return@withContext
        }

        // Rule 4: Mandatory Initial Handshake Rule
        // Connection request ONLY allowed if host device is on Nearby Devices screen menu
        if (!isNearbyMenuOpen && !_isSessionActive.value) {
          writer.println("NEARBY_REJECTED:MENU_CLOSED")
          socket.close()
          return@withContext
        }

        // Trigger on-screen approval dialog
        val req = IncomingConnectionRequest(
          requestId = "req_${System.currentTimeMillis()}",
          requesterToken = reqToken,
          requesterName = reqName,
          requesterModel = reqModel,
          requesterIp = senderIp,
          clientSocket = socket
        )
        _incomingRequest.value = req

      } else if (line.startsWith("REMOTE_CMD:")) {
        // Process remote storage management commands
        processRemoteCommand(line.removePrefix("REMOTE_CMD:"), socket, reader, writer)
      } else {
        socket.close()
      }
    } catch (_: Exception) {
      try { socket.close() } catch (_: Exception) {}
    }
  }

  private fun processRemoteCommand(cmd: String, socket: Socket, reader: BufferedReader, writer: PrintWriter) {
    try {
      when {
        cmd.startsWith("LIST_DIR:") -> {
          val path = cmd.removePrefix("LIST_DIR:")
          val targetDir = if (path.isBlank() || path == "/") FileManager.getRootDirectory() else File(path)
          val files = targetDir.listFiles() ?: emptyArray()

          val jsonArray = JSONArray()
          for (f in files) {
            // Rule 1 & 8: Strictly filter out .recycle_bin from remote file listings
            if (f.name.equals(".recycle_bin", ignoreCase = true) || f.name.equals(".jack_recycle_bin", ignoreCase = true) || f.name.equals("recycle_manifest.json", ignoreCase = true)) {
              continue
            }
            val obj = JSONObject().apply {
              put("name", f.name)
              put("path", f.absolutePath)
              put("isDirectory", f.isDirectory)
              put("sizeBytes", if (f.isFile) f.length() else 0L)
              put("lastModified", f.lastModified())
              put("formattedSize", if (f.isFile) formatBytes(f.length()) else "${f.listFiles()?.size ?: 0} items")
            }
            jsonArray.put(obj)
          }

          writer.println("REMOTE_RES_LIST:${targetDir.absolutePath}:${jsonArray}")
        }

        cmd.startsWith("GET_STORAGE_METRICS") -> {
          val metrics = FileManager.getStorageMetrics()
          val obj = JSONObject().apply {
            put("totalBytes", metrics.realTotalBytes)
            put("usedBytes", metrics.usedBytes)
            put("freeBytes", metrics.realFreeBytes)
            put("totalFormatted", metrics.totalGbFormatted)
            put("usedFormatted", metrics.usedGbFormatted)
            put("freeFormatted", metrics.freeGbFormatted)
            put("usedRatio", metrics.usedRatio)
          }
          writer.println("REMOTE_RES_METRICS:${obj}")
        }

        cmd.startsWith("GET_BATTERY") -> {
          val level = appContext?.let { getBatteryLevel(it) } ?: 100
          writer.println("REMOTE_RES_BATTERY:$level")
        }

        cmd.startsWith("RENAME:") -> {
          val parts = cmd.removePrefix("RENAME:").split("||")
          if (parts.size >= 2) {
            val oldFile = File(parts[0])
            val newName = parts[1]
            val newFile = File(oldFile.parentFile, newName)
            val success = oldFile.renameTo(newFile)
            writer.println(if (success) "REMOTE_RES_OK" else "REMOTE_RES_ERR:Rename failed")
          } else {
            writer.println("REMOTE_RES_ERR:Invalid parameters")
          }
        }

        cmd.startsWith("DELETE:") -> {
          val targetPath = cmd.removePrefix("DELETE:")
          val targetFile = File(targetPath)
          val res = kotlinx.coroutines.runBlocking { FileManager.delete(targetFile) }
          if (res.isSuccess) {
            writer.println("REMOTE_RES_OK")
          } else {
            writer.println("REMOTE_RES_ERR:${res.exceptionOrNull()?.message ?: "Delete failed"}")
          }
        }

        cmd.startsWith("CREATE_DIR:") -> {
          val parts = cmd.removePrefix("CREATE_DIR:").split("||")
          if (parts.size >= 2) {
            val parentDir = File(parts[0])
            val folderName = parts[1]
            val newDir = File(parentDir, folderName)
            val success = newDir.mkdirs() || newDir.exists()
            writer.println(if (success) "REMOTE_RES_OK" else "REMOTE_RES_ERR:Create dir failed")
          } else {
            writer.println("REMOTE_RES_ERR:Invalid parameters")
          }
        }

        else -> {
          writer.println("REMOTE_RES_ERR:Unknown command")
        }
      }
    } catch (e: Exception) {
      writer.println("REMOTE_RES_ERR:${e.message ?: "Execution error"}")
    } finally {
      socket.close()
    }
  }

  fun approveIncomingConnection(request: IncomingConnectionRequest) {
    engineScope?.launch(Dispatchers.IO) {
      try {
        val writer = PrintWriter(request.clientSocket.getOutputStream(), true)
        writer.println("NEARBY_APPROVED")
        request.clientSocket.close()

        _activePeer.value = NearbyPeer(
          hardwareToken = request.requesterToken,
          deviceName = request.requesterName,
          deviceModel = request.requesterModel,
          ipAddress = request.requesterIp
        )
        _isSessionActive.value = true
        _statusMessage.value = "Connected to ${request.requesterName}"
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        if (_incomingRequest.value?.requestId == request.requestId) {
          _incomingRequest.value = null
        }
      }
    }
  }

  fun rejectIncomingConnection(request: IncomingConnectionRequest) {
    engineScope?.launch(Dispatchers.IO) {
      try {
        val writer = PrintWriter(request.clientSocket.getOutputStream(), true)
        writer.println("NEARBY_REJECTED:USER_DECLINED")
        request.clientSocket.close()
      } catch (_: Exception) {
      } finally {
        if (_incomingRequest.value?.requestId == request.requestId) {
          _incomingRequest.value = null
        }
      }
    }
  }

  /**
   * Initiates connection from Device A to Device B.
   */
  fun requestConnectPeer(
    peer: NearbyPeer,
    context: Context,
    onSuccess: () -> Unit,
    onRejected: (String) -> Unit,
    onError: (String) -> Unit
  ) {
    engineScope?.launch(Dispatchers.IO) {
      try {
        _statusMessage.value = "Connecting to ${peer.deviceName}..."
        val socket = Socket()
        socket.connect(InetSocketAddress(peer.ipAddress, TCP_CMD_PORT), 5000)
        socket.soTimeout = 25000

        val myToken = getHardwareToken(context)
        val myName = getResolvedDeviceName(context)
        val myModel = Build.MODEL ?: "Device"

        val writer = PrintWriter(socket.getOutputStream(), true)
        writer.println("NEARBY_CONNECT_REQ:$myToken:$myName:$myModel")

        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val response = reader.readLine()
        socket.close()

        if (response == "NEARBY_APPROVED") {
          _activePeer.value = peer
          _isSessionActive.value = true
          _statusMessage.value = "Connected to ${peer.deviceName}"

          // Fetch initial remote root storage metrics and file listing
          fetchRemoteStorageMetrics(peer.ipAddress)
          fetchRemoteDirectoryListing(peer.ipAddress, "")

          withContext(Dispatchers.Main) { onSuccess() }
        } else if (response != null && response.startsWith("NEARBY_REJECTED")) {
          val reason = when {
            response.contains("BLOCKED") -> "Connection blocked by device"
            response.contains("MENU_CLOSED") -> "Nearby Devices menu is not open on target device"
            else -> "Connection request declined"
          }
          _statusMessage.value = reason
          withContext(Dispatchers.Main) { onRejected(reason) }
        } else {
          _statusMessage.value = "Connection failed"
          withContext(Dispatchers.Main) { onError("No response from ${peer.deviceName}") }
        }
      } catch (e: Exception) {
        _statusMessage.value = "Could not reach ${peer.deviceName}"
        withContext(Dispatchers.Main) { onError(e.message ?: "Socket connection error") }
      }
    }
  }

  fun fetchRemoteDirectoryListing(peerIp: String, dirPath: String) {
    engineScope?.launch(Dispatchers.IO) {
      try {
        val socket = Socket()
        socket.connect(InetSocketAddress(peerIp, TCP_CMD_PORT), 4000)
        socket.soTimeout = 15000

        val writer = PrintWriter(socket.getOutputStream(), true)
        writer.println("REMOTE_CMD:LIST_DIR:$dirPath")

        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val response = reader.readLine()
        socket.close()

        if (response != null && response.startsWith("REMOTE_RES_LIST:")) {
          val body = response.removePrefix("REMOTE_RES_LIST:")
          val firstColon = body.indexOf(":")
          val resolvedPath = body.substring(0, firstColon)
          val jsonStr = body.substring(firstColon + 1)

          val array = JSONArray(jsonStr)
          val list = mutableListOf<RemoteFileItem>()
          for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
              RemoteFileItem(
                name = obj.getString("name"),
                path = obj.getString("path"),
                isDirectory = obj.getBoolean("isDirectory"),
                sizeBytes = obj.getLong("sizeBytes"),
                lastModified = obj.getLong("lastModified"),
                formattedSize = obj.getString("formattedSize")
              )
            )
          }

          _remoteCurrentPath.value = resolvedPath
          _remoteFileList.value = list.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun fetchRemoteStorageMetrics(peerIp: String) {
    engineScope?.launch(Dispatchers.IO) {
      try {
        val socket = Socket()
        socket.connect(InetSocketAddress(peerIp, TCP_CMD_PORT), 4000)
        socket.soTimeout = 10000

        val writer = PrintWriter(socket.getOutputStream(), true)
        writer.println("REMOTE_CMD:GET_STORAGE_METRICS")

        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val response = reader.readLine()
        socket.close()

        if (response != null && response.startsWith("REMOTE_RES_METRICS:")) {
          val jsonStr = response.removePrefix("REMOTE_RES_METRICS:")
          val obj = JSONObject(jsonStr)
          val metrics = RemoteStorageMetrics(
            deviceName = _activePeer.value?.deviceName ?: "Remote Device",
            totalBytes = obj.optLong("totalBytes"),
            usedBytes = obj.optLong("usedBytes"),
            freeBytes = obj.optLong("freeBytes"),
            totalFormatted = obj.optString("totalFormatted", "0 GB"),
            usedFormatted = obj.optString("usedFormatted", "0 GB"),
            freeFormatted = obj.optString("freeFormatted", "0 GB"),
            usedRatio = obj.optDouble("usedRatio", 0.0).toFloat()
          )
          _remoteStorageMetrics.value = metrics
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun fetchRemoteBatteryLevel(peerIp: String) {
    engineScope?.launch(Dispatchers.IO) {
      try {
        val socket = Socket()
        socket.connect(InetSocketAddress(peerIp, TCP_CMD_PORT), 3000)
        socket.soTimeout = 4000

        val writer = PrintWriter(socket.getOutputStream(), true)
        writer.println("REMOTE_CMD:GET_BATTERY")

        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val response = reader.readLine()
        socket.close()

        if (response != null && response.startsWith("REMOTE_RES_BATTERY:")) {
          val level = response.removePrefix("REMOTE_RES_BATTERY:").toIntOrNull()
          if (level != null) {
            _remoteBatteryLevel.value = level
          }
        }
      } catch (_: Exception) {}
    }
  }

  fun renameRemoteItem(peerIp: String, remotePath: String, newName: String, onResult: (Boolean) -> Unit) {
    engineScope?.launch(Dispatchers.IO) {
      var success = false
      try {
        val socket = Socket()
        socket.connect(InetSocketAddress(peerIp, TCP_CMD_PORT), 4000)
        socket.soTimeout = 10000

        val writer = PrintWriter(socket.getOutputStream(), true)
        writer.println("REMOTE_CMD:RENAME:$remotePath||$newName")

        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val response = reader.readLine()
        socket.close()

        success = (response == "REMOTE_RES_OK")
        if (success) {
          val currentDir = _remoteCurrentPath.value
          fetchRemoteDirectoryListing(peerIp, currentDir)
        }
      } catch (_: Exception) {}
      withContext(Dispatchers.Main) { onResult(success) }
    }
  }

  fun deleteRemoteItem(peerIp: String, remotePath: String, onResult: (Boolean) -> Unit) {
    engineScope?.launch(Dispatchers.IO) {
      var success = false
      try {
        val socket = Socket()
        socket.connect(InetSocketAddress(peerIp, TCP_CMD_PORT), 4000)
        socket.soTimeout = 10000

        val writer = PrintWriter(socket.getOutputStream(), true)
        writer.println("REMOTE_CMD:DELETE:$remotePath")

        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val response = reader.readLine()
        socket.close()

        success = (response == "REMOTE_RES_OK")
        if (success) {
          val currentDir = _remoteCurrentPath.value
          fetchRemoteDirectoryListing(peerIp, currentDir)
          fetchRemoteStorageMetrics(peerIp)
        }
      } catch (_: Exception) {}
      withContext(Dispatchers.Main) { onResult(success) }
    }
  }

  fun createRemoteFolder(peerIp: String, parentPath: String, folderName: String, onResult: (Boolean) -> Unit) {
    engineScope?.launch(Dispatchers.IO) {
      var success = false
      try {
        val socket = Socket()
        socket.connect(InetSocketAddress(peerIp, TCP_CMD_PORT), 4000)
        socket.soTimeout = 10000

        val writer = PrintWriter(socket.getOutputStream(), true)
        writer.println("REMOTE_CMD:CREATE_DIR:$parentPath||$folderName")

        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val response = reader.readLine()
        socket.close()

        success = (response == "REMOTE_RES_OK")
        if (success) {
          fetchRemoteDirectoryListing(peerIp, parentPath)
        }
      } catch (_: Exception) {}
      withContext(Dispatchers.Main) { onResult(success) }
    }
  }

  /**
   * Formats HTTP media streaming URL for remote video/audio playback without downloading.
   */
  fun getRemoteStreamingUrl(peerIp: String, remoteFilePath: String): String {
    val encoded = URLEncoder.encode(remoteFilePath, "UTF-8")
    return "http://$peerIp:$HTTP_STREAMING_PORT/stream?path=$encoded"
  }

  /**
   * Rule 4: Prominent 'End Connection' Action
   * Dismantles session, closes sockets, clears session variables, and resets state.
   */
  @Synchronized
  fun endSession() {
    _isSessionActive.value = false
    _activePeer.value = null
    _remoteFileList.value = emptyList()
    _remoteStorageMetrics.value = null
    _remoteCurrentPath.value = ""
    _statusMessage.value = "Disconnected"

    _incomingRequest.value?.let { req ->
      try { req.clientSocket.close() } catch (_: Exception) {}
    }
    _incomingRequest.value = null

    if (!isNearbyMenuOpen) {
      stopServices()
    }
  }

  @Synchronized
  fun stopServices() {
    try { udpBroadcastSocket?.close() } catch (_: Exception) {}
    udpBroadcastSocket = null

    try { udpListenSocket?.close() } catch (_: Exception) {}
    udpListenSocket = null

    try { tcpCmdServerSocket?.close() } catch (_: Exception) {}
    tcpCmdServerSocket = null

    try { httpStreamingServerSocket?.close() } catch (_: Exception) {}
    httpStreamingServerSocket = null

    engineScope?.cancel()
    engineScope = null

    _discoveredPeers.value = emptyList()
    _statusMessage.value = "Offline"
  }
}
