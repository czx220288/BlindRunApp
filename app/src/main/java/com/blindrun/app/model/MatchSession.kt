package com.blindrun.app.model

data class MatchSession(
    val sessionId: String,
    val blindUserId: String,
    val companionUserId: String,
    val active: Boolean
)