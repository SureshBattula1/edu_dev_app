package com.example.myeduapp.data.repository

import com.example.myeduapp.core.network.AssignmentApi
import com.example.myeduapp.data.model.Assignment
import com.example.myeduapp.core.datastore.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class AssignmentRepository {
    private val api = AssignmentApi()

    suspend fun getAssignments(): Result<List<Assignment>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getAssignments(token)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
