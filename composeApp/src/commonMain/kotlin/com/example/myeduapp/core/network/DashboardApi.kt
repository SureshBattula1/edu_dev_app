package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.DashboardResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class DashboardApi {
    private val client = ApiClient.client

    suspend fun getDashboardData(token: String): DashboardResponse {
        return client.get(ApiConfig.DASHBOARD) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }
}
