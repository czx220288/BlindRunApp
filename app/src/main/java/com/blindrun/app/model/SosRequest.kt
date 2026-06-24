package com.blindrun.app.model

import com.google.gson.annotations.SerializedName

data class SosRequest(
    @SerializedName("userId")
    val userId: String = "",
    
    @SerializedName("latitude")
    val latitude: Double = 0.0,
    
    @SerializedName("longitude")
    val longitude: Double = 0.0,
    
    @SerializedName("sessionId")
    val sessionId: String? = null,
    
    @SerializedName("recruitId")
    val recruitId: String? = null,
    
    @SerializedName("companionUserId")
    val companionUserId: String? = null,
    
    @SerializedName("blindUserId")
    val blindUserId: String? = null,
    
    @SerializedName("sessionStatus")
    val sessionStatus: String? = null
)
