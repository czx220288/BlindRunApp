package com.blindrun.app.repository

import com.blindrun.app.model.MatchSession
import com.blindrun.app.network.MockApiInterceptor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepository @Inject constructor() {

    suspend fun getMatchesForUser(userId: String, role: String): Result<List<MatchSession>> {
        val sessions = MockApiInterceptor.sessionsStore[userId] ?: emptyList()
        return Result.success(sessions)
    }
}