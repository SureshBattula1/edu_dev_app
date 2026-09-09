package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.LoginResponse
import com.example.myeduapp.data.model.MeResponse
import com.example.myeduapp.data.model.User
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AuthApi {
    private val client = ApiClient.client

    suspend fun login(identifier: String, password: String): LoginResponse {
        return client.post(ApiConfig.LOGIN) {
            setBody(mapOf("email" to identifier, "password" to password))
        }.body()
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
    ): LoginResponse {
        return client.post(ApiConfig.REGISTER) {
            setBody(mapOf(
                "first_name" to firstName,
                "last_name" to lastName,
                "email" to email,
                "phone" to (phone ?: ""),
                "password" to password,
                "password_confirmation" to passwordConfirmation,
                "role" to role,
                "branch_id" to branchId
            ))
        }.body()
    }

    suspend fun getMe(token: String): User {
        val response: MeResponse = client.get(ApiConfig.ME) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
        return response.data ?: throw IllegalStateException("No user data returned")
    }

    suspend fun logout(token: String) {
        client.post(ApiConfig.LOGOUT) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    suspend fun updateProfile(token: String, firstName: String, lastName: String, phone: String?): User {
        val response: LoginResponse = client.put(ApiConfig.UPDATE_PROFILE) {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(mapOf(
                "first_name" to firstName,
                "last_name" to lastName,
                "phone" to (phone ?: "")
            ))
        }.body()
        return response.user ?: throw IllegalStateException("No user data returned")
    }

    suspend fun changePassword(token: String, currentPassword: String, newPassword: String, newPasswordConfirmation: String) {
        client.put(ApiConfig.CHANGE_PASSWORD) {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(mapOf(
                "current_password" to currentPassword,
                "password" to newPassword,
                "password_confirmation" to newPasswordConfirmation
            ))
        }
    }
}
