package com.example.myeduapp.data.repository

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.network.LeaveApi
import com.example.myeduapp.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class LeaveRepository {
    private val api = LeaveApi()

    suspend fun getMyLeaves(category: LeaveCategory, userId: Int): Result<LeaveListResponse> =
        withContext(Dispatchers.IO) {
            try {
                val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
                val response = when (category) {
                    LeaveCategory.STUDENT -> api.getStudentLeaves(token, userId)
                    LeaveCategory.TEACHER -> api.getTeacherLeaves(token, userId)
                }
                if (!response.success) {
                    Result.failure(Exception(response.message ?: "Failed to load leaves"))
                } else {
                    Result.success(response)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getLeavesForApproval(
        category: LeaveCategory,
        status: String? = "Pending",
        page: Int = 1
    ): Result<LeaveListResponse> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getLeaves(token, category.apiType, status, page)
            if (!response.success) {
                Result.failure(Exception(response.message ?: "Failed to load leaves"))
            } else {
                Result.success(response)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun applyStudentLeave(body: CreateStudentLeaveBody): Result<String> =
        submitLeave { token -> api.applyStudentLeave(token, body) }

    suspend fun applyTeacherLeave(body: CreateTeacherLeaveBody): Result<String> =
        submitLeave { token -> api.applyTeacherLeave(token, body) }

    suspend fun approveLeave(leaveId: Int, category: LeaveCategory, remarks: String? = null): Result<String> =
        updateLeaveStatus(leaveId, category, "Approved", remarks)

    suspend fun rejectLeave(leaveId: Int, category: LeaveCategory, remarks: String? = null): Result<String> =
        updateLeaveStatus(leaveId, category, "Rejected", remarks)

    suspend fun cancelLeave(leaveId: Int, category: LeaveCategory): Result<String> =
        updateLeaveStatus(leaveId, category, "Cancelled", null)

    private suspend fun updateLeaveStatus(
        leaveId: Int,
        category: LeaveCategory,
        status: String,
        remarks: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.updateLeave(
                token,
                leaveId,
                category.apiType,
                UpdateLeaveBody(status = status, remarks = remarks)
            )
            if (response.success) {
                Result.success(response.message ?: "Leave updated")
            } else {
                Result.failure(Exception(response.message ?: "Failed to update leave"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun submitLeave(
        call: suspend (String) -> LeaveActionResponse
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = call(token)
            if (response.success) {
                Result.success(response.message ?: "Leave submitted")
            } else {
                Result.failure(Exception(response.message ?: "Failed to submit leave"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
