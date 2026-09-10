package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String,
    val title: String,
    val message: String,
    val type: String = "Info",
    val date: String? = null,
    val created_at: String? = null,
    @Serializable(with = JsonFlexibleBooleanSerializer::class)
    val is_read: Boolean = false,
    val read_at: String? = null,
    val source: String? = null,
    val event: String? = null,
    val priority: String? = null,
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val assignment_id: String = "",
    val group_key: String? = null,
    val action_url: String? = null,
    @Serializable(with = JsonFlexibleBooleanSerializer::class)
    val can_view_receipts: Boolean = false,
    val description: String? = null,
    val optional_description: String? = null,
    val attachments: List<AssignmentAttachment> = emptyList(),
    val grade: String? = null,
    val section: String? = null,
    val audience: String? = null,
    @Serializable(with = JsonFlexibleNullableIntSerializer::class)
    val student_count: Int? = null,
    val sent_at: String? = null,
    val status: String? = null
) {
    val displayDate: String get() = date ?: created_at.orEmpty()
    val isRead: Boolean get() = is_read || !read_at.isNullOrBlank()
}

@Serializable
data class PageMeta(
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val current_page: Int = 1,
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val per_page: Int = 20,
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val total: Int = 0,
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val last_page: Int = 1,
    @Serializable(with = JsonFlexibleBooleanSerializer::class)
    val has_more_pages: Boolean = false
)

@Serializable
data class NotificationsPageResponse(
    val success: Boolean,
    val data: List<Notification> = emptyList(),
    val meta: PageMeta? = null,
    val message: String? = null
)

@Serializable
data class NotificationViewer(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val user_id: String = "",
    val name: String = "",
    val role: String? = null,
    val audience: String? = null,
    @Serializable(with = JsonFlexibleBooleanSerializer::class)
    val viewed: Boolean = false,
    val viewed_at: String? = null
)

@Serializable
data class NotificationReceipts(
    val group_key: String? = null,
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val total: Int = 0,
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val viewed: Int = 0,
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val pending: Int = 0,
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val percent: Int = 0,
    val viewers: List<NotificationViewer> = emptyList()
)

@Serializable
data class NotificationReceiptsResponse(
    val success: Boolean,
    val data: NotificationReceipts? = null,
    val message: String? = null
)

@Serializable
data class BroadcastNotificationBody(
    val title: String,
    val description: String,
    val optional_description: String? = null,
    val grade: String,
    val section: String,
    val audience_mode: String,
    val student_ids: List<String> = emptyList(),
    val attachments: List<AssignmentAttachment> = emptyList(),
    val branch_id: Int? = null
)

@Serializable
data class BroadcastNotificationResult(
    val group_key: String? = null,
    val grade: String? = null,
    val section: String? = null,
    val class_name: String? = null,
    @kotlinx.serialization.SerialName("class")
    val classLabel: String? = null,
    @Serializable(with = JsonFlexibleNullableIntSerializer::class)
    val student_count: Int? = null,
    val sent_at: String? = null
)

@Serializable
data class BroadcastNotificationResponse(
    val success: Boolean,
    val data: BroadcastNotificationResult? = null,
    val message: String? = null
)

@Serializable
data class SentNotificationsResponse(
    val success: Boolean,
    val data: List<Notification> = emptyList(),
    val message: String? = null
)