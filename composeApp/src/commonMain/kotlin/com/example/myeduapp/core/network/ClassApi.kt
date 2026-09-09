package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.ClassResponse
import com.example.myeduapp.data.model.FilterOptionListResponse
import com.example.myeduapp.data.model.GradeListResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ClassApi {
    private val client = ApiClient.client

    suspend fun getMyClasses(token: String, branchId: Int? = null, academicYear: String? = null): ClassResponse {
        return client.get(ApiConfig.CLASSES) {
            header(HttpHeaders.Authorization, "Bearer $token")
            branchId?.let { parameter("branch_id", it) }
            academicYear?.let { parameter("academic_year", it) }
            parameter("is_active", true)
        }.body()
    }

    suspend fun getGrades(token: String, branchId: Int? = null): GradeListResponse {
        return client.get(ApiConfig.CLASS_GRADES) {
            header(HttpHeaders.Authorization, "Bearer $token")
            branchId?.let { parameter("branch_id", it) }
        }.body()
    }

    suspend fun getSections(token: String, grade: String? = null, branchId: Int? = null): FilterOptionListResponse {
        return client.get(ApiConfig.CLASS_SECTIONS) {
            header(HttpHeaders.Authorization, "Bearer $token")
            grade?.let { parameter("grade", it) }
            branchId?.let { parameter("branch_id", it) }
        }.body()
    }
}
