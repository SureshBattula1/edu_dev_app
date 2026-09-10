package com.example.myeduapp.features.leaves

import com.example.myeduapp.core.security.AuthorizationManager
import com.example.myeduapp.data.model.Permission
import com.example.myeduapp.data.model.UserRole

object LeaveAccess {
    fun canApplyStudentLeave(role: UserRole): Boolean = role == UserRole.STUDENT

    fun canApplyTeacherLeave(role: UserRole): Boolean = role == UserRole.TEACHER

    fun canApproveStudentLeaves(role: UserRole): Boolean =
        role in listOf(UserRole.TEACHER, UserRole.STAFF, UserRole.BRANCH_ADMIN, UserRole.SUPER_ADMIN) ||
            AuthorizationManager.hasPermission(Permission.LEAVES_APPROVE)

    fun canApproveTeacherLeaves(role: UserRole): Boolean =
        role in listOf(UserRole.BRANCH_ADMIN, UserRole.SUPER_ADMIN) ||
            (AuthorizationManager.hasPermission(Permission.LEAVES_APPROVE) &&
                role !in listOf(UserRole.TEACHER, UserRole.STUDENT))
}
