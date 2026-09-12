package com.example.myeduapp.data.repository

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.network.BranchApi
import com.example.myeduapp.data.model.BranchOption
import com.example.myeduapp.data.model.FilterOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class BranchRepository {
    private val api = BranchApi()

    suspend fun getAccessibleBranches(): Result<List<BranchOption>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getAccessibleBranches(token)
            if (!response.success) {
                return@withContext Result.failure(Exception("Failed to load branches"))
            }
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBranchFilterOptions(): Result<List<FilterOption>> =
        getAccessibleBranches().map { branches ->
            branches.map { FilterOption(value = it.id, label = it.name) }
        }
}
