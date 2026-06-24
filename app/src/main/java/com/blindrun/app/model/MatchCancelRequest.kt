package com.blindrun.app.model

data class MatchCancelRequest(
    val sessionId: String,
    val userId: String
)