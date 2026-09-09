package com.example.myeduapp.data.repository

import com.example.myeduapp.core.network.DashboardApi
import com.example.myeduapp.data.model.DashboardResponse
import com.example.myeduapp.core.datastore.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class DashboardRepository {
    private val dashboardApi = DashboardApi()

    suspend fun getDashboard(): Result<DashboardResponse> = withContext(Dispatchers.IO) {
        val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
        try {
            val data = dashboardApi.getDashboardData(token)
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
