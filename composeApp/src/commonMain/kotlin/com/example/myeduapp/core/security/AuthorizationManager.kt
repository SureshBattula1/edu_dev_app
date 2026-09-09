package com.example.myeduapp.core.security

import com.example.myeduapp.data.model.User
import com.example.myeduapp.data.model.UserRole
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.datastore.AuthState

object AuthorizationManager {
    
    private val currentUser: User?
        get() = (SessionManager.authState.value as? AuthState.Authenticated)?.user

    fun hasRole(role: UserRole): Boolean {
        return currentUser?.userRole == role
    }

    fun hasPermission(permissionSlug: String): Boolean {
        val user = currentUser ?: return false
        
        // SuperAdmin often has bypass, but per backend comments, 
        // they follow permissions too. However, we'll keep a role check
        // for global system administration that might not have a specific slug.
        if (user.userRole == UserRole.SUPER_ADMIN) return true
        
        return user.permissions.contains(permissionSlug)
    }

    fun hasAnyPermission(permissionSlugs: List<String>): Boolean {
        if (currentUser?.userRole == UserRole.SUPER_ADMIN) return true
        return permissionSlugs.any { hasPermission(it) }
    }

    fun hasAllPermissions(permissionSlugs: List<String>): Boolean {
        if (currentUser?.userRole == UserRole.SUPER_ADMIN) return true
        return permissionSlugs.all { hasPermission(it) }
    }

    /**
     * Checks if the user can access a feature based on role and permissions.
     */
    fun canAccessFeature(featurePermission: String, allowedRoles: List<UserRole> = emptyList()): Boolean {
        val user = currentUser ?: return false
        
        // If specific roles are required and user doesn't have it, check permission as fallback
        if (allowedRoles.isNotEmpty() && allowedRoles.contains(user.userRole)) {
            return true
        }
        
        return hasPermission(featurePermission)
    }

    /**
     * Checks if an action is within the user's scope (Branch/Company).
     */
    fun isWithinScope(branchId: Int?, companyId: Int? = null): Boolean {
        val user = currentUser ?: return false
        
        // SuperAdmin has global scope
        if (user.userRole == UserRole.SUPER_ADMIN) return true
        
        // Check company scope if provided
        if (companyId != null && user.company_id != companyId) return false
        
        // BranchAdmin has scope of their branch and children (handled by backend usually)
        // For frontend, we just check if it's their branch or they have cross-branch access
        if (branchId != null) {
            if (user.branch_id == branchId) return true
            
            // Check for cross-branch permission
            return hasAnyPermission(listOf("system.cross_branch_access", "system.view_all_branches"))
        }
        
        return true
    }
}
