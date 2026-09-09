package com.example.myeduapp.data.repository

import com.example.myeduapp.core.network.AuthApi
import com.example.myeduapp.data.model.LoginResponse
import com.example.myeduapp.data.model.User
import com.example.myeduapp.core.datastore.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class AuthRepository {
    private val authApi = AuthApi()

    /** Login with email OR phone + password. */
    suspend fun login(identifier: String, password: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            println("AuthRepository: Attempting login for $identifier")
            val response = authApi.login(identifier.trim(), password)
            if (response.success && response.user != null && response.access_token != null) {
                println("AuthRepository: Login SUCCESS for ${response.user.email}")
                SessionManager.startSession(response.user, response.access_token)
                Result.success(response)
            } else {
                println("AuthRepository: Login FAILED - ${response.message}")
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            println("AuthRepository: Login ERROR - ${e.message}")
            Result.failure(Exception(e.toFriendlyMessage()))
        }
    }

    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        phone: String?,
        password: String,
        passwordConfirmation: String,
        role: String,
        branchId: Int
    ): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            println("AuthRepository: Attempting registration for $email")
            val response = authApi.register(
                firstName.trim(), lastName.trim(), email.trim(),
                phone, password, passwordConfirmation, role, branchId
            )
            if (response.success && response.user != null && response.access_token != null) {
                println("AuthRepository: Registration SUCCESS for ${response.user.email}")
                SessionManager.startSession(response.user, response.access_token)
                Result.success(response)
            } else {
                println("AuthRepository: Registration FAILED - ${response.message}")
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            println("AuthRepository: Registration ERROR - ${e.message}")
            Result.failure(Exception(e.toFriendlyMessage()))
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.token
            if (token != null) {
                authApi.logout(token)
            }
        } catch (e: Exception) {
            // Ignore logout failure, we clear local session anyway
        } finally {
            SessionManager.clearSession()
        }
    }

    /**
     * Runs at app start. If a token is stored, it is validated against GET /me.
     * App.kt observes AuthState and redirects the user based on role.
     */
    suspend fun checkAuth() = withContext(Dispatchers.IO) {
        val token = SessionManager.token
        if (token != null) {
            try {
                println("AuthRepository: Checking authentication status...")
                val user = authApi.getMe(token)
                println("AuthRepository: Auth check SUCCESS for ${user.email}")
                SessionManager.startSession(user, token)
            } catch (e: Exception) {
                println("AuthRepository: Auth check FAILED/EXPIRED - ${e.message}")
                SessionManager.clearSession()
            }
        } else {
            println("AuthRepository: No token found, user is guest")
            SessionManager.clearSession()
        }
    }

    suspend fun updateProfile(firstName: String, lastName: String, phone: String?): Result<User> = withContext(Dispatchers.IO) {
        val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
        try {
            val user = authApi.updateProfile(token, firstName.trim(), lastName.trim(), phone)
            SessionManager.startSession(user, token) // Refresh local session with new user data
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(e.toFriendlyMessage()))
        }
    }

    suspend fun changePassword(old: String, new: String, confirm: String): Result<Unit> = withContext(Dispatchers.IO) {
        val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))
        try {
            authApi.changePassword(token, old, new, confirm)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.toFriendlyMessage()))
        }
    }
}

/** Maps raw Ktor/HTTP messages to human-friendly, role-aware errors shown in the UI. */
private fun Throwable.toFriendlyMessage(): String {
    val raw = message ?: "Something went wrong. Please try again."
    return when {
        raw.contains("401") -> "Invalid credentials. Please check your email/phone and password."
        raw.contains("403") -> "Your account is inactive. Please contact the administrator."
        raw.contains("422") -> "Validation failed. Please check all the fields and try again."
        raw.contains("429") -> "Too many attempts. Please wait a minute and try again."
        raw.contains("timeout", ignoreCase = true) ||
            raw.contains("timed out", ignoreCase = true) ||
            raw.contains("connection", ignoreCase = true) ||
            raw.contains("refused", ignoreCase = true) ->
            "Cannot reach the server. Check your internet connection."
        raw.contains("Unauthenticated", ignoreCase = true) -> "Your session expired. Please log in again."
        else -> raw
    }
}
