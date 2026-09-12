package com.example.myeduapp.data.repository

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.network.ClassApi
import com.example.myeduapp.data.model.FilterOption
import com.example.myeduapp.data.model.GradeOption
import com.example.myeduapp.data.model.SchoolClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ClassRepository {
    private val api = ClassApi()

    private val sessionBranchId: Int? get() = SessionManager.user?.branch_id

    private fun resolveBranchId(overrideBranchId: Int?): Int? = overrideBranchId ?: sessionBranchId

    suspend fun getMyClasses(
        academicYear: String? = null,
        branchId: Int? = null
    ): Result<List<SchoolClass>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val year = academicYear ?: SessionManager.academicYearName
            val response = api.getMyClasses(token, resolveBranchId(branchId), year)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGrades(branchId: Int? = null): Result<List<GradeOption>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getGrades(token, resolveBranchId(branchId))
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSections(
        grade: String? = null,
        branchId: Int? = null
    ): Result<List<FilterOption>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getSections(token, grade, resolveBranchId(branchId))
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
