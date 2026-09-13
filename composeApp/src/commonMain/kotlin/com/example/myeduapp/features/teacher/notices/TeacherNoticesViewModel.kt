package com.example.myeduapp.features.teacher.notices

import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.Announcement
import com.example.myeduapp.data.repository.CommunicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherNoticesUiState(
    val noticeList: List<Announcement> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class TeacherNoticesViewModel(
    private val communicationRepository: CommunicationRepository = CommunicationRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(TeacherNoticesUiState())
    val uiState: StateFlow<TeacherNoticesUiState> = _uiState.asStateFlow()

    init {
        loadNotices()
    }

    fun loadNotices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = communicationRepository.getAnnouncements()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    noticeList = result.getOrElse { emptyList() },
                    error = result.exceptionOrNull()?.message?.takeIf { _ -> result.isFailure }
                )
            }
        }
    }
}
