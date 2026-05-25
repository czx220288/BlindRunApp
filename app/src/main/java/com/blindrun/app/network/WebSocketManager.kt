package com.blindrun.app.network

import android.util.Log
import com.blindrun.app.model.UserLocation
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.*
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor(private val client: OkHttpClient) {
    private var webSocket: WebSocket? = null
    private val _locationFlow = MutableSharedFlow<UserLocation>(extraBufferCapacity = 10)
    val locationFlow: SharedFlow<UserLocation> = _locationFlow
    private val _notificationFlow = MutableSharedFlow<Map<String, String>>(extraBufferCapacity = 10)
    val notificationFlow: SharedFlow<Map<String, String>> = _notificationFlow

    fun connect(userId: String, sessionId: String) {
        val webSocketUrl = "ws://10.62.68.184:8080/ws/location?userId=$userId&sessionId=$sessionId"
        Log.d("WebSocket", "Connecting to $webSocketUrl")
        val request = Request.Builder().url(webSocketUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Received: $text")
                try {
                    val json = JSONObject(text)
                    if (json.has("type")) {
                        val type = json.getString("type")
                        val recruitId = json.optString("recruitId")
                        val notification = mapOf("type" to type, "recruitId" to recruitId)
                        _notificationFlow.tryEmit(notification)
                        Log.d("WebSocket", "Emitted notification: $notification")
                    } else {
                        val location = Gson().fromJson(text, UserLocation::class.java)
                        _locationFlow.tryEmit(location)
                    }
                } catch (e: Exception) {
                    Log.e("WebSocket", "Parse error: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error: ${t.message}")
            }

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connected")
            }
        })
    }

    fun sendLocation(location: UserLocation) {
        val json = Gson().toJson(location)
        webSocket?.send(json)
    }

    fun disconnect() {
        webSocket?.close(1000, null)
        webSocket = null
    }
}