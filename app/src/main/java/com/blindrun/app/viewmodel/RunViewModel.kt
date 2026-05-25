package com.blindrun.app.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blindrun.app.location.LocationService
import com.blindrun.app.model.Recruit
import com.blindrun.app.model.UserLocation
import com.blindrun.app.network.WebSocketManager
import com.blindrun.app.repository.RecruitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RunViewModel @Inject constructor(
    private val locationService: LocationService,
    private val webSocketManager: WebSocketManager,
    private val recruitRepository: RecruitRepository
) : ViewModel() {

    private val _recruit = MutableStateFlow<Recruit?>(null)
    val recruit: StateFlow<Recruit?> = _recruit

    private val _myLocation = MutableStateFlow<UserLocation?>(null)
    val myLocation: StateFlow<UserLocation?> = _myLocation

    private val _partnerLocation = MutableStateFlow<UserLocation?>(null)
    val partnerLocation: StateFlow<UserLocation?> = _partnerLocation

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadRecruit(recruitId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = recruitRepository.getRecruitById(recruitId)
            result.onSuccess { recruitData ->
                _recruit.value = recruitData
            }.onFailure {
                _recruit.value = null
            }
            _isLoading.value = false
        }
    }

    fun startRun(recruitId: String, context: Context) {
        val userId = "user_${System.currentTimeMillis()}"
        val sessionId = "session_$recruitId"
        // 建立 WebSocket 连接（用于位置同步）
        webSocketManager.connect(userId, sessionId)

        locationService.init(userId)
        locationService.startLocation()

        viewModelScope.launch {
            locationService.locationFlow.collect { location ->
                if (location != null) {
                    _myLocation.value = location
                    webSocketManager.sendLocation(location)
                }
            }
        }

        viewModelScope.launch {
            webSocketManager.locationFlow.collect { partnerLoc ->
                _partnerLocation.value = partnerLoc
            }
        }
    }

    fun triggerSos(context: Context) {
        Toast.makeText(context, "SOS 已发送", Toast.LENGTH_SHORT).show()
    }

    fun stopRun() {
        locationService.stopLocation()
        webSocketManager.disconnect()
    }
}