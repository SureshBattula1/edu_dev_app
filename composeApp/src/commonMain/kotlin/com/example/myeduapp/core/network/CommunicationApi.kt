package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.Announcement
import com.example.myeduapp.data.model.ApiResponse
import com.example.myeduapp.data.model.BroadcastNotificationBody
import com.example.myeduapp.data.model.BroadcastNotificationResponse
import com.example.myeduapp.data.model.Holiday
import com.example.myeduapp.data.model.Notification
import com.example.myeduapp.data.model.NotificationReceiptsResponse
import com.example.myeduapp.data.model.NotificationsPageResponse
import com.example.myeduapp.data.model.SentNotificationsResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders

class CommunicationApi {
    private val client = ApiClient.client

    suspend fun getNotifications(
        token: String,
        status: String = "all",
        perPage: Int = 20,
        page: Int = 1,
        type: String? = null,
        source: String? = null,
        period: String? = null
    ): NotificationsPageResponse {
        return client.get("${ApiConfig.COMMUNICATIONS}/notifications") {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("status", status)
            parameter("per_page", perPage)
            parameter("page", page)
            type?.let { parameter("type", it) }
            source?.let { parameter("source", it) }
            period?.let { parameter("period", it) }
        }.body()
    }

    suspend fun markNotificationRead(token: String, id: String): ApiResponse<Notification?> {
        return client.post("${ApiConfig.COMMUNICATIONS}/notifications/$id/read") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun markAllNotificationsRead(token: String): ApiResponse<Unit?> {
        return client.post("${ApiConfig.COMMUNICATIONS}/notifications/read-all") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun getNotificationReceipts(token: String, groupKey: String): NotificationReceiptsResponse {
        return client.get("${ApiConfig.COMMUNICATIONS}/notifications/receipts") {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("group_key", groupKey)
        }.body()
    }

    suspend fun broadcastNotification(token: String, body: BroadcastNotificationBody): BroadcastNotificationResponse {
        return client.post("${ApiConfig.COMMUNICATIONS}/notifications/broadcast") {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(body)
        }.body()
    }

    suspend fun getSentNotifications(token: String): SentNotificationsResponse {
        return client.get("${ApiConfig.COMMUNICATIONS}/notifications/sent") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun getAnnouncements(token: String): List<Announcement> {
        val response: ApiResponse<List<Announcement>> = client.get("${ApiConfig.COMMUNICATIONS}/announcements") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
        return response.data ?: emptyList()
    }

    suspend fun getHolidays(token: String): List<Holiday> {
        val response: ApiResponse<List<Holiday>> = client.get(ApiConfig.HOLIDAYS) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
        return response.data ?: emptyList()
    }
}
