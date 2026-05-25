package com.blindrun.app.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blindrun.app.location.LocationService
import com.blindrun.app.model.UserLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RunViewModel @Inject constructor(
    private val locationService: LocationService
) : ViewModel() {

    private val _myLocation = MutableStateFlow<UserLocation?>(null)
    val myLocation: StateFlow<UserLocation?> = _myLocation

    private val _partnerLocation = MutableStateFlow<UserLocation?>(null)
    val partnerLocation: StateFlow<UserLocation?> = _partnerLocation

    fun startRun(recruitId: String, context: Context) {
        try {
            val userId = "user_${System.currentTimeMillis()}"
            locationService.init(userId)
            locationService.startLocation()

            viewModelScope.launch {
                locationService.locationFlow.collect { location ->
                    _myLocation.value = location
                    // 模拟伙伴位置
                    if (_partnerLocation.value == null && location != null) {
                        _partnerLocation.value = UserLocation(
                            userId = "partner",
                            latitude = location.latitude + 0.002,
                            longitude = location.longitude + 0.002,
                            timestamp = System.currentTimeMillis()
                        )
                    } else if (_partnerLocation.value != null) {
                        val partner = _partnerLocation.value!!
                        _partnerLocation.value = partner.copy(
                            latitude = partner.latitude + 0.0001,
                            longitude = partner.longitude + 0.0001,
                            timestamp = System.currentTimeMillis()
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "定位初始化失败", Toast.LENGTH_SHORT).show()
        }
    }

    fun triggerSos(context: Context) {
        Toast.makeText(context, "SOS 已发送", Toast.LENGTH_LONG).show()
    }

    fun stopRun() {
        try {
            locationService.stopLocation()
        } catch (e: Exception) {
            // ignore
        }
    }
}