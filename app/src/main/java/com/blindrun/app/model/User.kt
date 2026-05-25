package com.blindrun.app.model

data class User(
    val userId: String,
    val name: String,
    val role: String, // "blind" or "companion"
    val token: String = ""
)