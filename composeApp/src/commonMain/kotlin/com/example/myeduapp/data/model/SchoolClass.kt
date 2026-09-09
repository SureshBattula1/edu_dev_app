package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SchoolClass(
    val id: Int,
    val grade: String? = null,
    val name: String? = null,
    val class_name: String? = null,
    val section: String,
    val subject: String? = null,
    val student_count: Int = 0,
    val current_strength: Int? = null,
    val academic_year: String? = null,
    val schedule: String? = null,
    val room: String? = null,
    val room_number: String? = null,
    val class_teacher_id: Int? = null
) {
    val displayGrade: String get() = grade ?: name ?: ""
    val displayName: String get() = class_name?.takeIf { it.isNotBlank() }
        ?: run {
            val g = displayGrade
            when {
                g.isNotEmpty() && section.isNotEmpty() -> "Grade $g-$section"
                g.isNotEmpty() -> "Grade $g"
                else -> section
            }
        }
    val studentCount: Int get() = if (student_count > 0) student_count else (current_strength ?: 0)
    val roomDisplay: String? get() = room ?: room_number
}

@Serializable
data class ClassResponse(
    val success: Boolean,
    val data: List<SchoolClass> = emptyList()
)
