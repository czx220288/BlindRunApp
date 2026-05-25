package com.blindrun.app.model

data class Recruit(
    val id: String,
    val userId: String,
    val userName: String,
    val startTime: Long,
    val startLocation: String,
    val startLat: Double,
    val startLng: Double,
    val endLocation: String,
    val endLat: Double,
    val endLng: Double,
    val distance: Int,
    var status: String
)