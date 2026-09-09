package com.example.myeduapp.data.repository

import com.example.myeduapp.core.network.StudentApi
import com.example.myeduapp.data.model.Student
import com.example.myeduapp.core.datastore.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class StudentRepository {
    private val api = StudentApi()

    suspend fun getStudents(
        query: String? = null,
        grade: String? = null,
        section: String? = null,
        academicYearId: String? = SessionManager.academicYearId
    ): Result<List<Student>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getStudents(token, query, grade, section, academicYearId)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStudentsByClass(
        grade: String,
        section: String,
        academicYearId: String? = SessionManager.academicYearId
    ): Result<List<Student>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val allStudents = mutableListOf<Student>()
            var page = 1
            var hasMore = true
            while (hasMore) {
                val response = api.getStudents(
                    token = token,
                    grade = grade,
                    section = section,
                    academicYearId = academicYearId,
                    page = page,
                    perPage = 100
                )
                allStudents.addAll(response.data)
                hasMore = response.meta?.has_more_pages == true && response.data.isNotEmpty()
                page++
            }
            Result.success(
                allStudents
                    .filter { it.attendanceUserId.isNotBlank() && it.attendanceUserId != "0" }
                    .distinctBy { it.attendanceUserId }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
