package com.example.myeduapp.core.datastore

import com.example.myeduapp.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

object SessionManager {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val secureStorage = getSecureStorage()
    private val json = Json { ignoreUnknownKeys = true }

    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER = "auth_user"
    private const val KEY_ACADEMIC_YEAR_ID = "academic_year_id"
    private const val KEY_ACADEMIC_YEAR_NAME = "academic_year_name"

    private var _token: String? = null
    val token: String? get() = _token

    private var _user: User? = null
    val user: User? get() = _user

    private var _academicYearId: String? = null
    val academicYearId: String? get() = _academicYearId

    private var _academicYearName: String? = null
    val academicYearName: String? get() = _academicYearName

    init {
        loadSession()
    }

    private fun loadSession() {
        _token = secureStorage.getString(KEY_TOKEN)
        val userJson = secureStorage.getString(KEY_USER)
        _user = userJson?.let {
            try {
                json.decodeFromString<User>(it)
            } catch (e: Exception) {
                null
            }
        }

        secureStorage.getString(KEY_ACADEMIC_YEAR_ID)?.takeIf { it.isNotBlank() }?.let { id ->
            _academicYearId = id
            _academicYearName = secureStorage.getString(KEY_ACADEMIC_YEAR_NAME)
        }

        if (_token != null && _user != null) {
            _authState.value = AuthState.Loading // Set to loading while we verify with backend
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun startSession(user: User, token: String) {
        println("SessionManager: Starting session for ${user.email}")
        _user = user
        _token = token
        
        secureStorage.saveString(KEY_TOKEN, token)
        secureStorage.saveString(KEY_USER, json.encodeToString(user))
        
        _authState.value = AuthState.Authenticated(user, token)
    }

    fun setAcademicYear(id: String, name: String) {
        _academicYearId = id
        _academicYearName = name
        secureStorage.saveString(KEY_ACADEMIC_YEAR_ID, id)
        secureStorage.saveString(KEY_ACADEMIC_YEAR_NAME, name)
    }

    fun clearSession() {
        println("SessionManager: Clearing session")
        _user = null
        _token = null
        _academicYearId = null
        _academicYearName = null
        secureStorage.clear()
        _authState.value = AuthState.Unauthenticated
    }

    fun updateLoading() {
        _authState.value = AuthState.Loading
    }

    fun setError(message: String) {
        _authState.value = AuthState.Error(message)
    }
}
