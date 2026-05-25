package com.blindrun.app.repository

import com.blindrun.app.model.AcceptRequest
import com.blindrun.app.model.MatchSession
import com.blindrun.app.model.Recruit
import com.blindrun.app.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecruitRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun publishRecruit(recruit: Recruit): Result<Recruit> {
        return try {
            val response = apiService.publishRecruit(recruit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("发布失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNearbyRecruits(lat: Double, lng: Double): Result<List<Recruit>> {
        return try {
            val response = apiService.getNearbyRecruits(lat, lng)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("获取附近招募失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptRecruit(recruitId: String, companionId: String): Result<MatchSession> {
        return try {
            val request = AcceptRequest(recruitId, companionId)
            val response = apiService.acceptRecruit(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("接单失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}