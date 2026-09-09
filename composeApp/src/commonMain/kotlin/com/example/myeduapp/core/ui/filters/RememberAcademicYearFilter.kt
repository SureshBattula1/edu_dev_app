package com.example.myeduapp.core.ui.filters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.data.model.AcademicYear
import com.example.myeduapp.data.model.FilterOption
import com.example.myeduapp.data.repository.AcademicYearRepository

data class AcademicYearFilterState(
    val academicYears: List<AcademicYear>,
    val options: List<FilterOption>,
    val selectedYearId: String?,
    val selectedYear: AcademicYear?,
    val isLoading: Boolean,
    val isReady: Boolean,
    val error: String?,
    val onYearSelected: (String) -> Unit
)

@Composable
fun rememberAcademicYearFilter(
    repository: AcademicYearRepository = remember { AcademicYearRepository() }
): AcademicYearFilterState {
    var academicYears by remember { mutableStateOf<List<AcademicYear>>(emptyList()) }
    var selectedYearId by remember { mutableStateOf(SessionManager.academicYearId) }
    var isLoading by remember { mutableStateOf(true) }
    var isReady by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        isReady = false
        error = null
        repository.loadFilterOptions()
            .onSuccess { years ->
                academicYears = years
                val validSelection = selectedYearId?.let { id -> years.find { it.id == id } }
                val default = validSelection ?: years.find { it.is_current } ?: years.firstOrNull()
                default?.let { year ->
                    selectedYearId = year.id
                    SessionManager.setAcademicYear(year.id, year.name)
                }
                isLoading = false
                isReady = selectedYearId != null
            }
            .onFailure {
                error = it.message ?: "Failed to load academic years"
                isLoading = false
                isReady = false
            }
    }

    val options = remember(academicYears) {
        academicYears.map { FilterOption(value = it.id, label = it.name) }
    }

    val selectedYear = remember(academicYears, selectedYearId) {
        academicYears.find { it.id == selectedYearId }
    }

    return AcademicYearFilterState(
        academicYears = academicYears,
        options = options,
        selectedYearId = selectedYearId,
        selectedYear = selectedYear,
        isLoading = isLoading,
        isReady = isReady,
        error = error,
        onYearSelected = { yearId ->
            selectedYearId = yearId
            academicYears.find { it.id == yearId }?.let { year ->
                SessionManager.setAcademicYear(year.id, year.name)
            }
        }
    )
}
