package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SchoolClass(
    val id: Int,
    val name: String,
    val section: String,
    val subject: String? = null,
    val student_count: Int = 0,
    val academic_year: String? = null,
    val schedule: String? = null,
    val room: String? = null
)

@Serializable
data class ClassResponse(
    val success: Boolean,
    val data: List<SchoolClass> = emptyList()
)
