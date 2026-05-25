package com.blindrun.app.network

import android.util.Log
import com.blindrun.app.model.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.Buffer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MockApiInterceptor : Interceptor {

    companion object {
        val recruitsStore = ConcurrentHashMap<String, Recruit>()
        val sessionsStore = mutableMapOf<String, MutableList<MatchSession>>()
        private val gson = com.google.gson.Gson()

        init {
            // 预置招募（起终点坐标示例）
            val recruit1 = Recruit(
                id = "1", userId = "blind1", userName = "张明",
                startTime = System.currentTimeMillis() + 3600000,
                startLocation = "朝阳公园南门", startLat = 39.9142, startLng = 116.4174,
                endLocation = "朝阳公园北门", endLat = 39.9242, endLng = 116.4274,
                distance = 1000, status = "active"
            )
            val recruit2 = Recruit(
                id = "2", userId = "blind2", userName = "李芳",
                startTime = System.currentTimeMillis() + 7200000,
                startLocation = "奥森北园", startLat = 39.8992, startLng = 116.4154,
                endLocation = "奥森南园", endLat = 39.8892, endLng = 116.4254,
                distance = 800, status = "active"
            )
            recruitsStore[recruit1.id] = recruit1
            recruitsStore[recruit2.id] = recruit2
        }

        fun addSession(userId: String, session: MatchSession) {
            sessionsStore.getOrPut(userId) { mutableListOf() }.add(session)
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val method = request.method

        Thread.sleep(200)

        return when {
            path == "/api/login" && method == "POST" -> {
                val body = getRequestBody(request)
                val loginRequest = gson.fromJson(body, LoginRequest::class.java)
                val user = User(
                    userId = loginRequest.userId,
                    name = if (loginRequest.role == "blind") "盲人用户" else "陪跑员",
                    role = loginRequest.role,
                    token = "mock_token_${UUID.randomUUID()}"
                )
                mockResponse(200, gson.toJson(user))
            }
            path == "/api/recruit/publish" && method == "POST" -> {
                val body = getRequestBody(request)
                val recruit = gson.fromJson(body, Recruit::class.java)
                val newRecruit = recruit.copy(id = UUID.randomUUID().toString())
                recruitsStore[newRecruit.id] = newRecruit
                mockResponse(200, gson.toJson(newRecruit))
            }
            path == "/api/recruit/nearby" && method == "GET" -> {
                val activeList = recruitsStore.values.filter { it.status == "active" }
                mockResponse(200, gson.toJson(activeList))
            }
            path == "/api/recruit/accept" && method == "POST" -> {
                val body = getRequestBody(request)
                val accept = gson.fromJson(body, AcceptRequest::class.java)
                val recruit = recruitsStore[accept.recruitId]
                if (recruit != null && recruit.status == "active") {
                    recruit.status = "accepted"
                    val session = MatchSession(
                        sessionId = UUID.randomUUID().toString(),
                        blindUserId = recruit.userId,
                        companionUserId = accept.companionId,
                        active = true
                    )
                    addSession(recruit.userId, session)
                    addSession(accept.companionId, session)
                    mockResponse(200, gson.toJson(session))
                } else {
                    mockResponse(400, "{\"error\":\"招募不存在或已接单\"}")
                }
            }
            path == "/api/sos/trigger" && method == "POST" -> {
                val body = getRequestBody(request)
                val sos = gson.fromJson(body, SosRequest::class.java)
                Log.w("MockSOS", "SOS triggered by ${sos.userId} at (${sos.latitude}, ${sos.longitude})")
                mockResponse(200, "{}")
            }
            else -> mockResponse(404, "{\"error\":\"not found\"}")
        }
    }

    private fun getRequestBody(request: Request): String {
        val buffer = Buffer()
        request.body?.writeTo(buffer)
        return buffer.readUtf8()
    }

    private fun mockResponse(code: Int, body: String): Response {
        return Response.Builder()
            .request(Request.Builder().url("https://mock.api.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("Mock")
            .body(ResponseBody.create("application/json".toMediaType(), body))
            .build()
    }
}