package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    SUPER_ADMIN,
    BRANCH_ADMIN,
    TEACHER,
    STAFF,
    ACCOUNTANT,
    STUDENT,
    PARENT;

    companion object {
        fun fromString(role: String?): UserRole {
            return when (role?.lowercase()) {
                "superadmin" -> SUPER_ADMIN
                "branchadmin" -> BRANCH_ADMIN
                "teacher" -> TEACHER
                "staff" -> STAFF
                "accountant" -> ACCOUNTANT
                "student" -> STUDENT
                "parent" -> PARENT
                else -> STUDENT // Default to safest restricted experience
            }
        }
    }
}
