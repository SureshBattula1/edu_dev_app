package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.ApiResponse
import com.example.myeduapp.data.model.Exam
import com.example.myeduapp.data.model.ExamResult
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.*

class ExamApi {
    private val client = ApiClient.client
    private val json = ApiClient.jsonConfig

    suspend fun getUpcomingExams(token: String): List<Exam> {
        val response: ApiResponse<List<Exam>> = client.get(ApiConfig.EXAMS_UPCOMING) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
        return response.data ?: emptyList()
    }

    /**
     * @param studentUserId users.id — exam_results.student_id references users table
     */
    suspend fun getStudentResults(token: String, studentUserId: Int): List<ExamResult> {
        val body = client.get(ApiConfig.EXAMS_RESULTS) {
            parameter("student_id", studentUserId)
            parameter("limit", 100)
            header(HttpHeaders.Authorization, "Bearer $token")
        }.bodyAsText()
        return parseStudentResults(body)
    }

    private fun parseStudentResults(body: String): List<ExamResult> {
        return try {
            val root = json.parseToJsonElement(body).jsonObject
            val data = root["data"] ?: return emptyList()
            when (data) {
                is JsonArray -> data.mapNotNull { parseExamResult(it.jsonObject) }
                else -> emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseExamResult(obj: JsonObject): ExamResult? {
        val subject = obj.string("subject") ?: return null
        val marks = obj.float("marks")
        val percentage = obj.float("percentage")
        val grade = obj.string("grade") ?: ""
        return ExamResult(
            id = obj.int("id") ?: 0,
            subject = subject,
            marks = marks ?: 0f,
            percentage = percentage,
            total_marks = obj.float("total_marks") ?: 100f,
            grade = grade,
            remarks = obj.string("remarks"),
            date = obj.string("date")
        )
    }

    private fun JsonObject.string(key: String): String? =
        this[key]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }

    private fun JsonObject.float(key: String): Float? =
        this[key]?.jsonPrimitive?.contentOrNull?.toFloatOrNull()

    private fun JsonObject.int(key: String): Int? =
        this[key]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()?.toInt()
}
