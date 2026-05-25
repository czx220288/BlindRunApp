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

    private val _sendResult = MutableStateFlow<Boolean?>(null)
    val sendResult: StateFlow<Boolean?> = _sendResult

    /**
     * 触发 SOS 报警，上传当前位置
     * @param context 用于显示 Toast 提示
     * @param onSuccess 成功回调（可选）
     * @param onError 失败回调（可选）
     */
    fun triggerSos(
        context: Context,
        userId: String,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isSending.value = true
            _sendResult.value = null

            // 获取当前最新位置
            val currentLocation = locationRepository.currentLocation.value
            if (currentLocation == null) {
                val errorMsg = "无法获取当前位置，请检查定位权限"
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                onError?.invoke(errorMsg)
                _isSending.value = false
                _sendResult.value = false
                return@launch
            }

            val sosRequest = SosRequest(
                userId = userId,
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude
            )

            try {
                val response = apiService.triggerSos(sosRequest)
                if (response.isSuccessful) {
                    Toast.makeText(context, "SOS 已发送，救援人员将尽快联系您", Toast.LENGTH_LONG).show()
                    _sendResult.value = true
                    onSuccess?.invoke()
                } else {
                    val errorMsg = "SOS 发送失败: ${response.code()}"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    onError?.invoke(errorMsg)
                    _sendResult.value = false
                }
            } catch (e: Exception) {
                val errorMsg = "网络错误: ${e.message}"
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                onError?.invoke(errorMsg)
                _sendResult.value = false
            } finally {
                _isSending.value = false
            }
        }
    }

    /**
     * 重置发送状态（用于页面重新进入时清除上次结果）
     */
    fun resetResult() {
        _sendResult.value = null
    }
}