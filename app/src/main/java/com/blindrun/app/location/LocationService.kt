package com.blindrun.app.location

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.blindrun.app.model.UserLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var locationClient: AMapLocationClient? = null
    private val _locationFlow = MutableStateFlow<UserLocation?>(null)
    val locationFlow: StateFlow<UserLocation?> = _locationFlow
    private var currentUserId: String = ""

    fun init(userId: String) {
        currentUserId = userId
        if (locationClient == null) {
            locationClient = AMapLocationClient(context)
            val option = AMapLocationClientOption().apply {
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                interval = 2000
                isOnceLocation = false
                isNeedAddress = false
            }
            locationClient?.setLocationOption(option)
            locationClient?.setLocationListener(listener)
        }
    }

    private val listener = AMapLocationListener { location ->
        if (location != null && location.errorCode == 0) {
            val userLocation = UserLocation(
                userId = currentUserId,
                latitude = location.latitude,
                longitude = location.longitude,
                timestamp = location.time
            )
            _locationFlow.value = userLocation
        }
    }

    fun startLocation() {
        if (checkPermission()) {
            locationClient?.startLocation()
        }
    }

    fun stopLocation() {
        locationClient?.stopLocation()
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun onDestroy() {
        locationClient?.onDestroy()
    }
}