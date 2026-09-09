package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Assignment(
    val id: Int,
    val title: String,
    val description: String? = null,
    val class_name: String,
    val section: String,
    val subject: String,
    val due_date: String,
    val submission_count: Int = 0,
    val status: String = "Active"
)

@Serializable
data class AssignmentResponse(
    val success: Boolean,
    val data: List<Assignment> = emptyList()
)
