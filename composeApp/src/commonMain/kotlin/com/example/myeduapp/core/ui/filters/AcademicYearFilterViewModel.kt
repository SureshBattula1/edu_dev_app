package com.example.myeduapp.core.ui.filters

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.AcademicYear
import com.example.myeduapp.data.model.FilterOption
import com.example.myeduapp.data.repository.AcademicYearRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AcademicYearFilterUiState(
    val academicYears: List<AcademicYear> = emptyList(),
    val selectedYearId: String? = SessionManager.academicYearId,
    val isLoading: Boolean = true,
    val isReady: Boolean = false,
    val error: String? = null
) {
    val options: List<FilterOption>
        get() = academicYears.map { FilterOption(value = it.id, label = it.name) }

    val selectedYear: AcademicYear?
        get() = academicYears.find { it.id == selectedYearId }
}

class AcademicYearFilterViewModel(
    private val repository: AcademicYearRepository = AcademicYearRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(AcademicYearFilterUiState())
    val uiState: StateFlow<AcademicYearFilterUiState> = _uiState.asStateFlow()

    init {
        loadYears()
    }

    fun loadYears() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isReady = false, error = null) }
            repository.loadFilterOptions()
                .onSuccess { years ->
                    val currentId = _uiState.value.selectedYearId
                    val validSelection = currentId?.let { id -> years.find { it.id == id } }
                    val default = validSelection ?: years.find { it.is_current } ?: years.firstOrNull()
                    default?.let { year ->
                        SessionManager.setAcademicYear(year.id, year.name)
                    }
                    _uiState.update {
                        it.copy(
                            academicYears = years,
                            selectedYearId = default?.id,
                            isLoading = false,
                            isReady = default != null,
                            error = null
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isReady = false,
                            error = err.message ?: "Failed to load academic years"
                        )
                    }
                }
        }
    }

    fun selectYear(yearId: String) {
        val year = _uiState.value.academicYears.find { it.id == yearId } ?: return
        SessionManager.setAcademicYear(year.id, year.name)
        _uiState.update { it.copy(selectedYearId = yearId) }
    }
}
