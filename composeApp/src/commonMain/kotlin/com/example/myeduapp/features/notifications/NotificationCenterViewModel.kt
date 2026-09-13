package com.example.myeduapp.features.notifications

import com.example.myeduapp.core.util.DateUtils
import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.Notification
import com.example.myeduapp.data.repository.CommunicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CenterTab { Inbox, Sent }

enum class CenterFilter(val label: String) {
    All("All"),
    Unread("Unread"),
    Assignment("Assignment"),
    Attendance("Attendance"),
    Info("Info"),
    Warning("Warning"),
    Alert("Alert");

    fun toParams(): Triple<String, String?, String?> = when (this) {
        All -> Triple("all", null, null)
        Unread -> Triple("unread", null, null)
        Assignment -> Triple("all", null, "assignment")
        Attendance -> Triple("all", null, "attendance")
        Info -> Triple("all", "Info", null)
        Warning -> Triple("all", "Warning", null)
        Alert -> Triple("all", "Alert", null)
    }
}

data class NotificationCenterUiState(
    val tab: CenterTab = CenterTab.Inbox,
    val filter: CenterFilter = CenterFilter.All,
    val query: String = "",
    val today: List<Notification> = emptyList(),
    val older: List<Notification> = emptyList(),
    val olderPage: Int = 1,
    val olderHasMore: Boolean = false,
    val loading: Boolean = true,
    val loadingOlder: Boolean = false,
    val opened: Notification? = null,
    val sent: List<Notification> = emptyList(),
    val loadingSent: Boolean = false,
    val sentError: String? = null
) {
    val combined: List<Notification>
        get() {
            val all = today + older.filter { item -> today.none { it.id == item.id } }
            val q = query.trim()
            return if (q.isBlank()) all
            else all.filter { item ->
                listOf(item.title, item.message, item.description, item.optional_description, item.source)
                    .any { it.orEmpty().contains(q, ignoreCase = true) }
            }
        }

    val todayKey: String get() = DateUtils.today()
    val yesterdayKey: String get() = DateUtils.shiftDate(todayKey, -1)

    val todayItems: List<Notification>
        get() = combined.filter { notificationDay(it) == todayKey }

    val yesterdayItems: List<Notification>
        get() = combined.filter { notificationDay(it) == yesterdayKey }

    val earlierItems: List<Notification>
        get() = combined.filter {
            val day = notificationDay(it)
            day != todayKey && day != yesterdayKey
        }

    val unreadCount: Int
        get() = combined.count { !it.isRead }
}

class NotificationCenterViewModel(
    initialTab: CenterTab = CenterTab.Inbox,
    private val communicationRepository: CommunicationRepository = CommunicationRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(NotificationCenterUiState(tab = initialTab))
    val uiState: StateFlow<NotificationCenterUiState> = _uiState.asStateFlow()

    fun setTab(tab: CenterTab) {
        _uiState.update { it.copy(tab = tab) }
    }

    fun setFilter(filter: CenterFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun setQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun openNotification(notification: Notification?) {
        _uiState.update { it.copy(opened = notification) }
    }

    fun onTabOrFilterChanged() {
        val tab = _uiState.value.tab
        if (tab == CenterTab.Sent) loadSent() else reload()
    }

    fun reload() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loading = true,
                    today = emptyList(),
                    older = emptyList(),
                    olderPage = 1,
                    olderHasMore = false
                )
            }
            loadToday()
            loadOlder(reset = true)
            _uiState.update { it.copy(loading = false) }
        }
    }

    fun loadSent() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingSent = true, sentError = null) }
            communicationRepository.getSentNotifications()
                .onSuccess { sent ->
                    _uiState.update { it.copy(sent = sent, loadingSent = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            loadingSent = false,
                            sentError = error.message ?: "Could not load sent notifications"
                        )
                    }
                }
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
        communicationRepository.getNotificationsPage(status, type, source, "today", 1, 50)
            .onSuccess { page ->
                _uiState.update { it.copy(today = page.items) }
            }
    }

    private suspend fun loadOlder(reset: Boolean) {
        if (_uiState.value.loadingOlder) return
        _uiState.update { it.copy(loadingOlder = true) }
        val next = if (reset) 1 else _uiState.value.olderPage + 1
        val (status, type, source) = _uiState.value.filter.toParams()
        communicationRepository.getNotificationsPage(status, type, source, "older", next, 20)
            .onSuccess { page ->
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

    fun markAllAsRead() {
        viewModelScope.launch {
            communicationRepository.markAllAsRead().onSuccess {
                _uiState.update { state ->
                    state.copy(
                        today = state.today.map { it.copy(is_read = true, read_at = "now") },
                        older = state.older.map { it.copy(is_read = true, read_at = "now") }
                    )
                }
            }
        }
    }

    fun openItem(item: Notification) {
        _uiState.update { it.copy(opened = item) }
        if (!item.isRead) {
            viewModelScope.launch {
                communicationRepository.markAsRead(item.id)
                _uiState.update { state ->
                    state.copy(
                        today = state.today.map {
                            if (it.id == item.id) it.copy(is_read = true, read_at = "now") else it
                        },
                        older = state.older.map {
                            if (it.id == item.id) it.copy(is_read = true, read_at = "now") else it
                        }
                    )
                }
            }
        }
    }

    fun refresh() {
        if (_uiState.value.tab == CenterTab.Sent) loadSent() else reload()
    }
}

internal fun notificationDay(item: Notification): String {
    val raw = item.displayDate.trim()
    return if (raw.length >= 10) raw.take(10) else raw
}
