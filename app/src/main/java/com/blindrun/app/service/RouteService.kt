package com.blindrun.app.service

import com.blindrun.app.model.RouteResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface RouteService {
    @GET("v3/direction/walking")
    suspend fun getWalkingRoute(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") key: String
    ): Response<RouteResponse>
}