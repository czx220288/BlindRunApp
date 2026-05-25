package com.blindrun.app.network

import com.blindrun.app.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("/api/login")
    suspend fun login(@Body request: LoginRequest): Response<User>

    @POST("/api/recruit/publish")
    suspend fun publishRecruit(@Body recruit: Recruit): Response<Recruit>

    @GET("/api/recruit/nearby")
    suspend fun getNearbyRecruits(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Int = 5000
    ): Response<List<Recruit>>

    @POST("/api/recruit/accept")
    suspend fun acceptRecruit(@Body request: AcceptRequest): Response<MatchSession>

    @POST("/api/sos/trigger")
    suspend fun triggerSos(@Body request: SosRequest): Response<Unit>
}

data class LoginRequest(
    val userId: String,
    val role: String
)