package com.example.myeduapp.features.teacher.students

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.FilterOption
import com.example.myeduapp.data.model.GradeOption
import com.example.myeduapp.data.model.Student
import com.example.myeduapp.data.model.UserRole
import com.example.myeduapp.data.repository.BranchRepository
import com.example.myeduapp.data.repository.ClassRepository
import com.example.myeduapp.data.repository.StudentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyStudentsUiState(
    val students: List<Student> = emptyList(),
    val branchOptions: List<FilterOption> = emptyList(),
    val grades: List<GradeOption> = emptyList(),
    val sections: List<FilterOption> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedBranchId: String? = null,
    val selectedGrade: String? = null,
    val selectedSection: String? = null,
    val showBranchFilter: Boolean = SessionManager.user?.userRole == UserRole.SUPER_ADMIN
) {
    val gradeOptions: List<FilterOption>
        get() = grades.map { FilterOption(value = it.value, label = it.label) }

    val hasActiveFilters: Boolean
        get() = selectedBranchId != null || selectedGrade != null || selectedSection != null

    val selectedBranchInt: Int?
        get() = selectedBranchId?.toIntOrNull()
}

class MyStudentsViewModel(
    private val studentRepository: StudentRepository = StudentRepository(),
    private val classRepository: ClassRepository = ClassRepository(),
    private val branchRepository: BranchRepository = BranchRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(MyStudentsUiState())
    val uiState: StateFlow<MyStudentsUiState> = _uiState.asStateFlow()

    private var loadStudentsJob: Job? = null

    init {
        if (_uiState.value.showBranchFilter) {
            loadBranches()
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onBranchSelected(branchId: String?) {
        _uiState.update {
            it.copy(
                selectedBranchId = branchId,
                selectedGrade = null,
                selectedSection = null
            )
        }
    }

    fun onGradeSelected(grade: String?) {
        _uiState.update {
            it.copy(selectedGrade = grade, selectedSection = null)
        }
    }

    fun onSectionSelected(section: String?) {
        _uiState.update { it.copy(selectedSection = section) }
    }

    fun clearClassFilters() {
        _uiState.update {
            it.copy(
                selectedBranchId = null,
                selectedGrade = null,
                selectedSection = null
            )
        }
    }

    fun loadGrades() {
        viewModelScope.launch {
            val state = _uiState.value
            classRepository.getGrades(
                branchId = if (state.showBranchFilter) state.selectedBranchInt else null
            ).onSuccess { grades ->
                _uiState.update {
                    it.copy(
                        grades = grades,
                        selectedGrade = null,
                        selectedSection = null
                    )
                }
            }
        }
    }

    fun loadSections() {
        viewModelScope.launch {
            val state = _uiState.value
            classRepository.getSections(
                grade = state.selectedGrade,
                branchId = if (state.showBranchFilter) state.selectedBranchInt else null
            ).onSuccess { sections ->
                _uiState.update {
                    it.copy(
                        sections = sections,
                        selectedSection = if (state.selectedGrade == null) null else it.selectedSection
                    )
                }
            }
        }
    }

    fun loadStudents(academicYearId: String?) {
        val query = _uiState.value.searchQuery
        if (query.isNotEmpty() && query.length < 2) return
        if (academicYearId == null) return

        loadStudentsJob?.cancel()
        loadStudentsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, students = emptyList()) }
            if (query.isNotEmpty()) delay(500)
            val state = _uiState.value
            studentRepository.getStudents(
                query = query.ifBlank { null },
                grade = state.selectedGrade,
                section = state.selectedSection,
                academicYearId = academicYearId,
                branchId = if (state.showBranchFilter) state.selectedBranchInt else null
            ).onSuccess { students ->
                _uiState.update { it.copy(students = students, isLoading = false) }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadBranches() {
        viewModelScope.launch {
            branchRepository.getBranchFilterOptions().onSuccess { options ->
                _uiState.update { it.copy(branchOptions = options) }
            }
        }
    }
}
