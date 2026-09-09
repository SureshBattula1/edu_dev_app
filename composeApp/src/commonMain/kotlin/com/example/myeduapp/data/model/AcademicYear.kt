package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AcademicYear(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String,
    val name: String,
    val start_date: String? = null,
    val end_date: String? = null,
    val is_current: Boolean = false,
    val is_active: Boolean = true,
    val description: String? = null
)

@Serializable
data class AcademicYearListResponse(
    val success: Boolean,
    val data: List<AcademicYear> = emptyList(),
    val message: String? = null
)

@Serializable
data class AcademicYearResponse(
    val success: Boolean,
    val data: AcademicYear? = null,
    val message: String? = null
)
