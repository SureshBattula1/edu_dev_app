package com.example.myeduapp.features.teacher.profile

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val updateSuccess: Boolean = false
)

class EditProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(
        SessionManager.user.let { user ->
            EditProfileUiState(
                firstName = user?.first_name.orEmpty(),
                lastName = user?.last_name.orEmpty(),
                phone = user?.phone.orEmpty()
            )
        }
    )
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun onFirstNameChange(value: String) {
        _uiState.update { it.copy(firstName = value, errorMessage = null) }
    }

    fun onLastNameChange(value: String) {
        _uiState.update { it.copy(lastName = value, errorMessage = null) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value, errorMessage = null) }
    }

    fun saveProfile() {
        val state = _uiState.value
        if (state.firstName.isBlank() || state.lastName.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.updateProfile(
                state.firstName,
                state.lastName,
                state.phone.ifBlank { null }
            )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    updateSuccess = result.isSuccess,
                    errorMessage = if (result.isFailure) {
                        result.exceptionOrNull()?.message ?: "Failed to update profile"
                    } else null
                )
            }
        }
    }
}
