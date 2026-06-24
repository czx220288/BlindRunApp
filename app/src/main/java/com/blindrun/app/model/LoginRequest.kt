package com.blindrun.app.model

data class LoginRequest(
    val userId: String,
    val password: String,
    val role: String
)