package com.blindrun.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blindrun.app.model.MatchSession
import com.blindrun.app.model.Recruit
import com.blindrun.app.repository.RecruitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyViewModel @Inject constructor(
    private val repository: RecruitRepository
) : ViewModel() {

    private val _recruits = MutableStateFlow<List<Recruit>>(emptyList())
    val recruits: StateFlow<List<Recruit>> = _recruits

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadNearbyRecruits(lat: Double = 39.9042, lng: Double = 116.4074) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.getNearbyRecruits(lat, lng)
            result.onSuccess { recruits ->
                _recruits.value = recruits
            }.onFailure { e ->
                _error.value = e.message ?: "加载失败"
            }
            _isLoading.value = false
        }
    }

    fun acceptRecruit(recruit: Recruit, companionId: String, onResult: (Boolean, MatchSession?) -> Unit) {
        viewModelScope.launch {
            val result = repository.acceptRecruit(recruit.id, companionId)
            if (result.isSuccess) {
                onResult(true, result.getOrNull())
            } else {
                onResult(false, null)
            }
        }
    }
}