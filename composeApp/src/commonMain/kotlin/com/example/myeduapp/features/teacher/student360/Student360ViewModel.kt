package com.example.myeduapp.features.teacher.student360

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.Attendance
import com.example.myeduapp.data.model.AttendanceOverview
import com.example.myeduapp.data.model.ExamResult
import com.example.myeduapp.data.model.FeeDue
import com.example.myeduapp.data.model.FeePayment
import com.example.myeduapp.data.model.FeeSummary
import com.example.myeduapp.data.model.Student
import com.example.myeduapp.data.model.StudentDetail
import com.example.myeduapp.data.model.StudentFeesData
import com.example.myeduapp.data.model.User
import com.example.myeduapp.data.model.mergeWithDetail
import com.example.myeduapp.data.model.resolveStudentUserId
import com.example.myeduapp.data.model.toStudentSeed
import com.example.myeduapp.data.repository.AttendanceRepository
import com.example.myeduapp.data.repository.ExamRepository
import com.example.myeduapp.data.repository.FeeRepository
import com.example.myeduapp.data.repository.StudentRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class Student360UiState(
    val resolvedStudent: Student? = null,
    val resolvingStudent: Boolean = false,
    val selectedTab: Int = 0,
    val isLoading: Boolean = true,
    val detail: StudentDetail? = null,
    val loadError: String? = null,
    val attendanceOverview: AttendanceOverview? = null,
    val attendanceRecords: List<Attendance> = emptyList(),
    val feeDues: List<FeeDue> = emptyList(),
    val feePayments: List<FeePayment> = emptyList(),
    val feeSummary: FeeSummary = FeeSummary(),
    val examResults: List<ExamResult> = emptyList(),
    val isSelfView: Boolean = false
) {
    val displayStudent: Student?
        get() = resolvedStudent?.mergeWithDetail(detail)

    val studentUserId: Int?
        get() = displayStudent?.let { resolveStudentUserId(it, detail) }

    val branchId: Int?
        get() = SessionManager.user?.branch_id ?: detail?.branch_id?.toIntOrNull()
}

class Student360ViewModel(
    private val initialStudent: Student?,
    private val studentRepository: StudentRepository = StudentRepository(),
    private val attendanceRepository: AttendanceRepository = AttendanceRepository(),
    private val feeRepository: FeeRepository = FeeRepository(),
    private val examRepository: ExamRepository = ExamRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(
        Student360UiState(
            resolvedStudent = initialStudent,
            resolvingStudent = initialStudent == null,
            isSelfView = initialStudent == null
        )
    )
    val uiState: StateFlow<Student360UiState> = _uiState.asStateFlow()

    init {
        resolveAndLoad(SessionManager.user)
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    private fun resolveAndLoad(sessionUser: User?) {
        viewModelScope.launch {
            if (initialStudent != null) {
                _uiState.update {
                    it.copy(resolvedStudent = initialStudent, resolvingStudent = false)
                }
                loadStudentData(initialStudent)
                return@launch
            }
            if (sessionUser == null) {
                _uiState.update { it.copy(resolvingStudent = false, resolvedStudent = null) }
                return@launch
            }
            _uiState.update { it.copy(resolvingStudent = true) }
            studentRepository.resolveStudentForUser(sessionUser)
                .onSuccess { student ->
                    _uiState.update {
                        it.copy(resolvedStudent = student, resolvingStudent = false)
                    }
                    loadStudentData(student)
                }
                .onFailure {
                    val seed = sessionUser.toStudentSeed()
                    _uiState.update {
                        it.copy(resolvedStudent = seed, resolvingStudent = false)
                    }
                    loadStudentData(seed)
                }
        }
    }

    private suspend fun loadStudentData(currentStudent: Student) {
        _uiState.update { it.copy(isLoading = true, loadError = null) }
        coroutineScope {
            val detailResult = async { studentRepository.getStudentDetail(currentStudent.id) }.await()
            detailResult
                .onSuccess { detail -> _uiState.update { it.copy(detail = detail) } }
                .onFailure { err -> _uiState.update { it.copy(loadError = err.message) } }

            val mergedStudent = currentStudent.mergeWithDetail(detailResult.getOrNull())
            val userIdInt = resolveStudentUserId(mergedStudent, detailResult.getOrNull())

            val attendanceJob = async {
                userIdInt?.let { attendanceRepository.getStudentAttendance(it) }
                    ?: Result.success(emptyList())
            }
            val overviewJob = async {
                userIdInt?.let { attendanceRepository.getStudentOverview(it) }
                    ?: Result.failure(Exception("no user"))
            }
            val feesJob = async {
                userIdInt?.let { feeRepository.getStudentFees(it) }
                    ?: Result.success(StudentFeesData())
            }
            val examsJob = async {
                userIdInt?.let { examRepository.getStudentResults(it) }
                    ?: Result.success(emptyList())
            }

            attendanceJob.await().onSuccess { records ->
                _uiState.update { it.copy(attendanceRecords = records) }
            }
            overviewJob.await().onSuccess { overview ->
                _uiState.update { it.copy(attendanceOverview = overview) }
            }
            feesJob.await().onSuccess { fees ->
                _uiState.update {
                    it.copy(
                        feeDues = fees.dues,
                        feePayments = fees.payments,
                        feeSummary = fees.summary
                    )
                }
            }
            examsJob.await().onSuccess { results ->
                _uiState.update { it.copy(examResults = results) }
            }
        }
        _uiState.update { it.copy(isLoading = false) }
    }
}
