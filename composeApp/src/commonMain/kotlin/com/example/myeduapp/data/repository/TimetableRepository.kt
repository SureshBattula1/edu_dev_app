package com.example.myeduapp.data.repository

import com.example.myeduapp.core.network.TimetableApi
import com.example.myeduapp.data.model.TimetableSlot
import com.example.myeduapp.core.datastore.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class TimetableRepository {
    private val api = TimetableApi()

    suspend fun getTimetable(grade: String, section: String): Result<List<TimetableSlot>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getTimetableByClass(token, grade, section)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
