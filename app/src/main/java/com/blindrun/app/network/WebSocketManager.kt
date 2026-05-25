package com.blindrun.app.network

import android.util.Log
import com.blindrun.app.model.UserLocation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor() {
    private val _locationFlow = MutableSharedFlow<UserLocation>(extraBufferCapacity = 10)
    val locationFlow: SharedFlow<UserLocation> = _locationFlow

    private var isConnected = false
    private var currentSessionId: String? = null

    // 模拟 WebSocket 连接，实际使用时替换为真实的 OkHttp WebSocket
    fun connect(userId: String, sessionId: String) {
        Log.d("WebSocket", "Connecting to session $sessionId as user $userId")
        currentSessionId = sessionId
        isConnected = true
        // 模拟接收到对方位置（实际应监听服务器推送）
    }

    fun sendLocation(lat: Double, lng: Double) {
        if (!isConnected) return
        Log.d("WebSocket", "Send location: $lat, $lng")
        // 实际 webSocket.send(...)
        // 这里模拟将位置发送给服务器，服务器再广播给伙伴
    }

    fun receivePartnerLocation(partnerLocation: UserLocation) {
        // 供外部模拟调用，实际由 WebSocket 回调触发
        _locationFlow.tryEmit(partnerLocation)
    }

    fun disconnect() {
        isConnected = false
        currentSessionId = null
    }
}