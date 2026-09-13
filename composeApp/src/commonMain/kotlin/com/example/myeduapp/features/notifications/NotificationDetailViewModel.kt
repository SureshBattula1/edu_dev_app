package com.example.myeduapp.features.notifications

import com.example.myeduapp.core.viewmodel.BaseViewModel
import com.example.myeduapp.data.model.NotificationReceipts
import com.example.myeduapp.data.repository.CommunicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationDetailUiState(
    val receipts: NotificationReceipts? = null,
    val loadingViews: Boolean = false
)

class NotificationDetailViewModel(
    private val communicationRepository: CommunicationRepository = CommunicationRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(NotificationDetailUiState())
    val uiState: StateFlow<NotificationDetailUiState> = _uiState.asStateFlow()

    fun loadReceipts(groupKey: String?, showViews: Boolean) {
        if (!showViews || groupKey.isNullOrBlank()) {
            _uiState.update { NotificationDetailUiState() }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loadingViews = true, receipts = null) }
            communicationRepository.getNotificationReceipts(groupKey)
                .onSuccess { receipts ->
                    _uiState.update { it.copy(receipts = receipts, loadingViews = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(loadingViews = false) }
                }
        }
    }
}
