package com.example.myeduapp.features.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.sound.showAppNotification
import com.example.myeduapp.core.ui.components.AppBackTopBar
import com.example.myeduapp.core.ui.components.AppCard
import com.example.myeduapp.core.ui.components.AppLoaderCompact
import com.example.myeduapp.core.ui.components.AppLoaderFullscreen
import com.example.myeduapp.core.ui.theme.CardBackground
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.data.model.Notification

class NotificationsInboxScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        NotificationCenterContent(onBack = { navigator.pop() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsModal(
    onDismiss: () -> Unit,
    onUnreadCount: (Int) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        NotificationsListBody(
            compact = true,
            onUnreadCount = onUnreadCount,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.88f)
        )
    }
}

@Composable
fun NotificationsInboxContent(onBack: () -> Unit, compactBar: Boolean = false) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppBackTopBar(title = "Notifications", onBack = onBack) }
    ) { padding ->
        if (compactBar) {
            CompactNotificationsList(
                modifier = Modifier.fillMaxSize().padding(padding),
                showFilters = true
            )
        } else {
            PagedNotificationsFeed(
                modifier = Modifier.fillMaxSize().padding(padding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsListBody(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onUnreadCount: (Int) -> Unit = {}
) {
    if (compact) {
        CompactNotificationsList(modifier = modifier, onUnreadCount = onUnreadCount)
    } else {
        PagedNotificationsFeed(modifier = modifier, onUnreadCount = onUnreadCount)
    }
}

@Composable
private fun CompactNotificationsList(
    modifier: Modifier,
    onUnreadCount: (Int) -> Unit = {},
    showFilters: Boolean = false
) {
    val screen = LocalNavigator.currentOrThrow.lastItem
    val viewModel = screen.rememberScreenModel(tag = "compact") { CompactNotificationsViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.filter) {
        viewModel.load(onUnreadCount)
    }

    NotificationListChrome(
        modifier = modifier,
        title = "Alerts",
        unreadCount = uiState.unreadCount,
        isLoading = uiState.isLoading,
        items = uiState.notifications,
        emptyText = "No notifications yet",
        filters = if (showFilters) NotificationFilter.entries else emptyList(),
        selectedFilter = uiState.filter,
        onFilter = { viewModel.setFilter(it) },
        onMarkAll = { viewModel.markAllAsRead(onUnreadCount) },
        onOpen = { notification -> viewModel.markOpened(notification, onUnreadCount) }
    )
    NotificationDetailDialog(uiState.opened) { viewModel.openNotification(null) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PagedNotificationsFeed(
    modifier: Modifier,
    onUnreadCount: (Int) -> Unit = {}
) {
    val screen = LocalNavigator.currentOrThrow.lastItem
    val viewModel = screen.rememberScreenModel(tag = "paged") { PagedNotificationsViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.filter) {
        viewModel.reload(onUnreadCount)
    }

    val olderHasMore = uiState.olderHasMore
    val loadingOlder = uiState.loadingOlder
    val shouldLoadMore by remember(olderHasMore, loadingOlder) {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            olderHasMore && !loadingOlder && total > 0 && last >= total - 3
        }
    }
    LaunchedEffect(shouldLoadMore, uiState.filter) {
        if (shouldLoadMore) viewModel.loadMoreIfNeeded()
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Inbox",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (uiState.unreadCount > 0) {
                Badge(containerColor = PrimaryBlue) { Text("${uiState.unreadCount}") }
                TextButton(onClick = { viewModel.markAllAsRead(onUnreadCount) }) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mark all read")
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NotificationFilter.entries.forEach { option ->
                FilterChip(
                    selected = uiState.filter == option,
                    onClick = { viewModel.setFilter(option) },
                    label = { Text(option.label, fontSize = 12.sp) }
                )
            }
        }

        when {
            uiState.loadingToday -> AppLoaderFullscreen(message = "Loading notifications")
            uiState.today.isEmpty() && uiState.older.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No notifications for this filter", color = SecondaryText)
            }
            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    SectionLabel("Today")
                }
                if (uiState.today.isEmpty()) {
                    item {
                        Text(
                            "Nothing new today",
                            fontSize = 13.sp,
                            color = SecondaryText,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                } else {
                    items(uiState.today, key = { "today-${it.id}" }) { notification ->
                        NotificationCard(
                            notification,
                            onClick = { viewModel.markOpened(notification, onUnreadCount) }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionLabel("Earlier")
                }
                if (uiState.older.isEmpty() && !uiState.loadingOlder) {
                    item {
                        Text("No earlier notifications", fontSize = 13.sp, color = SecondaryText)
                    }
                } else {
                    items(uiState.older, key = { "older-${it.id}" }) { notification ->
                        NotificationCard(
                            notification,
                            onClick = { viewModel.markOpened(notification, onUnreadCount) }
                        )
                    }
                }
                if (uiState.loadingOlder) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            AppLoaderCompact(size = 28.dp)
                        }
                    }
                }
            }
        }
    }

    NotificationDetailDialog(uiState.opened) { viewModel.openNotification(null) }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = PrimaryBlue,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationListChrome(
    modifier: Modifier,
    title: String,
    unreadCount: Int,
    isLoading: Boolean,
    items: List<Notification>,
    emptyText: String,
    onMarkAll: () -> Unit,
    onOpen: (Notification) -> Unit,
    filters: List<NotificationFilter> = emptyList(),
    selectedFilter: NotificationFilter = NotificationFilter.All,
    onFilter: (NotificationFilter) -> Unit = {}
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (unreadCount > 0) {
                Badge(containerColor = PrimaryBlue) { Text("$unreadCount", fontSize = 10.sp) }
                TextButton(onClick = onMarkAll) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Read all", fontSize = 12.sp)
                }
            }
        }
        if (filters.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filters.forEach { option ->
                    FilterChip(
                        selected = selectedFilter == option,
                        onClick = { onFilter(option) },
                        label = { Text(option.label, fontSize = 11.sp) },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
        }

        when {
            isLoading -> AppLoaderFullscreen(message = "Loading notifications")
            items.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(emptyText, color = SecondaryText, fontSize = 13.sp)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(items, key = { it.id }) { notification ->
                    NotificationCard(notification, onClick = { onOpen(notification) })
                }
            }
        }
    }
}

@Composable
fun IncomingNotificationBanner(
    notification: Notification,
    onOpen: () -> Unit,
    onDismiss: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onOpen,
        containerColor = SecondaryBlue
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = CardBackground, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("New notification", fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                Text(notification.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Text(notification.message, fontSize = 12.sp, color = SecondaryText, maxLines = 2)
            }
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    onClick: (() -> Unit)? = null
) {
    val unread = !notification.isRead
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        containerColor = if (unread) SecondaryBlue else CardBackground
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (unread) PrimaryBlue else SecondaryText.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (unread) CardBackground else SecondaryText,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        notification.title,
                        fontSize = 13.sp,
                        fontWeight = if (unread) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    if (unread) {
                        Box(
                            modifier = Modifier.size(8.dp).clip(CircleShape).background(PrimaryBlue)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(notification.message, fontSize = 12.sp, color = SecondaryText, maxLines = 2)
                Spacer(modifier = Modifier.height(4.dp))
                val meta = listOfNotNull(
                    notification.source?.takeIf { it.isNotBlank() }?.replaceFirstChar { ch -> ch.uppercase() },
                    notification.displayDate.takeIf { it.isNotBlank() }
                ).joinToString(" · ")
                Text(meta, fontSize = 11.sp, color = SecondaryText.copy(alpha = 0.75f))
            }
        }
    }
}

fun playIncomingNotificationAlert(title: String, message: String) {
    showAppNotification(title, message)
}
