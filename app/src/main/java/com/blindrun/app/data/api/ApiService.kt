package com.blindrun.app.data.api

import com.blindrun.app.data.model.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("users")
    suspend fun getUsers(): List<UserDto>

    @POST("users")
    suspend fun createUser(@Body user: UserDto): UserDto
}