package com.blindrun.app.model

import com.google.gson.annotations.SerializedName

data class MatchSession(
    @SerializedName("sessionId")
    val sessionId: String = "",
    
    @SerializedName("blindUserId")
    val blindUserId: String = "",
    
    @SerializedName("companionUserId")
    val companionUserId: String = "",
    
    @SerializedName("recruitId")
    val recruitId: String = "",
    
    @SerializedName("active")
    val active: Boolean = false,
    
    @SerializedName("blindConfirmed")
    val blindConfirmed: Boolean = false,
    
    @SerializedName("companionConfirmed")
    val companionConfirmed: Boolean = false,
    
    @SerializedName("status")
    val status: String = "pending",
    
    @SerializedName("startTime")
    val startTime: Long = 0,
    
    @SerializedName("createdAt")
    val createdAt: String? = null
)