package com.blindrun.app.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blindrun.app.model.SosRequest
import com.blindrun.app.network.ApiService
import com.blindrun.app.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SosViewModel @Inject constructor(
    private val apiService: ApiService,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    fun triggerSos(
        context: Context,
        userId: String,
        sessionId: String? = null,
        recruitId: String? = null,
        companionUserId: String? = null,
        blindUserId: String? = null,
        sessionStatus: String? = null,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isSending.value = true

            val currentLocation = locationRepository.currentLocation.value
            if (currentLocation == null) {
                val errorMsg = "无法获取当前位置"
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                onError?.invoke(errorMsg)
                _isSending.value = false
                return@launch
            }

            val sosRequest = SosRequest(
                userId = userId,
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude,
                sessionId = sessionId,
                recruitId = recruitId,
                companionUserId = companionUserId,
                blindUserId = blindUserId,
                sessionStatus = sessionStatus
            )

            try {
                val response = apiService.triggerSos(sosRequest)
                if (response.isSuccessful) {
                    Toast.makeText(context, "SOS 已发送", Toast.LENGTH_SHORT).show()
                    onSuccess?.invoke()
                } else {
                    val errorMsg = "SOS 发送失败"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    onError?.invoke(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "网络错误"
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                onError?.invoke(errorMsg)
            } finally {
                _isSending.value = false
            }
        }
    }
}