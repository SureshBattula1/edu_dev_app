package com.example.myeduapp.data.repository

import com.example.myeduapp.core.network.CommunicationApi
import com.example.myeduapp.core.network.UploadApi
import com.example.myeduapp.core.platform.PickedDocument
import com.example.myeduapp.data.model.Notification
import com.example.myeduapp.data.model.NotificationReceipts
import com.example.myeduapp.data.model.Holiday
import com.example.myeduapp.data.model.Announcement
import com.example.myeduapp.data.model.AssignmentAttachment
import com.example.myeduapp.data.model.BroadcastNotificationBody
import com.example.myeduapp.data.model.BroadcastNotificationResult
import com.example.myeduapp.core.datastore.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

data class NotificationsPage(
    val items: List<Notification>,
    val page: Int,
    val lastPage: Int,
    val hasMore: Boolean
)

class CommunicationRepository {
    private val api = CommunicationApi()
    private val uploadApi = UploadApi()

    suspend fun getNotifications(unreadOnly: Boolean = false, limit: Int = 20): Result<List<Notification>> =
        getNotificationsPage(status = if (unreadOnly) "unread" else "all", perPage = limit).map { it.items }

    suspend fun getNotificationsPage(
        status: String = "all",
        type: String? = null,
        source: String? = null,
        period: String? = null,
        page: Int = 1,
        perPage: Int = 20
    ): Result<NotificationsPage> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getNotifications(
                token = token,
                status = status,
                perPage = perPage,
                page = page,
                type = type,
                source = source,
                period = period
            )
            if (!response.success) {
                Result.failure(Exception(response.message ?: "Failed to load notifications"))
            } else {
                val meta = response.meta
                Result.success(
                    NotificationsPage(
                        items = response.data,
                        page = meta?.current_page ?: page,
                        lastPage = meta?.last_page ?: 1,
                        hasMore = meta?.has_more_pages ?: ((meta?.current_page ?: page) < (meta?.last_page ?: 1))
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotificationReceipts(groupKey: String): Result<NotificationReceipts> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getNotificationReceipts(token, groupKey)
            val data = response.data
            if (!response.success || data == null) {
                Result.failure(Exception(response.message ?: "Could not load views"))
            } else {
                Result.success(data)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.markNotificationRead(token, id)
            if (response.success) Result.success(Unit)
            else Result.failure(Exception(response.message ?: "Failed to mark as read"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAllAsRead(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.markAllNotificationsRead(token)
            if (response.success) Result.success(Unit)
            else Result.failure(Exception(response.message ?: "Failed to mark all as read"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun broadcastNotification(body: BroadcastNotificationBody): Result<BroadcastNotificationResult> =
        withContext(Dispatchers.IO) {
            try {
                val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
                val response = api.broadcastNotification(token, body)
                val data = response.data
                if (!response.success || data == null) {
                    Result.failure(Exception(response.message ?: "Failed to send notification"))
                } else {
                    Result.success(data)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getSentNotifications(): Result<List<Notification>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = api.getSentNotifications(token)
            if (!response.success) {
                Result.failure(Exception(response.message ?: "Failed to load sent notifications"))
            } else {
                Result.success(response.data)
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
