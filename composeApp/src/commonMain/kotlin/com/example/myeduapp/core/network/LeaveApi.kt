package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders

class LeaveApi {
    private val client = ApiClient.client

    suspend fun getLeaves(
        token: String,
        type: String,
        status: String? = null,
        page: Int = 1,
        perPage: Int = 50
    ): LeaveListResponse {
        return client.get(ApiConfig.LEAVES) {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("type", type)
            status?.let { parameter("status", it) }
            parameter("page", page)
            parameter("per_page", perPage)
        }.body()
    }

    suspend fun getStudentLeaves(token: String, userId: Int): LeaveListResponse {
        return client.get("${ApiConfig.LEAVES}/student/$userId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun getTeacherLeaves(token: String, userId: Int): LeaveListResponse {
        return client.get("${ApiConfig.LEAVES}/teacher/$userId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun getLeaveDetail(token: String, leaveId: Int, type: String): LeaveDetailResponse {
        return client.get("${ApiConfig.LEAVES}/$leaveId") {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("type", type)
        }.body()
    }

    suspend fun applyStudentLeave(token: String, body: CreateStudentLeaveBody): LeaveActionResponse {
        return client.post(ApiConfig.LEAVES) {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("type", "student")
            setBody(body)
        }.body()
    }

    suspend fun applyTeacherLeave(token: String, body: CreateTeacherLeaveBody): LeaveActionResponse {
        return client.post(ApiConfig.LEAVES) {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("type", "teacher")
            setBody(body)
        }.body()
    }

    suspend fun updateLeave(
        token: String,
        leaveId: Int,
        type: String,
        body: UpdateLeaveBody
    ): LeaveActionResponse {
        return client.put("${ApiConfig.LEAVES}/$leaveId") {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("type", type)
            setBody(body)
        }.body()
    }
}
