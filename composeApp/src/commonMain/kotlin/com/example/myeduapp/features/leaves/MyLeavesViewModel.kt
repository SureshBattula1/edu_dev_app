package com.example.myeduapp.features.leaves

import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.CreateStudentLeaveBody
import com.example.myeduapp.data.model.CreateTeacherLeaveBody
import com.example.myeduapp.data.model.LeaveCategory
import com.example.myeduapp.data.model.LeaveRecord
import com.example.myeduapp.data.model.LeaveSummary
import com.example.myeduapp.data.repository.LeaveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyLeavesUiState(
    val leaves: List<LeaveRecord> = emptyList(),
    val summary: LeaveSummary? = null,
    val statusFilter: String? = null,
    val isLoading: Boolean = true,
    val showApplySheet: Boolean = false,
    val isSubmitting: Boolean = false,
    val snackbarMessage: String? = null
)

class MyLeavesViewModel(
    private val category: LeaveCategory,
    private val leaveRepository: LeaveRepository = LeaveRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(MyLeavesUiState())
    val uiState: StateFlow<MyLeavesUiState> = _uiState.asStateFlow()

    fun setStatusFilter(filter: String?) {
        _uiState.update { it.copy(statusFilter = filter) }
    }

    fun setShowApplySheet(show: Boolean) {
        _uiState.update { it.copy(showApplySheet = show) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun loadLeaves(userId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val filter = _uiState.value.statusFilter
            leaveRepository.getMyLeaves(category, userId)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            leaves = response.data.filter { record ->
                                filter == null || record.status == filter
                            },
                            summary = response.summary,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, snackbarMessage = error.message)
                    }
                }
        }
    }

    fun cancelLeave(leaveId: Int, userId: Int) {
        viewModelScope.launch {
            leaveRepository.cancelLeave(leaveId, category)
                .onSuccess { message ->
                    _uiState.update { it.copy(snackbarMessage = message) }
                    loadLeaves(userId)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(snackbarMessage = error.message) }
                }
        }
    }

    fun applyLeave(
        userId: Int,
        branchId: Int?,
        leaveType: String,
        fromDate: String,
        toDate: String,
        reason: String,
        remarks: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = when (category) {
                LeaveCategory.STUDENT -> leaveRepository.applyStudentLeave(
                    CreateStudentLeaveBody(
                        student_id = userId,
                        from_date = fromDate.take(10),
                        to_date = toDate.take(10),
                        leave_type = leaveType,
                        reason = reason,
                        remarks = remarks.takeIf { it.isNotBlank() },
                        branch_id = branchId
                    )
                )
                LeaveCategory.TEACHER -> leaveRepository.applyTeacherLeave(
                    CreateTeacherLeaveBody(
                        teacher_id = userId,
                        from_date = fromDate.take(10),
                        to_date = toDate.take(10),
                        leave_type = leaveType,
                        reason = reason,
                        remarks = remarks.takeIf { it.isNotBlank() },
                        branch_id = branchId
                    )
                )
            }
            result.onSuccess { message ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        showApplySheet = false,
                        snackbarMessage = message
                    )
                }
                loadLeaves(userId)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isSubmitting = false, snackbarMessage = error.message)
                }
            }
        }
    }
}
