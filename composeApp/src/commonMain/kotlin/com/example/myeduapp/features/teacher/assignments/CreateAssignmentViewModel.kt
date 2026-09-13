package com.example.myeduapp.features.teacher.assignments

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.platform.PickedDocument
import com.example.myeduapp.core.sound.showAppNotification
import com.example.myeduapp.core.util.DateUtils
import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.AssignmentAttachment
import com.example.myeduapp.data.model.CreateAssignmentBody
import com.example.myeduapp.data.model.EligibleStudent
import com.example.myeduapp.data.model.SubjectOption
import com.example.myeduapp.data.model.UpdateAssignmentBody
import com.example.myeduapp.data.repository.AssignmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateAssignmentUiState(
    val subjects: List<SubjectOption> = emptyList(),
    val selectedSubjectId: String? = null,
    val title: String = "",
    val description: String = "",
    val optionalDescription: String = "",
    val dueDate: String = DateUtils.currentLocalDate().toString(),
    val maxMarks: String = "20",
    val assignmentType: String = "Homework",
    val audienceAll: Boolean = true,
    val students: List<EligibleStudent> = emptyList(),
    val selectedStudentIds: Set<String> = emptySet(),
    val loadingStudents: Boolean = false,
    val submitting: Boolean = false,
    val uploading: Boolean = false,
    val loadingDetail: Boolean = false,
    val successTitle: String? = null,
    val successWasUpdate: Boolean = false,
    val attachments: List<AssignmentAttachment> = emptyList(),
    val pendingGrade: String? = null,
    val pendingSection: String? = null,
    val snackbarMessage: String? = null,
    val navigateBack: Boolean = false
)

