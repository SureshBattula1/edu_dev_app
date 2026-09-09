package com.example.myeduapp.data.repository

import com.example.myeduapp.core.network.AttendanceApi
import com.example.myeduapp.data.model.Attendance
import com.example.myeduapp.data.model.AttendanceOverview
import com.example.myeduapp.data.model.ClassAttendanceResponse
import com.example.myeduapp.core.datastore.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class AttendanceRepository {
    private val api = AttendanceApi()

    suspend fun getStudentAttendance(userId: Int): Result<List<Attendance>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getStudentAttendance(token, userId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStudentOverview(userId: Int): Result<AttendanceOverview> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getStudentOverview(token, userId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClassAttendance(classId: Int, date: String): Result<ClassAttendanceResponse> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getClassAttendance(token, classId, date)
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception("Failed to load class attendance"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitAttendance(classId: Int, date: String, records: List<Attendance>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.submitAttendance(token, classId, date, records)
            if (response.success) {
                Result.success(response.data ?: "Attendance submitted successfully")
            } else {
                Result.failure(Exception(response.message ?: "Failed to submit attendance"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
