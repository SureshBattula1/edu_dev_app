package com.example.myeduapp.features.teacher.fees

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.FeeDue
import com.example.myeduapp.data.model.FeePayment
import com.example.myeduapp.data.model.FeeSummary
import com.example.myeduapp.data.repository.FeeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherFeesUiState(
    val dues: List<FeeDue> = emptyList(),
    val payments: List<FeePayment> = emptyList(),
    val feeSummary: FeeSummary = FeeSummary(),
    val selectedTab: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class TeacherFeesViewModel(
    private val feeRepository: FeeRepository = FeeRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(TeacherFeesUiState())
    val uiState: StateFlow<TeacherFeesUiState> = _uiState.asStateFlow()

    init {
        loadFees()
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun loadFees() {
        val userId = SessionManager.user?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = feeRepository.getStudentFees(userId)
            _uiState.update {
                if (result.isSuccess) {
                    val fees = result.getOrNull()
                    it.copy(
                        isLoading = false,
                        dues = fees?.dues.orEmpty(),
                        payments = fees?.payments.orEmpty(),
                        feeSummary = fees?.summary ?: FeeSummary()
                    )
                } else {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
}
