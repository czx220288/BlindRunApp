package com.blindrun.app.model

data class User(
    val userId: String,
    val name: String,
    val role: String,
    val password: String = "",
    val token: String = ""
)