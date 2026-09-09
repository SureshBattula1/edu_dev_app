package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.StudentResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class StudentApi {
    private val client = ApiClient.client

    suspend fun getStudents(token: String, query: String? = null): StudentResponse {
        return client.get("students") {
            parameter("search", query)
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }
}
