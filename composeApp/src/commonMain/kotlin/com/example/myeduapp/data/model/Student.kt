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
    val avatar: String? = null
) {
    val full_name: String get() = "$first_name $last_name"

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
