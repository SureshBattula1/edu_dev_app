package com.example.myeduapp.data.repository

import com.example.myeduapp.core.network.ClassApi
import com.example.myeduapp.data.model.SchoolClass
import com.example.myeduapp.core.datastore.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ClassRepository {
    private val api = ClassApi()

    suspend fun getMyClasses(): Result<List<SchoolClass>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getMyClasses(token)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBranchClasses(): Result<List<SchoolClass>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getBranchClasses(token)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
