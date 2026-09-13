package com.example.myeduapp.features.notifications

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.platform.PickedDocument
import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.AssignmentAttachment
import com.example.myeduapp.data.model.BroadcastNotificationBody
import com.example.myeduapp.data.model.BroadcastNotificationResult
import com.example.myeduapp.data.model.EligibleStudent
import com.example.myeduapp.data.repository.AssignmentRepository
import com.example.myeduapp.data.repository.CommunicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ComposeTeacherNotificationUiState(
    val title: String = "",
    val description: String = "",
    val optionalDescription: String = "",
    val audienceAll: Boolean = true,
    val students: List<EligibleStudent> = emptyList(),
    val selectedStudentIds: Set<String> = emptySet(),
    val loadingStudents: Boolean = false,
    val submitting: Boolean = false,
    val uploading: Boolean = false,
    val confirmSend: Boolean = false,
    val attachments: List<AssignmentAttachment> = emptyList(),
    val success: BroadcastNotificationResult? = null,
    val snackbarMessage: String? = null
)

class ComposeTeacherNotificationViewModel(
    private val communicationRepository: CommunicationRepository = CommunicationRepository(),
    private val assignmentRepository: AssignmentRepository = AssignmentRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ComposeTeacherNotificationUiState())
    val uiState: StateFlow<ComposeTeacherNotificationUiState> = _uiState.asStateFlow()

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onOptionalDescriptionChange(value: String) {
        _uiState.update { it.copy(optionalDescription = value) }
    }

    fun setAudienceAll(all: Boolean) {
        _uiState.update { it.copy(audienceAll = all) }
    }

    fun setConfirmSend(show: Boolean) {
        _uiState.update { it.copy(confirmSend = show) }
    }

    fun toggleStudent(studentId: String) {
        _uiState.update { state ->
            val selected = if (studentId in state.selectedStudentIds) {
                state.selectedStudentIds - studentId
            } else {
                state.selectedStudentIds + studentId
            }
            state.copy(selectedStudentIds = selected)
        }
    }

    fun setStudentChecked(studentId: String, checked: Boolean) {
        _uiState.update { state ->
            val selected = if (checked) {
                state.selectedStudentIds + studentId
            } else {
                state.selectedStudentIds - studentId
            }
            state.copy(selectedStudentIds = selected)
        }
    }

    fun removeAttachment(filePath: String) {
        _uiState.update { state ->
            state.copy(attachments = state.attachments.filterNot { it.file_path == filePath })
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun loadStudents(grade: String?, section: String?, audienceAll: Boolean) {
        if (audienceAll) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(students = emptyList(), selectedStudentIds = emptySet())
            }
            if (grade == null || section == null) return@launch
            _uiState.update { it.copy(loadingStudents = true) }
            assignmentRepository.getEligibleStudents(grade, section)
                .onSuccess { students ->
                    _uiState.update { it.copy(students = students, loadingStudents = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            loadingStudents = false,
                            snackbarMessage = error.message ?: "Could not load students"
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
                communicationRepository.uploadAttachment(file)
                    .onSuccess { attachment ->
                        _uiState.update { state ->
                            state.copy(attachments = state.attachments + attachment)
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(snackbarMessage = error.message ?: "Could not upload ${file.name}")
                        }
                    }
            }
            _uiState.update { it.copy(uploading = false) }
        }
    }

    fun sendNow(grade: String, section: String) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(submitting = true) }
            communicationRepository.broadcastNotification(
                BroadcastNotificationBody(
                    title = state.title.trim(),
                    description = state.description.trim(),
                    optional_description = state.optionalDescription.trim().ifBlank { null },
                    grade = grade,
                    section = section,
                    audience_mode = if (state.audienceAll) "all" else "custom",
                    student_ids = if (state.audienceAll) emptyList() else state.selectedStudentIds.toList(),
                    attachments = state.attachments,
                    branch_id = SessionManager.user?.branch_id
                )
            ).onSuccess { result ->
                _uiState.update {
                    it.copy(
                        submitting = false,
                        success = result,
                        snackbarMessage = "Notification sent successfully"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        submitting = false,
                        snackbarMessage = error.message ?: "Failed to send notification"
                    )
                }
            }
        }
    }

    fun resetForm() {
        _uiState.value = ComposeTeacherNotificationUiState()
    }
}
