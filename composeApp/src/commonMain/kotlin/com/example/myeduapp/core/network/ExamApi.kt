package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.Exam
import com.example.myeduapp.data.model.ExamResult
import com.example.myeduapp.data.model.ApiResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class ResultsData(val data: List<ExamResult>)

class ExamApi {
    private val client = ApiClient.client

    suspend fun getUpcomingExams(token: String): List<Exam> {
        val response: ApiResponse<List<Exam>> = client.get("dashboard/upcoming-exams") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
        return response.data ?: emptyList()
    }

    suspend fun getStudentResults(token: String, studentId: Int): List<ExamResult> {
        val response: ApiResponse<List<ExamResult>> = client.get("dashboard/student-results") {
            parameter("student_id", studentId)
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
        return response.data ?: emptyList()
    }
}
