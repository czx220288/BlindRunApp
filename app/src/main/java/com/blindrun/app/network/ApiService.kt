package com.blindrun.app.network

import com.blindrun.app.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("/api/login")
    suspend fun login(@Body request: LoginRequest): Response<User>

    @POST("/api/register")
    suspend fun register(@Body user: User): Response<User>

    @POST("/api/recruit/publish")
    suspend fun publishRecruit(@Body recruit: Recruit): Response<Recruit>

    @GET("/api/recruit/nearby")
    suspend fun getNearbyRecruits(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): Response<List<Recruit>>

    @GET("/api/recruit/history")
    suspend fun getRecruitHistory(@Query("userId") userId: String): Response<List<Recruit>>

    @GET("/api/match/history")
    suspend fun getMatchHistory(
        @Query("userId") userId: String,
        @Query("role") role: String
    ): Response<List<MatchSession>>

    @GET("/api/match/active")
    suspend fun getActiveMatches(
        @Query("userId") userId: String,
        @Query("role") role: String
    ): Response<List<MatchSession>>

    @POST("/api/recruit/accept")
    suspend fun acceptRecruit(@Body request: AcceptRequest): Response<MatchSession>

    @POST("/api/match/start")
    suspend fun startMatch(@Body request: MatchStartRequest): Response<MatchSession>

    @POST("/api/match/cancel")
    suspend fun cancelMatch(@Body request: MatchCancelRequest): Response<Unit>

    @POST("/api/match/finish")
    suspend fun finishMatch(@Body request: MatchFinishRequest): Response<MatchSession>

    @POST("/api/sos/trigger")
    suspend fun triggerSos(@Body request: SosRequest): Response<Unit>

    @GET("/api/recruit/{id}")
    suspend fun getRecruitById(@retrofit2.http.Path("id") id: String): Response<Recruit>

    @GET("/api/match/session/{recruitId}")
    suspend fun getMatchSessionByRecruitId(@retrofit2.http.Path("recruitId") recruitId: String): Response<MatchSession>
}