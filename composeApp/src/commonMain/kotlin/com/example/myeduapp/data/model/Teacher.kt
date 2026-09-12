package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TeacherUserBrief(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String? = null,
    val first_name: String? = null,
    val last_name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val is_active: Boolean? = null
)

@Serializable
data class TeacherBranchBrief(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String? = null,
    val name: String? = null,
    val code: String? = null
)

@Serializable
data class Teacher(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String,
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val user_id: String? = null,
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val branch_id: String? = null,
    val employee_id: String? = null,
    val designation: String? = null,
    val category_type: String? = null,
    val teacher_status: String? = null,
    val gender: String? = null,
    val class_teacher_of_grade: String? = null,
    val class_teacher_of_section: String? = null,
    val user: TeacherUserBrief? = null,
    val branch: TeacherBranchBrief? = null
) {
    val fullName: String
        get() {
            val first = user?.first_name.orEmpty()
            val last = user?.last_name.orEmpty()
            return "$first $last".trim().ifBlank { "Teacher #$id" }
        }

    val branchName: String?
        get() = branch?.name?.takeIf { it.isNotBlank() }
}

@Serializable
data class TeacherListMeta(
    val current_page: Int = 1,
    val per_page: Int = 25,
    val total: Int = 0,
    val last_page: Int = 1,
    val has_more_pages: Boolean = false
)

@Serializable
data class TeacherListResponse(
    val success: Boolean,
    val data: List<Teacher> = emptyList(),
    val meta: TeacherListMeta? = null,
    val message: String? = null
)
