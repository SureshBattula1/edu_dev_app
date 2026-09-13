package com.example.myeduapp.features.teacher.profile

import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChangePasswordUiState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val updateSuccess: Boolean = false
) {
    val isFormValid: Boolean
        get() = oldPassword.isNotBlank() &&
            newPassword.length >= 6 &&
            newPassword == confirmPassword
}

class ChangePasswordViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun onOldPasswordChange(value: String) {
        _uiState.update { it.copy(oldPassword = value, errorMessage = null) }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value, errorMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    }

    fun changePassword() {
        val state = _uiState.value
        if (!state.isFormValid) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.changePassword(
                state.oldPassword,
                state.newPassword,
                state.confirmPassword
            )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    updateSuccess = result.isSuccess,
                    errorMessage = if (result.isFailure) {
                        result.exceptionOrNull()?.message ?: "Failed to change password"
                    } else null
                )
            }
        }
    }
}
