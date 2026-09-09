package com.example.myeduapp.core.datastore

import com.example.myeduapp.data.model.User

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: User, val token: String) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
