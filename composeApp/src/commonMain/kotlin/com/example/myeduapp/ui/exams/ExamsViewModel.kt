package com.example.myeduapp.ui.exams

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.Exam
import com.example.myeduapp.data.model.ExamResult
import com.example.myeduapp.data.repository.ExamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExamsUiState(
    val upcomingExams: List<Exam> = emptyList(),
    val results: List<ExamResult> = emptyList(),
    val selectedTab: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class ExamsViewModel(
    private val examRepository: ExamRepository = ExamRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ExamsUiState())
    val uiState: StateFlow<ExamsUiState> = _uiState.asStateFlow()

    init {
        loadExams()
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun loadExams() {
        val userId = SessionManager.user?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val examsResult = examRepository.getUpcomingExams()
            val resultsResult = examRepository.getStudentResults(userId)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    upcomingExams = examsResult.getOrElse { emptyList() },
                    results = resultsResult.getOrElse { emptyList() }
                )
            }
        }
    }
}
