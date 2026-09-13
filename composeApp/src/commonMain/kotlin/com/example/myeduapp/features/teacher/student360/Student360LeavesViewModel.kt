package com.example.myeduapp.features.teacher.student360

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.CreateStudentLeaveBody
import com.example.myeduapp.data.model.LeaveCategory
import com.example.myeduapp.data.model.LeaveListResponse
import com.example.myeduapp.data.repository.LeaveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class Student360LeavesUiState(
    val response: LeaveListResponse? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val statusFilter: String? = null,
    val showApplySheet: Boolean = false,
    val isSubmitting: Boolean = false,
    val snackbarMessage: String? = null
)

class Student360LeavesViewModel(
    private val userId: Int?,
    private val branchId: Int?,
    private val leaveRepository: LeaveRepository = LeaveRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(Student360LeavesUiState())
    val uiState: StateFlow<Student360LeavesUiState> = _uiState.asStateFlow()

    init {
        loadLeaves()
    }

    fun loadLeaves() {
        val id = userId
        if (id == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Student account is not linked. Cannot load leaves."
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            leaveRepository.getMyLeaves(LeaveCategory.STUDENT, id)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(response = response, isLoading = false, error = null)
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            error = err.message ?: "Failed to load leaves",
                            response = null,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onStatusFilterSelected(status: String?) {
        _uiState.update { it.copy(statusFilter = status) }
    }

    fun showApplySheet(show: Boolean) {
        _uiState.update { it.copy(showApplySheet = show) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun cancelLeave(leaveId: Int) {
        viewModelScope.launch {
            leaveRepository.cancelLeave(leaveId, LeaveCategory.STUDENT)
                .onSuccess { message ->
                    _uiState.update { it.copy(snackbarMessage = message) }
                    loadLeaves()
                }
                .onFailure { err ->
                    _uiState.update { it.copy(snackbarMessage = err.message) }
                }
        }
    }

    fun applyLeave(
        leaveType: String,
        fromDate: String,
        toDate: String,
        reason: String,
        remarks: String
    ) {
        val id = userId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            leaveRepository.applyStudentLeave(
                CreateStudentLeaveBody(
                    student_id = id,
                    from_date = fromDate.take(10),
                    to_date = toDate.take(10),
                    leave_type = leaveType,
                    reason = reason,
                    remarks = remarks.takeIf { it.isNotBlank() },
                    branch_id = branchId ?: SessionManager.user?.branch_id
                )
            ).onSuccess { message ->
                _uiState.update {
                    it.copy(
                        snackbarMessage = message,
                        showApplySheet = false,
                        isSubmitting = false
                    )
                }
                loadLeaves()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(snackbarMessage = err.message, isSubmitting = false)
                }
            }
        }
    }
}
