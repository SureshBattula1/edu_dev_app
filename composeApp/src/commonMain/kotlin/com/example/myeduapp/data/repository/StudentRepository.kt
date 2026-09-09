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
        section: String? = null
    ): Result<List<Student>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getStudents(token, query, grade, section)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStudentsByClass(grade: String, section: String): Result<List<Student>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getStudents(token, null, grade, section)
            Result.success(response.data) 
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
