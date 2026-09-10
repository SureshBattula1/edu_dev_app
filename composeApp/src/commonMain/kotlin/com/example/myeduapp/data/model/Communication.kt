package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Holiday(
    val id: Int,
    val name: String,
    val date: String,
    val end_date: String? = null,
    val description: String? = null
)

@Serializable
data class Announcement(
    val id: Int,
    val title: String,
    val content: String,
    val author: String? = null,
    val published_at: String
)

@Serializable
data class CommunicationResponse(
    val success: Boolean,
    val notifications: List<Notification> = emptyList(),
    val holidays: List<Holiday> = emptyList(),
    val announcements: List<Announcement> = emptyList()
)
