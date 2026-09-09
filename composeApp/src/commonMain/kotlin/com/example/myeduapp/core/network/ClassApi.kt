package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.ClassResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ClassApi {
    private val client = ApiClient.client

    suspend fun getMyClasses(token: String): ClassResponse {
        return client.get("classes") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }
}
