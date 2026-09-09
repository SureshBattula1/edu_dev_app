package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.Attendance
import com.example.myeduapp.data.model.AttendanceOverview
import com.example.myeduapp.data.model.ApiResponse
import com.example.myeduapp.data.model.ClassAttendanceResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AttendanceApi {
    private val client = ApiClient.client

    suspend fun getStudentAttendance(token: String, userId: Int): List<Attendance> {
        val response: ApiResponse<List<Attendance>> = client.get("${ApiConfig.ATTENDANCE_STUDENT}/$userId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
        return response.data ?: emptyList()
    }

    suspend fun getStudentOverview(token: String, userId: Int): AttendanceOverview {
        val response: ApiResponse<AttendanceOverview> = client.get("${ApiConfig.ATTENDANCE_STUDENT}/$userId/overview") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
        return response.data ?: throw Exception(response.message ?: "Failed to load attendance overview")
    }

    /** Fetch all students with their attendance records for a specific class and date. */
    suspend fun getClassAttendance(token: String, classId: Int, date: String): ClassAttendanceResponse {
        return client.get("${ApiConfig.ATTENDANCE_CLASS}/$classId") {
            parameter("date", date)
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun submitAttendance(token: String, classId: Int, date: String, records: List<Attendance>): ApiResponse<String> {
        return client.post(ApiConfig.ATTENDANCE_SUBMIT) {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(mapOf(
                "class_id" to classId,
                "date" to date,
                "attendance" to records
            ))
        }.body()
    }
}
