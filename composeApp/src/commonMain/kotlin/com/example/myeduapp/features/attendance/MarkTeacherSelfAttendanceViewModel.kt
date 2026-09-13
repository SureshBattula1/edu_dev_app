package com.example.myeduapp.features.attendance

import com.example.myeduapp.core.util.DateUtils
import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MarkTeacherSelfAttendanceUiState(
    val selectedDate: String = DateUtils.today(),
    val selectedStatus: String = "Present",
    val isSubmitting: Boolean = false,
    val snackbarMessage: String? = null,
    val navigateBack: Boolean = false
)

class MarkTeacherSelfAttendanceViewModel(
    private val attendanceRepository: AttendanceRepository = AttendanceRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(MarkTeacherSelfAttendanceUiState())
    val uiState: StateFlow<MarkTeacherSelfAttendanceUiState> = _uiState.asStateFlow()

    val statuses = listOf("Present", "Absent", "Late", "Half-Day", "Sick Leave", "Leave")

    fun onDateChange(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onStatusChange(status: String) {
        _uiState.update { it.copy(selectedStatus = status) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            attendanceRepository.submitTeacherSelfAttendance(state.selectedDate, state.selectedStatus)
                .onSuccess { message ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            snackbarMessage = message,
                            navigateBack = true
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            snackbarMessage = e.message ?: "Failed to submit"
                        )
                    }
                }
        }
    }

    fun consumeSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun consumeNavigateBack() {
        _uiState.update { it.copy(navigateBack = false) }
    }
}
