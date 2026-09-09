package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FilterOption(
    val value: String,
    val label: String
)

@Serializable
data class FilterOptionListResponse(
    val success: Boolean,
    val data: List<FilterOption> = emptyList()
)

@Serializable
data class GradeOption(
    val value: String,
    val label: String,
    val description: String? = null,
    val students_count: Int = 0,
    val sections: List<String> = emptyList(),
    val classes_count: Int = 0,
    val is_active: Boolean = true
)

@Serializable
data class GradeListResponse(
    val success: Boolean,
    val data: List<GradeOption> = emptyList()
)
