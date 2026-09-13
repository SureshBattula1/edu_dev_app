package com.example.myeduapp.features.teacher.attendance

import com.example.myeduapp.core.util.DateUtils
import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.Attendance
import com.example.myeduapp.data.model.SchoolClass
import com.example.myeduapp.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherAttendanceUiState(
    val selectedDate: String = DateUtils.today(),
    val attendanceRecords: List<Attendance> = emptyList(),
    val isLoading: Boolean = false
)

class TeacherAttendanceViewModel(
    private val attendanceRepository: AttendanceRepository = AttendanceRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(TeacherAttendanceUiState())
    val uiState: StateFlow<TeacherAttendanceUiState> = _uiState.asStateFlow()

    fun onDateChange(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun loadAttendance(schoolClass: SchoolClass?) {
        if (schoolClass == null) {
            _uiState.update { it.copy(attendanceRecords = emptyList(), isLoading = false) }
            return
        }
        val date = _uiState.value.selectedDate
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            attendanceRepository.getClassAttendance(schoolClass, date)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(attendanceRecords = result.attendance, isLoading = false)
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(attendanceRecords = emptyList(), isLoading = false) }
                }
        }
    }
}
