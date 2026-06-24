package com.blindrun.app.model

data class MatchStartRequest(
    val sessionId: String,
    val userId: String,
    val role: String
)