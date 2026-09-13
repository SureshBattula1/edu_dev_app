package com.example.myeduapp.features.notifications

import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.Notification
import com.example.myeduapp.data.repository.CommunicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NotificationFilter(val label: String) {
    All("All"),
    Assignment("Assignment"),
    Attendance("Attendance"),
    Unread("Unread"),
    Info("Info"),
    Warning("Warning"),
    Alert("Alert");

    fun toParams(): Triple<String, String?, String?> = when (this) {
        All -> Triple("all", null, null)
        Assignment -> Triple("all", null, "assignment")
        Attendance -> Triple("all", null, "attendance")
        Unread -> Triple("unread", null, null)
        Info -> Triple("all", "Info", null)
        Warning -> Triple("all", "Warning", null)
        Alert -> Triple("all", "Alert", null)
    }
}

data class CompactNotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = true,
    val filter: NotificationFilter = NotificationFilter.All,
    val opened: Notification? = null,
    val unreadCount: Int = 0
)

class CompactNotificationsViewModel(
    private val communicationRepository: CommunicationRepository = CommunicationRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(CompactNotificationsUiState())
    val uiState: StateFlow<CompactNotificationsUiState> = _uiState.asStateFlow()

    fun setFilter(filter: NotificationFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun openNotification(notification: Notification?) {
        _uiState.update { it.copy(opened = notification) }
    }

    fun load(onUnreadCount: (Int) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val (status, type, source) = _uiState.value.filter.toParams()
            communicationRepository.getNotificationsPage(
                status = status,
                type = type,
                source = source,
                perPage = 20
            ).onSuccess { page ->
                val notifications = page.items
                val unread = notifications.count { !it.isRead }
                _uiState.update {
                    it.copy(
                        notifications = notifications,
                        isLoading = false,
                        unreadCount = unread
                    )
                }
                onUnreadCount(unread)
            }.onFailure {
                communicationRepository.getNotifications(unreadOnly = false, limit = 20)
                    .onSuccess { notifications ->
                        val unread = notifications.count { !it.isRead }
                        _uiState.update {
                            it.copy(
                                notifications = notifications,
                                isLoading = false,
                                unreadCount = unread
                            )
                        }
                        onUnreadCount(unread)
                    }
                    .onFailure {
                        _uiState.update { it.copy(isLoading = false) }
                    }
            }
        }
    }

    fun markAllAsRead(onUnreadCount: (Int) -> Unit = {}) {
        viewModelScope.launch {
            communicationRepository.markAllAsRead().onSuccess {
                _uiState.update { state ->
                    state.copy(
                        notifications = state.notifications.map {
                            it.copy(is_read = true, read_at = "now")
                        },
                        unreadCount = 0
                    )
                }
                onUnreadCount(0)
            }
        }
    }

    fun markOpened(notification: Notification, onUnreadCount: (Int) -> Unit = {}) {
        _uiState.update { it.copy(opened = notification) }
        if (!notification.isRead) {
            viewModelScope.launch {
                communicationRepository.markAsRead(notification.id)
                _uiState.update { state ->
                    val notifications = state.notifications.map {
                        if (it.id == notification.id) it.copy(is_read = true, read_at = "now") else it
                    }
                    val unread = notifications.count { !it.isRead }
                    onUnreadCount(unread)
                    state.copy(notifications = notifications, unreadCount = unread)
                }
            }
        }
    }
}

data class PagedNotificationsUiState(
    val filter: NotificationFilter = NotificationFilter.All,
    val today: List<Notification> = emptyList(),
    val older: List<Notification> = emptyList(),
    val olderPage: Int = 1,
    val olderHasMore: Boolean = false,
    val loadingToday: Boolean = true,
    val loadingOlder: Boolean = false,
    val opened: Notification? = null
) {
    val unreadCount: Int
        get() = (today + older).count { !it.isRead }
}

class PagedNotificationsViewModel(
    private val communicationRepository: CommunicationRepository = CommunicationRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(PagedNotificationsUiState())
    val uiState: StateFlow<PagedNotificationsUiState> = _uiState.asStateFlow()

    fun setFilter(filter: NotificationFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun openNotification(notification: Notification?) {
        _uiState.update { it.copy(opened = notification) }
    }

    fun reload(onUnreadCount: (Int) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loadingToday = true,
                    today = emptyList(),
                    older = emptyList(),
                    olderPage = 1,
                    olderHasMore = false
                )
            }
            loadToday()
            loadOlder(reset = true)
            _uiState.update { it.copy(loadingToday = false) }
            onUnreadCount(_uiState.value.unreadCount)
        }
    }

    fun loadMoreIfNeeded() {
        val state = _uiState.value
        if (state.olderHasMore && !state.loadingOlder) {
            viewModelScope.launch { loadOlder(reset = false) }
        }
    }

    private suspend fun loadToday() {
        val (status, type, source) = _uiState.value.filter.toParams()
        communicationRepository.getNotificationsPage(
            status = status,
            type = type,
            source = source,
            period = "today",
            page = 1,
            perPage = 50
        ).onSuccess { page ->
            _uiState.update { it.copy(today = page.items) }
        }
    }

    private suspend fun loadOlder(reset: Boolean) {
        if (_uiState.value.loadingOlder) return
        _uiState.update { it.copy(loadingOlder = true) }
        val nextPage = if (reset) 1 else _uiState.value.olderPage + 1
        val (status, type, source) = _uiState.value.filter.toParams()
        communicationRepository.getNotificationsPage(
            status = status,
            type = type,
            source = source,
            period = "older",
            page = nextPage,
            perPage = 20
        ).onSuccess { page ->
            _uiState.update { state ->
                val older = if (reset) {
                    page.items
                } else {
                    state.older + page.items.filter { item -> state.older.none { it.id == item.id } }
                }
                state.copy(
                    older = older,
                    olderPage = page.page,
                    olderHasMore = page.hasMore,
                    loadingOlder = false
                )
            }
        }.onFailure {
            _uiState.update { it.copy(loadingOlder = false) }
        }
    }

    fun markAllAsRead(onUnreadCount: (Int) -> Unit = {}) {
        viewModelScope.launch {
            communicationRepository.markAllAsRead().onSuccess {
                _uiState.update { state ->
                    state.copy(
                        today = state.today.map { it.copy(is_read = true, read_at = "now") },
                        older = state.older.map { it.copy(is_read = true, read_at = "now") }
                    )
                }
                onUnreadCount(0)
            }
        }
    }

    fun markOpened(notification: Notification, onUnreadCount: (Int) -> Unit = {}) {
        _uiState.update { it.copy(opened = notification) }
        if (!notification.isRead) {
            viewModelScope.launch {
                communicationRepository.markAsRead(notification.id)
                _uiState.update { state ->
                    val today = state.today.map {
                        if (it.id == notification.id) it.copy(is_read = true, read_at = "now") else it
                    }
                    val older = state.older.map {
                        if (it.id == notification.id) it.copy(is_read = true, read_at = "now") else it
                    }
                    val unread = (today + older).count { !it.isRead }
                    onUnreadCount(unread)
                    state.copy(today = today, older = older)
                }
            }
        }
    }
}
