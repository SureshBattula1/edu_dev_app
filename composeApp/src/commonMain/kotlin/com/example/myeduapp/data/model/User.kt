package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val first_name: String = "",
    val last_name: String = "",
    val email: String,
    val phone: String? = null,
    val role: String,
    val branch_id: Int? = null,
    val school_id: Int? = null,
    val company_id: Int? = null,
    val avatar: String? = null,
    val is_active: Boolean = true,
    val permissions: List<String> = emptyList(),
    
    // Additional Profile Fields
    val employee_id: String? = null,
    val dob: String? = null,
    val gender: String? = null,
    val address: String? = null,
    val designation: String? = null,
    val department: String? = null,
    val joining_date: String? = null,
    val qualification: String? = null,
    val experience: String? = null,
    val subjects: List<String> = emptyList(),
    val classes: List<String> = emptyList()
) {
    /** Full display name derived from first + last name (backend stores them separately). */
    val name: String
        get() = listOf(first_name, last_name).filter { it.isNotBlank() }.joinToString(" ")

    val userRole: UserRole
        get() = UserRole.fromString(role)
}

/** Wrapper for GET /me which returns { success, data } (unlike login/register which return { user }). */
@Serializable
data class MeResponse(
    val success: Boolean = false,
    val data: User? = null,
    val message: String? = null
)

@Serializable
data class LoginResponse(
    val success: Boolean = true,
    val message: String = "",
    val user: User? = null,
    val access_token: String? = null,
    val token_type: String? = null,
    val expires_in: String? = null
)
