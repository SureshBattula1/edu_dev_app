package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LeaveRequest(
    val id: Int? = null,
    val start_date: String,
    val end_date: String,
    val leave_type: String,
    val reason: String,
    val status: String = "Pending", // Pending, Approved, Rejected
    val applied_on: String? = null
)

@Serializable
data class LeaveResponse(
    val success: Boolean,
    val data: List<LeaveRequest> = emptyList()
)
