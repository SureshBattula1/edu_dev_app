package com.example.myeduapp.data.repository

import com.example.myeduapp.core.network.StudentApi
import com.example.myeduapp.data.model.Student
import com.example.myeduapp.core.datastore.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class StudentRepository {
    private val api = StudentApi()

    suspend fun getStudents(query: String? = null): Result<List<Student>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getStudents(token, query)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStudentsByClass(classId: Int): Result<List<Student>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getStudents(token, null) // In a real app, backend should support class_id filter
            // For now, filter locally if backend doesn't support specific class endpoint
            val filtered = response.data.filter { true } // Placeholder for filtering logic
            Result.success(response.data) 
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
