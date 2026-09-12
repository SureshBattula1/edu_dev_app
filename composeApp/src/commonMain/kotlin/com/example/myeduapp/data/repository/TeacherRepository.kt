package com.example.myeduapp.data.repository

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.network.TeacherApi
import com.example.myeduapp.data.model.Teacher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class TeacherRepository {
    private val api = TeacherApi()

    suspend fun getTeachers(
        query: String? = null,
        branchId: Int? = null,
        grade: String? = null,
        section: String? = null
    ): Result<List<Teacher>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val all = mutableListOf<Teacher>()
            var page = 1
            var hasMore = true
            while (hasMore) {
                val response = api.getTeachers(
                    token = token,
                    search = query,
                    branchId = branchId,
                    grade = grade,
                    section = section,
                    page = page,
                    perPage = 100
                )
                if (!response.success && all.isEmpty()) {
                    return@withContext Result.failure(Exception(response.message ?: "Failed to load teachers"))
                }
                all.addAll(response.data)
                hasMore = response.meta?.has_more_pages == true && response.data.isNotEmpty()
                page++
            }
            Result.success(all)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
