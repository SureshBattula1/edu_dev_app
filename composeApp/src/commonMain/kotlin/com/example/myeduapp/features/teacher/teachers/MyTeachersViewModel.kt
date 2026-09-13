package com.example.myeduapp.features.teacher.teachers

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.FilterOption
import com.example.myeduapp.data.model.GradeOption
import com.example.myeduapp.data.model.Teacher
import com.example.myeduapp.data.model.UserRole
import com.example.myeduapp.data.repository.BranchRepository
import com.example.myeduapp.data.repository.ClassRepository
import com.example.myeduapp.data.repository.TeacherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyTeachersUiState(
    val teachers: List<Teacher> = emptyList(),
    val branchOptions: List<FilterOption> = emptyList(),
    val grades: List<GradeOption> = emptyList(),
    val sections: List<FilterOption> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedBranchId: String? = null,
    val selectedGrade: String? = null,
    val selectedSection: String? = null,
    val showBranchFilter: Boolean = SessionManager.user?.userRole == UserRole.SUPER_ADMIN,
    val showClassSectionFilters: Boolean = SessionManager.user?.userRole == UserRole.BRANCH_ADMIN
) {
    val gradeOptions: List<FilterOption>
        get() = grades.map { FilterOption(value = it.value, label = it.label) }

    val hasActiveFilters: Boolean
        get() = selectedBranchId != null || selectedGrade != null || selectedSection != null
}

class MyTeachersViewModel(
    private val teacherRepository: TeacherRepository = TeacherRepository(),
    private val branchRepository: BranchRepository = BranchRepository(),
    private val classRepository: ClassRepository = ClassRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(MyTeachersUiState())
    val uiState: StateFlow<MyTeachersUiState> = _uiState.asStateFlow()

    private var loadTeachersJob: Job? = null

    init {
        val state = _uiState.value
        if (state.showBranchFilter) loadBranches()
        if (state.showClassSectionFilters) loadGrades()
        loadTeachers()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadTeachers()
    }

    fun onBranchSelected(branchId: String?) {
        _uiState.update { it.copy(selectedBranchId = branchId) }
        loadTeachers()
    }

    fun onGradeSelected(grade: String?) {
        _uiState.update {
            it.copy(selectedGrade = grade, selectedSection = null)
        }
        loadSections()
        loadTeachers()
    }

    fun onSectionSelected(section: String?) {
        _uiState.update { it.copy(selectedSection = section) }
        loadTeachers()
    }

    fun clearBranchFilter() {
        _uiState.update { it.copy(selectedBranchId = null) }
        loadTeachers()
    }

    fun clearClassFilters() {
        _uiState.update {
            it.copy(selectedGrade = null, selectedSection = null)
        }
        loadSections()
        loadTeachers()
    }

    private fun loadBranches() {
        viewModelScope.launch {
            branchRepository.getBranchFilterOptions().onSuccess { options ->
                _uiState.update { it.copy(branchOptions = options) }
            }
        }
    }

    private fun loadGrades() {
        viewModelScope.launch {
            classRepository.getGrades().onSuccess { grades ->
                _uiState.update { it.copy(grades = grades) }
            }
        }
    }

    private fun loadSections() {
        if (!_uiState.value.showClassSectionFilters) return
        viewModelScope.launch {
            val grade = _uiState.value.selectedGrade
            classRepository.getSections(grade).onSuccess { sections ->
                _uiState.update {
                    it.copy(
                        sections = sections,
                        selectedSection = if (grade == null) null else it.selectedSection
                    )
                }
            }
        }
    }

    private fun loadTeachers() {
        val query = _uiState.value.searchQuery
        if (query.isNotEmpty() && query.length < 2) return

        loadTeachersJob?.cancel()
        loadTeachersJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, teachers = emptyList()) }
            if (query.isNotEmpty()) delay(500)
            val state = _uiState.value
            teacherRepository.getTeachers(
                query = query.ifBlank { null },
                branchId = if (state.showBranchFilter) state.selectedBranchId?.toIntOrNull() else null,
                grade = if (state.showClassSectionFilters) state.selectedGrade else null,
                section = if (state.showClassSectionFilters) state.selectedSection else null
            ).onSuccess { teachers ->
                _uiState.update { it.copy(teachers = teachers, isLoading = false) }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
