package com.example.myeduapp.data.repository

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.network.FeeApi
import com.example.myeduapp.data.model.FeeDue
import com.example.myeduapp.data.model.FeePayment
import com.example.myeduapp.data.model.StudentFeesData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class FeeRepository {
    private val api = FeeApi()

    /**
     * @param studentUserId users.id from the student record
     */
    suspend fun getStudentFees(studentUserId: Int): Result<StudentFeesData> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            Result.success(api.getStudentFeesData(token, studentUserId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStudentDues(studentUserId: Int): Result<List<FeeDue>> = withContext(Dispatchers.IO) {
        getStudentFees(studentUserId).map { it.dues }
    }

    suspend fun getStudentPayments(studentUserId: Int): Result<List<FeePayment>> = withContext(Dispatchers.IO) {
        getStudentFees(studentUserId).map { it.payments }
    }
}
