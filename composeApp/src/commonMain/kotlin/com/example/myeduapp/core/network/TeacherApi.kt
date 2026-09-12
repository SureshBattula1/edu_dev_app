package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.TeacherListResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders

class TeacherApi {
    private val client = ApiClient.client

    suspend fun getTeachers(
        token: String,
        search: String? = null,
        branchId: Int? = null,
        grade: String? = null,
        section: String? = null,
        page: Int = 1,
        perPage: Int = 100
    ): TeacherListResponse {
        return client.get(ApiConfig.TEACHERS) {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("page", page)
            parameter("per_page", perPage)
            search?.takeIf { it.isNotBlank() }?.let { parameter("search", it) }
            branchId?.let { parameter("branch_id", it) }
            grade?.takeIf { it.isNotBlank() }?.let { parameter("grade", it) }
            section?.takeIf { it.isNotBlank() }?.let { parameter("section", it) }
            parameter("is_active", true)
        }.body()
    }
}
