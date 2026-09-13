package com.example.myeduapp.features.attendance

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.Attendance
import com.example.myeduapp.data.model.AttendanceOverview
import com.example.myeduapp.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ViewTeacherSelfAttendanceUiState(
    val records: List<Attendance> = emptyList(),
    val overview: AttendanceOverview? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class ViewTeacherSelfAttendanceViewModel(
    private val attendanceRepository: AttendanceRepository = AttendanceRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ViewTeacherSelfAttendanceUiState())
    val uiState: StateFlow<ViewTeacherSelfAttendanceUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        val userId = SessionManager.user?.id ?: run {
            _uiState.update { it.copy(isLoading = false, error = "Not signed in") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            var error: String? = null
            var records: List<Attendance> = emptyList()
            var overview: AttendanceOverview? = null

            attendanceRepository.getTeacherAttendance(userId)
                .onSuccess { records = it }
                .onFailure { error = it.message }

            attendanceRepository.getTeacherOverview(userId)
                .onSuccess { overview = it }

            _uiState.update {
                it.copy(
                    records = records,
                    overview = overview,
                    isLoading = false,
                    error = error
                )
            }
        }
    }
}
