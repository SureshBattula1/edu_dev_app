package com.example.myeduapp.features.teacher.exams

import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.Exam
import com.example.myeduapp.data.repository.ExamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherExamsUiState(
    val exams: List<Exam> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class TeacherExamsViewModel(
    private val examRepository: ExamRepository = ExamRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(TeacherExamsUiState())
    val uiState: StateFlow<TeacherExamsUiState> = _uiState.asStateFlow()

    init {
        loadExams()
    }

    fun loadExams() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = examRepository.getUpcomingExams()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    exams = result.getOrElse { emptyList() },
                    errorMessage = result.exceptionOrNull()?.message?.takeIf { _ -> result.isFailure }
                )
            }
        }
    }
}