class CreateAssignmentViewModel(
    private val assignmentId: String? = null,
    private val assignmentRepository: AssignmentRepository = AssignmentRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(
        CreateAssignmentUiState(loadingDetail = !assignmentId.isNullOrBlank())
    )
    val uiState: StateFlow<CreateAssignmentUiState> = _uiState.asStateFlow()

    val isEdit: Boolean get() = !assignmentId.isNullOrBlank()

    init {
        if (!assignmentId.isNullOrBlank()) {
            loadAssignment(assignmentId)
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onOptionalDescriptionChange(value: String) =
        _uiState.update { it.copy(optionalDescription = value) }
    fun onDueDateChange(value: String) = _uiState.update { it.copy(dueDate = value) }
    fun onMaxMarksChange(value: String) =
        _uiState.update { it.copy(maxMarks = value.filter { ch -> ch.isDigit() || ch == '.' }) }
    fun onAssignmentTypeChange(value: String) =
        _uiState.update { it.copy(assignmentType = value) }
    fun onSubjectSelected(subjectId: String?) =
        _uiState.update { it.copy(selectedSubjectId = subjectId) }
    fun onAudienceAllChange(all: Boolean) =
        _uiState.update { it.copy(audienceAll = all) }

    fun toggleStudent(studentId: String) {
        _uiState.update { state ->
            val ids = if (studentId in state.selectedStudentIds) {
                state.selectedStudentIds - studentId
            } else {
                state.selectedStudentIds + studentId
            }
            state.copy(selectedStudentIds = ids)
        }
    }

    fun setStudentChecked(studentId: String, checked: Boolean) {
        _uiState.update { state ->
            val ids = if (checked) {
                state.selectedStudentIds + studentId
            } else {
                state.selectedStudentIds - studentId
            }
            state.copy(selectedStudentIds = ids)
        }
    }

    fun removeAttachment(filePath: String) {
        _uiState.update { state ->
            state.copy(attachments = state.attachments.filterNot { it.file_path == filePath })
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }
    fun clearSuccess() = _uiState.update { it.copy(successTitle = null) }
    fun consumeNavigateBack() = _uiState.update { it.copy(navigateBack = false) }
    fun clearPendingSection() = _uiState.update { it.copy(pendingSection = null) }
    fun clearPendingGrade() = _uiState.update { it.copy(pendingGrade = null) }

    fun loadSubjects(grade: String) {
        viewModelScope.launch {
            assignmentRepository.getSubjects(grade).onSuccess { list ->
                _uiState.update { state ->
                    val keepSubject = isEdit ||
                        state.selectedSubjectId == null ||
                        list.any { it.id == state.selectedSubjectId }
                    state.copy(
                        subjects = list,
                        selectedSubjectId = if (keepSubject) state.selectedSubjectId else null
                    )
                }
            }
        }
    }

    fun loadEligibleStudents(grade: String, section: String) {
        if (isEdit || _uiState.value.audienceAll) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loadingStudents = true,
                    students = emptyList(),
                    selectedStudentIds = emptySet()
                )
            }
            assignmentRepository.getEligibleStudents(grade, section)
                .onSuccess { students ->
                    _uiState.update {
                        it.copy(students = students, loadingStudents = false)
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            loadingStudents = false,
                            snackbarMessage = err.message ?: "Could not load students"
                        )
                    }
                }
        }
    }

    fun uploadFiles(files: List<PickedDocument>) {
        if (files.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(uploading = true) }
            files.forEach { file ->
                assignmentRepository.uploadAttachment(file)
                    .onSuccess { attachment ->
                        _uiState.update { it.copy(attachments = it.attachments + attachment) }
                    }
                    .onFailure { err ->
                        _uiState.update {
                            it.copy(
                                snackbarMessage = err.message ?: "Could not upload ${file.name}"
                            )
                        }
                    }
            }
            _uiState.update { it.copy(uploading = false) }
        }
    }

    fun submit(grade: String, section: String) {
        val state = _uiState.value
        val subjectId = state.selectedSubjectId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(submitting = true) }
            val result = if (isEdit && assignmentId != null) {
                assignmentRepository.updateAssignment(
                    assignmentId,
                    UpdateAssignmentBody(
                        title = state.title.trim(),
                        description = state.description.trim().ifBlank { null },
                        instructions = state.optionalDescription.trim().ifBlank { null },
                        due_date = state.dueDate,
                        max_marks = state.maxMarks.toDoubleOrNull(),
                        assignment_type = state.assignmentType,
                        attachments = state.attachments,
                        is_published = true,
                        notify = true
                    )
                )
            } else {
                assignmentRepository.createAssignment(
                    CreateAssignmentBody(
                        branch_id = SessionManager.user?.branch_id,
                        grade = grade,
                        section = section,
                        subject_id = subjectId,
                        title = state.title.trim(),
                        description = state.description.trim().ifBlank { null },
                        instructions = state.optionalDescription.trim().ifBlank { null },
                        due_date = state.dueDate,
                        max_marks = state.maxMarks.toDoubleOrNull(),
                        assignment_type = state.assignmentType,
                        audience_mode = if (state.audienceAll) "all" else "custom",
                        student_ids = if (state.audienceAll) {
                            emptyList()
                        } else {
                            state.selectedStudentIds.toList()
                        },
                        is_published = true,
                        attachments = state.attachments
                    )
                )
            }
            _uiState.update { it.copy(submitting = false) }
            result.onSuccess { assignment ->
                val updated = isEdit
                showAppNotification(
                    if (updated) "Assignment updated" else "Assignment created",
                    if (updated) {
                        "“${assignment.title}” — students and teachers were notified again."
                    } else {
                        "“${assignment.title}” — students and teachers have been notified."
                    }
                )
                _uiState.update {
                    it.copy(
                        successWasUpdate = updated,
                        successTitle = assignment.title
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(snackbarMessage = err.message ?: "Failed to save assignment")
                }
            }
        }
    }

    private fun loadAssignment(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingDetail = true) }
            assignmentRepository.getAssignment(id)
                .onSuccess { assignment ->
                    if (!assignment.can_edit) {
                        _uiState.update {
                            it.copy(
                                snackbarMessage = "Only the teacher who created this assignment can update it.",
                                navigateBack = true,
                                loadingDetail = false
                            )
                        }
                        return@onSuccess
                    }
                    _uiState.update {
                        it.copy(
                            title = assignment.title,
                            description = assignment.description.orEmpty(),
                            optionalDescription = assignment.instructions.orEmpty(),
                            dueDate = assignment.due_date,
                            maxMarks = assignment.max_marks?.let { marks ->
                                if (marks % 1.0 == 0.0) marks.toInt().toString() else marks.toString()
                            } ?: "20",
                            assignmentType = assignment.assignment_type.ifBlank { "Homework" },
                            selectedSubjectId = assignment.subject_id.ifBlank { null },
                            attachments = assignment.attachments,
                            audienceAll = assignment.audience_mode != "custom",
                            pendingGrade = assignment.grade,
                            pendingSection = assignment.section,
                            loadingDetail = false
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            snackbarMessage = err.message ?: "Could not load assignment",
                            loadingDetail = false
                        )
                    }
                }
        }
    }
}
