package com.blindrun.app.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor() {
    private val _currentLocation = MutableStateFlow<com.blindrun.app.model.UserLocation?>(null)
    val currentLocation: StateFlow<com.blindrun.app.model.UserLocation?> = _currentLocation

    fun updateLocation(location: com.blindrun.app.model.UserLocation) {
        _currentLocation.value = location
    }
}