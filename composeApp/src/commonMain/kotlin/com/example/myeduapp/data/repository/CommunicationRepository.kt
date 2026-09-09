package com.example.myeduapp.data.repository

import com.example.myeduapp.core.network.CommunicationApi
import com.example.myeduapp.data.model.Notification
import com.example.myeduapp.data.model.Holiday
import com.example.myeduapp.data.model.Announcement
import com.example.myeduapp.core.datastore.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class CommunicationRepository {
    private val api = CommunicationApi()

    suspend fun getNotifications(): Result<List<Notification>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getNotifications(token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAnnouncements(): Result<List<Announcement>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getAnnouncements(token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHolidays(): Result<List<Holiday>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getHolidays(token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
