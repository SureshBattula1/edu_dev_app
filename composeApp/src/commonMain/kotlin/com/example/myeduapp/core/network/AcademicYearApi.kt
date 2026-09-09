package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.AcademicYearListResponse
import com.example.myeduapp.data.model.AcademicYearResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AcademicYearApi {
    private val client = ApiClient.client

    suspend fun getAcademicYears(token: String, includePast: Boolean = true): AcademicYearListResponse {
        return client.get(ApiConfig.ACADEMIC_YEARS) {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("active", true)
            if (includePast) {
                parameter("include_past", 1)
            }
            parameter("per_page", 100)
            parameter("sort_by", "start_date")
            parameter("order", "desc")
        }.body()
    }

    suspend fun getCurrentAcademicYear(token: String): AcademicYearResponse {
        return client.get(ApiConfig.ACADEMIC_YEAR_CURRENT) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }
}
