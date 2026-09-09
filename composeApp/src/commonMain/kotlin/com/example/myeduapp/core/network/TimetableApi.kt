package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.TimetableSlot
import com.example.myeduapp.data.model.TimetableResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class TimetableApi {
    private val client = ApiClient.client

    suspend fun getTimetableByClass(token: String, grade: String, section: String): List<TimetableSlot> {
        val response: TimetableResponse = client.get("timetables/class/$grade/$section") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
        return response.data
    }
}
