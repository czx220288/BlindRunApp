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
        @Query("lng") lng: Double
    ): Response<List<Recruit>>

    @POST("/api/recruit/accept")
    suspend fun acceptRecruit(@Body request: AcceptRequest): Response<MatchSession>

    @POST("/api/sos/trigger")
    suspend fun triggerSos(@Body request: SosRequest): Response<Unit>

    @GET("/api/recruit/{id}")
    suspend fun getRecruitById(@retrofit2.http.Path("id") id: String): Response<Recruit>
}