package com.blindrun.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blindrun.app.model.MatchSession
import com.blindrun.app.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val repository: MatchRepository
) : ViewModel() {

    private val _sessions = MutableStateFlow<List<MatchSession>>(emptyList())
    val sessions: StateFlow<List<MatchSession>> = _sessions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadMatches(userId: String, role: String) {
        if (userId.isBlank()) {
            _error.value = "用户ID无效"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.getMatchesForUser(userId, role)
            result.onSuccess { list ->
                _sessions.value = list
            }.onFailure { e ->
                _error.value = e.message ?: "加载失败"
            }
            _isLoading.value = false
        }
    }
}