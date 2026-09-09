package com.example.myeduapp.data.repository

import com.example.myeduapp.core.network.FeeApi
import com.example.myeduapp.data.model.FeeDue
import com.example.myeduapp.data.model.FeePayment
import com.example.myeduapp.core.datastore.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class FeeRepository {
    private val api = FeeApi()

    suspend fun getStudentDues(studentId: Int): Result<List<FeeDue>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getStudentDues(token, studentId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStudentPayments(studentId: Int): Result<List<FeePayment>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getStudentPayments(token, studentId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
