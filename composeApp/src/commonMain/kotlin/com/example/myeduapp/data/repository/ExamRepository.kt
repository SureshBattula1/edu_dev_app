package com.example.myeduapp.data.repository

import com.example.myeduapp.core.network.ExamApi
import com.example.myeduapp.data.model.Exam
import com.example.myeduapp.data.model.ExamResult
import com.example.myeduapp.core.datastore.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ExamRepository {
    private val api = ExamApi()

    suspend fun getUpcomingExams(): Result<List<Exam>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getUpcomingExams(token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStudentResults(studentId: Int): Result<List<ExamResult>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getStudentResults(token, studentId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
