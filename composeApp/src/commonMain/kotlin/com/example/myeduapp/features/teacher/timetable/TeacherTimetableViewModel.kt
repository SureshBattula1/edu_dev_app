package com.example.myeduapp.features.teacher.timetable

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.TimetableSlot
import com.example.myeduapp.data.repository.TimetableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherTimetableUiState(
    val slots: List<TimetableSlot> = emptyList(),
    val selectedDay: String = "Monday",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class TeacherTimetableViewModel(
    private val timetableRepository: TimetableRepository = TimetableRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(TeacherTimetableUiState())
    val uiState: StateFlow<TeacherTimetableUiState> = _uiState.asStateFlow()

    init {
        loadTimetable()
    }

    fun onDaySelected(day: String) {
        _uiState.update { it.copy(selectedDay = day) }
    }

    fun loadTimetable() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val grade = SessionManager.user?.branch_id?.toString() ?: "1"
            val result = timetableRepository.getTimetable(grade, "A")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    slots = result.getOrElse { emptyList() },
                    errorMessage = result.exceptionOrNull()?.message?.takeIf { _ -> result.isFailure }
                )
            }
        }
    }
}
