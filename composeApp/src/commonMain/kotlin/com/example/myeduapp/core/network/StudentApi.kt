package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.StudentResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class StudentApi {
    private val client = ApiClient.client

    suspend fun getStudents(
        token: String,
        query: String? = null,
        grade: String? = null,
        section: String? = null,
        academicYearId: String? = null,
        page: Int = 1,
        perPage: Int = 100
    ): StudentResponse {
        return client.get(ApiConfig.STUDENTS) {
            parameter("search", query)
            parameter("grade", grade)
            parameter("section", section)
            parameter("page", page)
            parameter("per_page", perPage)
            academicYearId?.let { parameter("academic_year_id", it) }
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }
}
