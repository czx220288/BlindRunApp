package com.blindrun.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blindrun.app.model.*
import com.blindrun.app.network.ApiService
import com.blindrun.app.network.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val apiService: ApiService,
    val webSocketManager: WebSocketManager
) : ViewModel() {

    private val _sessions = MutableStateFlow<List<MatchSession>>(emptyList())
    val sessions: StateFlow<List<MatchSession>> = _sessions

    private val _activeRecruits = MutableStateFlow<List<Recruit>>(emptyList())
    val activeRecruits: StateFlow<List<Recruit>> = _activeRecruits

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadMatches(userId: String, role: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiService.getMatchHistory(userId, role)
                if (response.isSuccessful) {
                    _sessions.value = response.body() ?: emptyList()
                    
                    val recruitIds = _sessions.value.map { it.recruitId }.distinct()
                    val recruits = mutableListOf<Recruit>()
                    for (recruitId in recruitIds) {
                        val recruitResponse = apiService.getRecruitById(recruitId)
                        if (recruitResponse.isSuccessful) {
                            recruitResponse.body()?.let { recruits.add(it) }
                        }
                    }
                    _activeRecruits.value = recruits
                } else {
                    _error.value = "加载失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "网络错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startMatch(sessionId: String, userId: String, role: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val request = MatchStartRequest(
                    sessionId = sessionId,
                    userId = userId,
                    role = role
                )
                val response = apiService.startMatch(request)
                if (response.isSuccessful && response.body() != null) {
                    val session = response.body()!!
                    if (session.active) {
                        onResult(true)
                    } else {
                        onResult(true)
                    }
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }
    }

    fun cancelMatch(sessionId: String, userId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val request = MatchCancelRequest(
                    sessionId = sessionId,
                    userId = userId
                )
                val response = apiService.cancelMatch(request)
                onResult(response.isSuccessful)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }
    }
}