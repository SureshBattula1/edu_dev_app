package com.example.myeduapp.data.repository

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.network.AssignmentApi
import com.example.myeduapp.core.network.UploadApi
import com.example.myeduapp.core.platform.PickedDocument
import com.example.myeduapp.data.model.Assignment
import com.example.myeduapp.data.model.AssignmentAttachment
import com.example.myeduapp.data.model.CreateAssignmentBody
import com.example.myeduapp.data.model.EligibleStudent
import com.example.myeduapp.data.model.SubjectOption
import com.example.myeduapp.data.model.UpdateAssignmentBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class AssignmentRepository {
    private val api = AssignmentApi()
    private val uploadApi = UploadApi()

    suspend fun getAssignments(): Result<List<Assignment>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getAssignments(token)
            if (!response.success) {
                Result.failure(Exception(response.message ?: "Failed to load assignments"))
            } else {
                Result.success(response.data)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAssignment(id: String): Result<Assignment> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getAssignment(token, id)
            val data = response.data
            if (!response.success || data == null) {
                Result.failure(Exception(response.message ?: "Failed to load assignment"))
            } else {
                Result.success(data)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAssignment(body: CreateAssignmentBody): Result<Assignment> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.createAssignment(token, body)
            val data = response.data
            if (!response.success || data == null) {
                Result.failure(Exception(response.message ?: "Failed to create assignment"))
            } else {
                Result.success(data)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAssignment(id: String, body: UpdateAssignmentBody): Result<Assignment> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.updateAssignment(token, id, body)
            val data = response.data
            if (!response.success || data == null) {
                Result.failure(Exception(response.message ?: "Failed to update assignment"))
            } else {
                Result.success(data)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadAttachment(file: PickedDocument): Result<AssignmentAttachment> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val uploaded = uploadApi.uploadDocument(token, file)
            Result.success(
                AssignmentAttachment(
                    file_name = uploaded.file_name.ifBlank { file.name },
                    original_name = file.name,
                    file_path = uploaded.file_path,
                    file_url = uploaded.file_url,
                    file_type = uploaded.file_type,
                    file_size = uploaded.file_size,
                    attachment_type = "document"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEligibleStudents(grade: String, section: String): Result<List<EligibleStudent>> =
        withContext(Dispatchers.IO) {
            try {
                val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
                val response = api.getEligibleStudents(token, grade, section, SessionManager.user?.branch_id)
                if (!response.success) {
                    Result.failure(Exception(response.message ?: "Failed to load students"))
                } else {
                    Result.success(response.data)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getSubjects(grade: String): Result<List<SubjectOption>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getSubjects(token, grade, SessionManager.user?.branch_id)
            if (!response.success) {
                Result.failure(Exception(response.message ?: "Failed to load subjects"))
            } else {
                Result.success(response.data)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
