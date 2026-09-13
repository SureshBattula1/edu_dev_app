package com.example.myeduapp.features.teacher.attendance

import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.Attendance
import com.example.myeduapp.data.model.SchoolClass
import com.example.myeduapp.data.model.Student
import com.example.myeduapp.data.repository.AttendanceRepository
import com.example.myeduapp.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateAttendanceUiState(
    val students: List<Student> = emptyList(),
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val isSubmitting: Boolean = false,
    val isUpdateMode: Boolean = false,
    val confirmSubmit: Boolean = false,
    val attendanceStates: Map<String, String> = emptyMap(),
    val remarksStates: Map<String, String> = emptyMap(),
    val snackbarMessage: String? = null,
    val navigateBack: Boolean = false
) {
    val presentCount: Int
        get() = students.count { (attendanceStates[it.attendanceUserId] ?: "Present") == "Present" }

    val absentCount: Int
        get() = students.count { (attendanceStates[it.attendanceUserId] ?: "Present") == "Absent" }
}

class CreateAttendanceViewModel(
    private val studentRepository: StudentRepository = StudentRepository(),
    private val attendanceRepository: AttendanceRepository = AttendanceRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(CreateAttendanceUiState())
    val uiState: StateFlow<CreateAttendanceUiState> = _uiState.asStateFlow()

    val statuses = listOf("Present", "Absent", "Late", "Half-Day", "Sick Leave")

    private var currentClass: SchoolClass? = null
    private var currentDate: String? = null

    fun loadStudents(schoolClass: SchoolClass, date: String) {
        currentClass = schoolClass
        currentDate = date
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadError = null,
                    isUpdateMode = false,
                    attendanceStates = emptyMap(),
                    remarksStates = emptyMap()
                )
            }

            val listResult = studentRepository.getStudentsByClass(
                schoolClass.displayGrade,
                schoolClass.section
            )
            val list = listResult.getOrElse { e ->
                _uiState.update {
                    it.copy(
                        loadError = e.message ?: "Could not load students",
                        students = emptyList(),
                        isLoading = false
                    )
                }
                return@launch
            }

            val attendanceStates = mutableMapOf<String, String>()
            val remarksStates = mutableMapOf<String, String>()
            list.forEach { student ->
                attendanceStates[student.attendanceUserId] = "Present"
                remarksStates[student.attendanceUserId] = ""
            }

            var isUpdateMode = false
            if (list.isNotEmpty()) {
                attendanceRepository.getClassAttendance(schoolClass, date).onSuccess { result ->
                    if (result.attendance.isNotEmpty()) {
                        isUpdateMode = applyExistingAttendance(
                            students = list,
                            existing = result.attendance,
                            attendanceStates = attendanceStates,
                            remarksStates = remarksStates
                        )
                    }
                }
            }

            _uiState.update {
                it.copy(
                    students = list,
                    attendanceStates = attendanceStates,
                    remarksStates = remarksStates,
                    isUpdateMode = isUpdateMode,
                    isLoading = false
                )
            }
        }
    }

    fun setStatus(userKey: String, status: String) {
        _uiState.update {
            it.copy(attendanceStates = it.attendanceStates + (userKey to status))
        }
    }

    fun setRemarks(userKey: String, remarks: String) {
        _uiState.update {
            it.copy(remarksStates = it.remarksStates + (userKey to remarks))
        }
    }

    fun markAllPresent() {
        _uiState.update { state ->
            state.copy(
                attendanceStates = state.students.associate { it.attendanceUserId to "Present" }
            )
        }
    }

    fun markAllAbsent() {
        _uiState.update { state ->
            state.copy(
                attendanceStates = state.students.associate { it.attendanceUserId to "Absent" }
            )
        }
    }

    fun setConfirmSubmit(show: Boolean) {
        _uiState.update { it.copy(confirmSubmit = show) }
    }

    fun submit() {
        val schoolClass = currentClass ?: return
        val date = currentDate ?: return
        val state = _uiState.value
        if (state.isSubmitting) return

        _uiState.update { it.copy(confirmSubmit = false, isSubmitting = true) }
        viewModelScope.launch {
            attendanceRepository.submitStudentAttendance(
                schoolClass = schoolClass,
                date = date,
                students = state.students,
                statusByUserId = state.attendanceStates,
                remarksByUserId = state.remarksStates,
                isUpdate = state.isUpdateMode
            ).onSuccess { message ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        snackbarMessage = message,
                        navigateBack = true
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        snackbarMessage = e.message ?: "Failed to save"
                    )
                }
            }
        }
    }

    fun consumeSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun consumeNavigateBack() {
        _uiState.update { it.copy(navigateBack = false) }
    }
}

private fun Student.matchesAttendanceUser(userId: String): Boolean =
    attendanceUserId == userId || user_id == userId || id == userId

private fun applyExistingAttendance(
    students: List<Student>,
    existing: List<Attendance>,
    attendanceStates: MutableMap<String, String>,
    remarksStates: MutableMap<String, String>
): Boolean {
    var applied = false
    existing.forEach { record ->
        val userId = record.student_id?.takeIf { it.isNotBlank() && it != "0" } ?: return@forEach
        val student = students.find { it.matchesAttendanceUser(userId) }
        val key = student?.attendanceUserId ?: userId
        attendanceStates[key] = record.status
        remarksStates[key] = record.remarks ?: ""
        applied = true
    }
    return applied
}
