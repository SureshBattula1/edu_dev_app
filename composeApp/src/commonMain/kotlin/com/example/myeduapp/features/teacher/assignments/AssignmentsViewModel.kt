package com.example.myeduapp.features.teacher.assignments

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.Assignment
import com.example.myeduapp.data.model.UserRole
import com.example.myeduapp.data.repository.AssignmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssignmentsUiState(
    val assignments: List<Assignment> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val canCreate: Boolean = SessionManager.user?.userRole in listOf(
        UserRole.TEACHER,
        UserRole.BRANCH_ADMIN,
        UserRole.SUPER_ADMIN
    )
)

class AssignmentsViewModel(
    private val assignmentRepository: AssignmentRepository = AssignmentRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(AssignmentsUiState())
    val uiState: StateFlow<AssignmentsUiState> = _uiState.asStateFlow()

    init {
        loadAssignments()
    }

    fun loadAssignments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            assignmentRepository.getAssignments()
                .onSuccess { list ->
                    _uiState.update {
                        it.copy(assignments = list, error = null, isLoading = false)
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(error = err.message, isLoading = false)
                    }
                }
        }
    }
}
