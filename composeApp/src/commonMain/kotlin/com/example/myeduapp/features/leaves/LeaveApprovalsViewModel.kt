package com.example.myeduapp.features.leaves

import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.LeaveCategory
import com.example.myeduapp.data.model.LeaveRecord
import com.example.myeduapp.data.repository.LeaveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LeaveApprovalsUiState(
    val leaves: List<LeaveRecord> = emptyList(),
    val statusFilter: String? = "Pending",
    val isLoading: Boolean = true,
    val processingId: Int? = null,
    val snackbarMessage: String? = null
)

class LeaveApprovalsViewModel(
    private val category: LeaveCategory,
    private val leaveRepository: LeaveRepository = LeaveRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(LeaveApprovalsUiState())
    val uiState: StateFlow<LeaveApprovalsUiState> = _uiState.asStateFlow()

    fun setStatusFilter(filter: String?) {
        _uiState.update { it.copy(statusFilter = filter) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun loadLeaves() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            leaveRepository.getLeavesForApproval(category, _uiState.value.statusFilter)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(leaves = response.data, isLoading = false)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, snackbarMessage = error.message)
                    }
                }
        }
    }

    fun processLeave(leaveId: Int, approve: Boolean, remarks: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(processingId = leaveId) }
            val result = if (approve) {
                leaveRepository.approveLeave(leaveId, category, remarks)
            } else {
                leaveRepository.rejectLeave(leaveId, category, remarks)
            }
            _uiState.update { it.copy(processingId = null) }
            result.onSuccess { message ->
                _uiState.update { it.copy(snackbarMessage = message) }
                loadLeaves()
            }.onFailure { error ->
                _uiState.update { it.copy(snackbarMessage = error.message) }
            }
        }
    }
}
