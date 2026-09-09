package com.example.myeduapp.data.repository

import com.example.myeduapp.core.network.LeaveApi
import com.example.myeduapp.data.model.LeaveRequest
import com.example.myeduapp.core.datastore.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class LeaveRepository {
    private val api = LeaveApi()

    suspend fun getStudentLeaves(studentId: Int): Result<List<LeaveRequest>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getStudentLeaves(token, studentId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun applyLeave(leave: LeaveRequest): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.applyLeave(token, leave)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
