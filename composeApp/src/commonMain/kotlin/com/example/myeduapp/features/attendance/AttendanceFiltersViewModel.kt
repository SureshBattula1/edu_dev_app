package com.example.myeduapp.features.attendance

import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.FilterOption
import com.example.myeduapp.data.model.SchoolClass
import com.example.myeduapp.data.repository.ClassRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AttendanceFiltersUiState(
    val gradeOptions: List<FilterOption> = emptyList(),
    val sectionOptions: List<FilterOption> = emptyList(),
    val selectedGrade: String? = null,
    val selectedSection: String? = null,
    val isLoadingGrades: Boolean = true
) {
    val selectedClass: SchoolClass?
        get() = if (selectedGrade != null && selectedSection != null) {
            schoolClassFrom(selectedGrade, selectedSection)
        } else null

    val isReady: Boolean
        get() = selectedGrade != null && selectedSection != null
}

class AttendanceFiltersViewModel(
    private val classRepository: ClassRepository = ClassRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(AttendanceFiltersUiState())
    val uiState: StateFlow<AttendanceFiltersUiState> = _uiState.asStateFlow()

    init {
        loadGrades()
    }

    fun onGradeSelected(grade: String?) {
        _uiState.update {
            it.copy(selectedGrade = grade, selectedSection = null, sectionOptions = emptyList())
        }
        loadSections(grade)
    }

    fun onSectionSelected(section: String?) {
        _uiState.update { it.copy(selectedSection = section) }
    }

    fun onClear() {
        _uiState.update {
            it.copy(
                selectedGrade = null,
                selectedSection = null,
                sectionOptions = emptyList()
            )
        }
    }

    private fun loadGrades() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingGrades = true) }
            classRepository.getGrades().onSuccess { grades ->
                _uiState.update {
                    it.copy(
                        gradeOptions = grades.map { g -> FilterOption(value = g.value, label = g.label) },
                        isLoadingGrades = false
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isLoadingGrades = false) }
            }
        }
    }

    private fun loadSections(grade: String?) {
        viewModelScope.launch {
            if (grade == null) {
                _uiState.update { it.copy(sectionOptions = emptyList()) }
                return@launch
            }
            classRepository.getSections(grade).onSuccess { sections ->
                _uiState.update { it.copy(sectionOptions = sections) }
            }
        }
    }
}
