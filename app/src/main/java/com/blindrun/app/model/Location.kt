package com.blindrun.app.model

data class UserLocation(
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)
