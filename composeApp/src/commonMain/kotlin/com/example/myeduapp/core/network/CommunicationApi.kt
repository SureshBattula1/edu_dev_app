package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.Notification
import com.example.myeduapp.data.model.Holiday
import com.example.myeduapp.data.model.Announcement
import com.example.myeduapp.data.model.ApiResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class CommunicationApi {
    private val client = ApiClient.client

    suspend fun getNotifications(token: String): List<Notification> {
        val response: ApiResponse<List<Notification>> = client.get("${ApiConfig.COMMUNICATIONS}/notifications") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
        return response.data ?: emptyList()
    }

    suspend fun getAnnouncements(token: String): List<Announcement> {
        val response: ApiResponse<List<Announcement>> = client.get("${ApiConfig.COMMUNICATIONS}/announcements") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
        return response.data ?: emptyList()
    }

    suspend fun getHolidays(token: String): List<Holiday> {
        val response: ApiResponse<List<Holiday>> = client.get("holidays/upcoming") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
        return response.data ?: emptyList()
    }
}
