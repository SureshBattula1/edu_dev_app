package com.example.myeduapp.data.repository

import com.example.myeduapp.core.network.StudentApi
import com.example.myeduapp.data.model.Student
import com.example.myeduapp.data.model.StudentDetail
import com.example.myeduapp.data.model.User
import com.example.myeduapp.data.model.toStudent
import com.example.myeduapp.data.model.toStudentSeed
import com.example.myeduapp.core.datastore.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class StudentRepository {
    private val api = StudentApi()

    suspend fun resolveStudentForUser(user: User): Result<Student> = withContext(Dispatchers.IO) {
        val studentId = user.user_type_id?.toString()
        if (studentId != null) {
            getStudentDetail(studentId).onSuccess { detail ->
                return@withContext Result.success(detail.toStudent())
            }
        }
        getStudentDetailByUserId(user.id).onSuccess { detail ->
            return@withContext Result.success(detail.toStudent())
        }
        Result.success(user.toStudentSeed())
    }

    suspend fun getStudentDetailByUserId(userId: Int): Result<StudentDetail> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getStudentByUserId(token, userId)
            val data = response.data
            if (!response.success || data == null) {
                return@withContext Result.failure(
                    Exception(response.message ?: "Student not found")
                )
            }
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStudentDetail(studentId: String): Result<StudentDetail> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getStudentDetail(token, studentId)
            val data = response.data
            if (!response.success || data == null) {
                return@withContext Result.failure(
                    Exception(response.message ?: "Student not found")
                )
            }
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
