package com.example.myeduapp.features.main

import com.example.myeduapp.core.sound.showAppNotification
import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.Notification
import com.example.myeduapp.data.model.User
import com.example.myeduapp.data.model.UserRole
import com.example.myeduapp.data.repository.AuthRepository
import com.example.myeduapp.data.repository.CommunicationRepository
import com.example.myeduapp.data.repository.StudentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MainUiState(
    val unreadCount: Int = 0,
    val incomingAlert: Notification? = null,
    val drawerAvatarUrl: String? = null,
    val showLogoutDialog: Boolean = false
)

class MainViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val communicationRepository: CommunicationRepository = CommunicationRepository(),
    private val studentRepository: StudentRepository = StudentRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var knownUnreadIds: Set<String> = emptySet()
    private var pollingStartedForUserId: String? = null

    fun startUnreadPolling(userId: String) {
        if (pollingStartedForUserId == userId) return
        pollingStartedForUserId = userId
        viewModelScope.launch {
            var primed = false
            while (isActive) {
                communicationRepository.getNotifications(unreadOnly = true, limit = 20).onSuccess { unread ->
                    val ids = unread.map { it.id }.toSet()
                    val newest = if (primed) unread.firstOrNull { it.id !in knownUnreadIds } else null
                    if (newest != null) {
                        showAppNotification(newest.title, newest.message)
                        _uiState.update { it.copy(incomingAlert = newest, unreadCount = unread.size) }
                    } else {
                        _uiState.update { it.copy(unreadCount = unread.size) }
                    }
                    knownUnreadIds = ids
                    primed = true
                }
                delay(8_000)
            }
        }
    }

    fun resolveDrawerAvatar(user: User, role: UserRole) {
        viewModelScope.launch {
            var url = user.avatar?.takeIf { it.isNotBlank() }
            if (url == null && role == UserRole.STUDENT) {
                studentRepository.resolveStudentForUser(user)
                    .onSuccess { url = it.avatar?.takeIf { a -> a.isNotBlank() } }
            }
            _uiState.update { it.copy(drawerAvatarUrl = url) }
        }
    }

    fun showLogoutDialog(show: Boolean) {
        _uiState.update { it.copy(showLogoutDialog = show) }
    }

    fun dismissIncomingAlert() {
        _uiState.update { it.copy(incomingAlert = null) }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(showLogoutDialog = false) }
            authRepository.logout()
        }
    }
}
