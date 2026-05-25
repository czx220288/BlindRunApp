package com.blindrun.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blindrun.app.model.Recruit
import com.blindrun.app.repository.RecruitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PublishViewModel @Inject constructor(
    private val repository: RecruitRepository
) : ViewModel() {

    private val _isPublishing = MutableStateFlow(false)
    val isPublishing: StateFlow<Boolean> = _isPublishing

    suspend fun publishRecruit(recruit: Recruit): Result<Recruit> {
        _isPublishing.value = true
        val result = repository.publishRecruit(recruit)
        _isPublishing.value = false
        return result
    }
}