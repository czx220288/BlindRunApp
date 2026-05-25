package com.blindrun.app.model

data class Recruit(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val startTime: Long = 0,
    val startLocation: String = "",
    val startLat: Double = 0.0,
    val startLng: Double = 0.0,
    val endLocation: String = "",
    val endLat: Double = 0.0,
    val endLng: Double = 0.0,
    val distance: Int = 0,
    var status: String = ""
)