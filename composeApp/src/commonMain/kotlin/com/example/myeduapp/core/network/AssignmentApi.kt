package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.AssignmentResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AssignmentApi {
    private val client = ApiClient.client

    suspend fun getAssignments(token: String): AssignmentResponse {
        return client.get("assignments") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }
}
