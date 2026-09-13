package com.example.myeduapp.features.teacher.communication

import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.Holiday
import com.example.myeduapp.data.model.Notification
import com.example.myeduapp.data.repository.CommunicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherCommunicationUiState(
    val notifications: List<Notification> = emptyList(),
    val holidays: List<Holiday> = emptyList(),
    val selectedTab: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class TeacherCommunicationViewModel(
    private val communicationRepository: CommunicationRepository = CommunicationRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(TeacherCommunicationUiState())
    val uiState: StateFlow<TeacherCommunicationUiState> = _uiState.asStateFlow()

    init {
        loadCommunications()
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun loadCommunications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val notifResult = communicationRepository.getNotifications(unreadOnly = false, limit = 20)
            val holidayResult = communicationRepository.getHolidays()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    notifications = notifResult.getOrElse { emptyList() },
                    holidays = holidayResult.getOrElse { emptyList() }
                )
            }
        }
    }

    fun markAsRead(notification: Notification) {
        viewModelScope.launch {
            communicationRepository.markAsRead(notification.id)
            _uiState.update { state ->
                state.copy(
                    notifications = state.notifications.map {
                        if (it.id == notification.id) it.copy(is_read = true, read_at = "now") else it
                    }
                )
            }
        }
    }
}
