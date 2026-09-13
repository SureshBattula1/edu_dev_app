package com.example.myeduapp.features.teacher.attendance

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.util.DateUtils
import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.AttendanceNotifyClass
import com.example.myeduapp.data.model.AttendanceNotifyReceipts
import com.example.myeduapp.data.model.ClassAttendanceStatus
import com.example.myeduapp.data.model.UserRole
import com.example.myeduapp.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherAttendanceOverviewUiState(
    val date: String = DateUtils.today(),
    val rows: List<ClassAttendanceStatus> = emptyList(),
    val selected: Set<String> = emptySet(),
    val loading: Boolean = true,
    val sending: Boolean = false,
    val confirmSend: Boolean = false,
    val error: String? = null,
    val canNotify: Boolean = false,
    val snackbarMessage: String? = null,
    val receiptsFor: ClassAttendanceStatus? = null,
    val receiptsLoading: Boolean = false,
    val receiptsData: AttendanceNotifyReceipts? = null,
    val receiptsError: String? = null
) {
    val createdRows: List<ClassAttendanceStatus>
        get() = rows.filter { it.created }

    val selectedCreated: List<String>
        get() = selected.filter { key -> createdRows.any { it.selectionKey == key } }
}

class TeacherAttendanceOverviewViewModel(
    private val attendanceRepository: AttendanceRepository = AttendanceRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(
        TeacherAttendanceOverviewUiState(
            canNotify = SessionManager.user?.userRole in listOf(
                UserRole.BRANCH_ADMIN,
                UserRole.SUPER_ADMIN
            )
        )
    )
    val uiState: StateFlow<TeacherAttendanceOverviewUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onDateChange(date: String) {
        _uiState.update { it.copy(date = date, selected = emptySet()) }
        load()
    }

    fun load() {
        val date = _uiState.value.date
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            attendanceRepository.getClassStatus(date)
                .onSuccess { rows ->
                    _uiState.update { state ->
                        state.copy(
                            rows = rows,
                            selected = state.selected.filter { key ->
                                rows.any { row -> row.created && row.selectionKey == key }
                            }.toSet(),
                            loading = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "Could not load attendance",
                            loading = false
                        )
                    }
                }
        }
    }

    fun selectAllCreated() {
        _uiState.update { state ->
            state.copy(selected = state.createdRows.map { it.selectionKey }.toSet())
        }
    }

    fun uncheckAll() {
        _uiState.update { it.copy(selected = emptySet()) }
    }

    fun toggleSelected(key: String, checked: Boolean, rowCreated: Boolean) {
        if (!rowCreated) return
        _uiState.update { state ->
            state.copy(
                selected = if (checked) state.selected + key else state.selected - key
            )
        }
    }

    fun setConfirmSend(show: Boolean) {
        _uiState.update { it.copy(confirmSend = show) }
    }

    fun sendNotifications() {
        val state = _uiState.value
        val classes = state.rows
            .filter { it.selectionKey in state.selectedCreated && it.created }
            .mapNotNull { row ->
                val grade = row.grade?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val section = row.section?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                AttendanceNotifyClass(grade = grade, section = section)
            }
        if (classes.isEmpty()) return

        _uiState.update { it.copy(confirmSend = false, sending = true) }
        viewModelScope.launch {
            attendanceRepository.notifyStudents(state.date, classes)
                .onSuccess { result ->
                    val message = "Sent to ${result.student_count} student(s)" +
                        if (result.skipped_no_login > 0) {
                            " · ${result.skipped_no_login} skipped (no login)"
                        } else {
                            ""
                        }
                    _uiState.update {
                        it.copy(
                            sending = false,
                            selected = emptySet(),
                            snackbarMessage = message
                        )
                    }
                    load()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            sending = false,
                            snackbarMessage = e.message ?: "Failed to send"
                        )
                    }
                }
        }
    }

    fun consumeSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun openReceipts(row: ClassAttendanceStatus) {
        _uiState.update {
            it.copy(
                receiptsFor = row,
                receiptsLoading = true,
                receiptsData = null,
                receiptsError = null
            )
        }
        viewModelScope.launch {
            attendanceRepository.getNotifyReceipts(
                _uiState.value.date,
                row.grade.orEmpty(),
                row.section.orEmpty()
            )
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(receiptsData = data, receiptsLoading = false)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            receiptsError = e.message ?: "Could not load status",
                            receiptsLoading = false
                        )
                    }
                }
        }
    }

    fun dismissReceipts() {
        _uiState.update {
            it.copy(
                receiptsFor = null,
                receiptsData = null,
                receiptsError = null,
                receiptsLoading = false
            )
        }
    }
}
