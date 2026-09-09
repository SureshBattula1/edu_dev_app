package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.LeaveRequest
import com.example.myeduapp.data.model.ApiResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class LeaveApi {
    private val client = ApiClient.client

    suspend fun getStudentLeaves(token: String, studentId: Int): List<LeaveRequest> {
        val response: ApiResponse<List<LeaveRequest>> = client.get("${ApiConfig.LEAVES}/student/$studentId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
        return response.data ?: emptyList()
    }

    suspend fun applyLeave(token: String, leave: LeaveRequest): Boolean {
        val response: ApiResponse<Boolean> = client.post(ApiConfig.LEAVES) {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(leave)
        }.body()
        return response.success
    }
}
