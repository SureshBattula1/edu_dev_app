package com.example.myeduapp.core.ui.filters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.data.model.AcademicYear
import com.example.myeduapp.data.model.FilterOption

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
fun rememberAcademicYearFilter(): AcademicYearFilterState {
    val screen = LocalNavigator.currentOrThrow.lastItem
    val viewModel = screen.rememberScreenModel(tag = "academicYearFilter") {
        AcademicYearFilterViewModel()
    }
    val uiState by viewModel.uiState.collectAsState()

    return AcademicYearFilterState(
        academicYears = uiState.academicYears,
        options = uiState.options,
        selectedYearId = uiState.selectedYearId,
        selectedYear = uiState.selectedYear,
        isLoading = uiState.isLoading,
        isReady = uiState.isReady,
        error = uiState.error,
        onYearSelected = viewModel::selectYear
    )
}
