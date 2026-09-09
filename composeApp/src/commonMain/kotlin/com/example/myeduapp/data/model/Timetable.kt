package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TimetableSlot(
    val id: Int? = null,
    val day: String,
    val start_time: String,
    val end_time: String,
    val subject: String,
    val teacher: String? = null,
    val room: String? = null
)

@Serializable
data class TimetableResponse(
    val success: Boolean,
    val data: List<TimetableSlot> = emptyList()
)
