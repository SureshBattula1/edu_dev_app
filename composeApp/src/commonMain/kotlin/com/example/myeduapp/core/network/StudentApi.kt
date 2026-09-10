package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.StudentDetailParser
import com.example.myeduapp.data.model.StudentDetailResponse
import com.example.myeduapp.data.model.StudentResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class StudentApi {
    private val client = ApiClient.client

    suspend fun getStudentDetail(token: String, studentId: String): StudentDetailResponse {
        val httpResponse = client.get("${ApiConfig.STUDENTS}/$studentId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val body = httpResponse.bodyAsText()
        return parseDetailResponse(body)
    }

    suspend fun getStudentByUserId(token: String, userId: Int): StudentDetailResponse {
        val httpResponse = client.get("${ApiConfig.STUDENTS}/by-user/$userId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val body = httpResponse.bodyAsText()
        return parseDetailResponse(body)
    }

    private fun parseDetailResponse(body: String): StudentDetailResponse {
        return try {
            val json = ApiClient.jsonConfig.parseToJsonElement(body).jsonObject
            val success = json["success"]?.jsonPrimitive?.booleanOrNull
                ?: json["success"]?.jsonPrimitive?.content?.toBooleanStrictOrNull()
                ?: false
            val message = json["message"]?.jsonPrimitive?.content
            val dataElement = json["data"]
            val detail = when {
                dataElement == null || dataElement is JsonNull -> null
                else -> StudentDetailParser.parse(dataElement)
            }
            StudentDetailResponse(success = success, data = detail, message = message)
        } catch (e: Exception) {
            StudentDetailResponse(
                success = false,
                data = null,
                message = e.message ?: "Failed to parse student profile"
            )
        }
    }

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
