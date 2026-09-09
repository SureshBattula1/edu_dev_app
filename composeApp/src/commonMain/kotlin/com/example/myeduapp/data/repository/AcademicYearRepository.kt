package com.example.myeduapp.data.repository

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.network.AcademicYearApi
import com.example.myeduapp.data.model.AcademicYear
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class AcademicYearRepository {
    private val api = AcademicYearApi()

    suspend fun getAcademicYears(includePast: Boolean = true): Result<List<AcademicYear>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getAcademicYears(token, includePast)
            if (!response.success) {
                return@withContext Result.failure(Exception(response.message ?: "Failed to load academic years"))
            }
            Result.success(sortYearsForFilter(response.data))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentAcademicYear(): Result<AcademicYear> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getCurrentAcademicYear(token)
            val year = response.data ?: return@withContext Result.failure(Exception("No current academic year"))
            Result.success(year)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Loads all active academic years for filter dropdowns. */
    suspend fun loadFilterOptions(): Result<List<AcademicYear>> = withContext(Dispatchers.IO) {
        getAcademicYears(includePast = true).fold(
            onSuccess = { years ->
                if (years.isNotEmpty()) {
                    Result.success(years)
                } else {
                    getCurrentAcademicYear().map { listOf(it) }
                }
            },
            onFailure = {
                getCurrentAcademicYear().map { listOf(it) }
            }
        )
    }

    suspend fun ensureAcademicYearSelected(): Result<AcademicYear> = withContext(Dispatchers.IO) {
        val storedId = SessionManager.academicYearId
        val storedName = SessionManager.academicYearName
        if (!storedId.isNullOrBlank() && !storedName.isNullOrBlank()) {
            return@withContext Result.success(AcademicYear(id = storedId, name = storedName))
        }

        loadFilterOptions().fold(
            onSuccess = { years ->
                val selected = years.find { it.is_current } ?: years.firstOrNull()
                if (selected != null) {
                    SessionManager.setAcademicYear(selected.id, selected.name)
                    Result.success(selected)
                } else {
                    Result.failure(Exception("No academic year available"))
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    private fun sortYearsForFilter(years: List<AcademicYear>): List<AcademicYear> {
        return years.sortedWith(
            compareByDescending<AcademicYear> { it.is_current }
                .thenByDescending { it.start_date ?: "" }
        )
    }
}
