package com.example.myeduapp.features.teacher.dashboard

import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.DashboardResponse
import com.example.myeduapp.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val dashboardData: DashboardResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository = DashboardRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = dashboardRepository.getDashboard()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    dashboardData = result.getOrNull(),
                    errorMessage = result.exceptionOrNull()?.message?.takeIf { _ -> result.isFailure }
                )
            }
        }
    }
}
