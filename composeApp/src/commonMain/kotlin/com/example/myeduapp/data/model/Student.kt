package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Student(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String,
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val user_id: String,
    val first_name: String,
    val last_name: String,
    val admission_number: String,
    val roll_number: String? = null,
    val grade: String? = null,
    val section: String? = null,
    val current_grade: String? = null,
    val current_section: String? = null,
    val grade_label: String? = null,
    val current_grade_label: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val gender: String? = null,
    val student_status: String? = null,
    val avatar: String? = null
) {
    val full_name: String get() = "$first_name $last_name"

    val displayGrade: String? get() = grade?.takeIf { it.isNotBlank() } ?: current_grade
    val displaySection: String? get() = section?.takeIf { it.isNotBlank() } ?: current_section
    val displayGradeLabel: String? get() = grade_label ?: current_grade_label

    /** User id for attendance bulk API (matches web: user_id || id). */
    val attendanceUserId: String
        get() = user_id.takeIf { it.isNotBlank() && it != "0" } ?: id
}

@Serializable
data class StudentListMeta(
    val current_page: Int = 1,
    val per_page: Int = 25,
    val total: Int = 0,
    val last_page: Int = 1,
    val has_more_pages: Boolean = false
)

@Serializable
data class StudentResponse(
    val success: Boolean,
    val data: List<Student> = emptyList(),
    val meta: StudentListMeta? = null
)
