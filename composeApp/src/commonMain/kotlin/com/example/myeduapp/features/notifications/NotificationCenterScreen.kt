package com.example.myeduapp.features.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.ui.components.AppBackTopBar
import com.example.myeduapp.core.ui.components.AppCard
import com.example.myeduapp.core.ui.components.AppLoaderCompact
import com.example.myeduapp.core.ui.components.AppLoaderFullscreen
import com.example.myeduapp.core.ui.theme.CardBackground
import com.example.myeduapp.core.ui.theme.ErrorColor
import com.example.myeduapp.core.ui.theme.OutlineSoft
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.PrimaryText
import com.example.myeduapp.core.ui.theme.SecondaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.ui.theme.SuccessColor
import com.example.myeduapp.core.ui.theme.WarningColor
import com.example.myeduapp.data.model.Notification
import com.example.myeduapp.data.model.UserRole

internal var pendingOpenSentNotifications = false

class NotificationCenterScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        NotificationCenterContent(onBack = { navigator.pop() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterContent(onBack: () -> Unit) {
    val navigator = LocalNavigator.currentOrThrow
    val screen = navigator.lastItem
    val viewModel = screen.rememberScreenModel { NotificationCenterViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val canCompose = SessionManager.user?.userRole in listOf(
        UserRole.TEACHER, UserRole.BRANCH_ADMIN, UserRole.SUPER_ADMIN, UserRole.STAFF
    )

    LaunchedEffect(Unit) {
        if (pendingOpenSentNotifications) {
            pendingOpenSentNotifications = false
            viewModel.setTab(CenterTab.Sent)
        }
    }

    LaunchedEffect(uiState.filter) {
        if (uiState.tab == CenterTab.Inbox) viewModel.reload()
    }
    LaunchedEffect(uiState.tab) {
        viewModel.onTabOrFilterChanged()
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppBackTopBar(
                title = "Notification Center",
                onBack = onBack,
                actions = {
                    if (canCompose) {
                        IconButton(onClick = { navigator.push(ComposeTeacherNotificationScreen()) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Compose", tint = Color.White)
                        }
                    }
                    if (uiState.tab == CenterTab.Inbox && uiState.unreadCount > 0) {
                        IconButton(onClick = { viewModel.markAllAsRead() }) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Mark all read", tint = Color.White)
                        }
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = uiState.tab == CenterTab.Inbox,
                        onClick = { viewModel.setTab(CenterTab.Inbox) },
                        label = { Text("Inbox", fontSize = 12.sp) },
                        modifier = Modifier.height(32.dp)
                    )
                    if (canCompose) {
                        FilterChip(
                            selected = uiState.tab == CenterTab.Sent,
                            onClick = { viewModel.setTab(CenterTab.Sent) },
                            label = { Text("Sent", fontSize = 12.sp) },
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
                if (uiState.tab == CenterTab.Inbox) {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { viewModel.setQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search alerts", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SecondaryText) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = OutlineSoft
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatPill("${uiState.unreadCount} unread", PrimaryBlue)
                        StatPill("${uiState.todayItems.size} today", SuccessColor)
                        StatPill("${uiState.combined.size} loaded", SecondaryText)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CenterFilter.entries.forEach { option ->
                            FilterChip(
                                selected = uiState.filter == option,
                                onClick = { viewModel.setFilter(option) },
                                label = { Text(option.label, fontSize = 11.sp) },
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }
                }
            }

            if (uiState.tab == CenterTab.Sent) {
                when {
                    uiState.loadingSent -> AppLoaderFullscreen(message = "Loading sent notifications")
                    uiState.sentError != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(uiState.sentError ?: "Could not load", color = SecondaryText, fontSize = 13.sp)
                            TextButton(onClick = { viewModel.loadSent() }) { Text("Retry", color = PrimaryBlue) }
                        }
                    }
                    uiState.sent.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No sent notifications", color = SecondaryText, fontSize = 13.sp)
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.sent, key = { it.group_key ?: it.id }) { item ->
                            SentCampaignRow(item, onClick = { viewModel.openNotification(item) })
                        }
                    }
                }
            } else when {
                uiState.loading -> AppLoaderFullscreen(message = "Loading notification center")
                uiState.combined.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No alerts in this view", color = SecondaryText, fontSize = 13.sp)
                    }
                }
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.todayItems.isNotEmpty()) {
                        item { SectionTitle("Today") }
                        items(uiState.todayItems, key = { "t-${it.id}" }) { item ->
                            CenterAlertRow(item, onClick = { viewModel.openItem(item) })
                        }
                    }
                    if (uiState.yesterdayItems.isNotEmpty()) {
                        item { SectionTitle("Yesterday") }
                        items(uiState.yesterdayItems, key = { "y-${it.id}" }) { item ->
                            CenterAlertRow(item, onClick = { viewModel.openItem(item) })
                        }
                    }
                    if (uiState.earlierItems.isNotEmpty()) {
                        item { SectionTitle("Earlier") }
                        items(uiState.earlierItems, key = { "e-${it.id}" }) { item ->
                            CenterAlertRow(item, onClick = { viewModel.openItem(item) })
                        }
                    }
                    if (uiState.loadingOlder) {
                        item {
                            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.Center) {
                                AppLoaderCompact(size = 24.dp)
                            }
                        }
                    }
                }
            }
        }
    }

    NotificationDetailDialog(uiState.opened) { viewModel.openNotification(null) }
}

@Composable
private fun StatPill(text: String, color: Color) {
    Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.12f)) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = PrimaryBlue,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun CenterAlertRow(item: Notification, onClick: () -> Unit) {
    val unread = !item.isRead
    val icon = centerIcon(item)
    val accent = centerAccent(item)
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        containerColor = if (unread) SecondaryBlue else CardBackground
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .background(if (unread) accent else Color.Transparent)
            )
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp).weight(1f),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(accent.copy(alpha = if (unread) 1f else 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = if (unread) Color.White else accent, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            item.title,
                            fontSize = 13.sp,
                            fontWeight = if (unread) FontWeight.Bold else FontWeight.SemiBold,
                            color = PrimaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (unread) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accent))
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        item.description?.trim()?.ifBlank { null } ?: item.message,
                        fontSize = 12.sp,
                        color = SecondaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            listOfNotNull(
                                item.source?.takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() },
                                item.displayDate.takeIf { it.isNotBlank() }
                            ).joinToString(" · "),
                            fontSize = 10.sp,
                            color = SecondaryText
                        )
                        if (item.attachments.isNotEmpty()) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(12.dp))
                        }
                        if (item.priority.equals("High", true) || item.priority.equals("Urgent", true)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = WarningColor, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SentCampaignRow(item: Notification, onClick: () -> Unit) {
    val audience = item.audience
        ?: listOfNotNull(item.grade?.let { "Grade $it" }, item.section).joinToString(" - ").ifBlank { null }
    AppCard(modifier = Modifier.fillMaxWidth(), onClick = onClick, containerColor = CardBackground) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text("Sent", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SuccessColor)
            }
            Text(
                item.description?.trim()?.ifBlank { null } ?: item.message,
                fontSize = 12.sp,
                color = SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                listOfNotNull(
                    audience,
                    item.student_count?.let { "$it students" },
                    item.sent_at ?: item.displayDate
                ).joinToString(" · "),
                fontSize = 10.sp,
                color = SecondaryText
            )
        }
    }
}

private fun centerIcon(item: Notification): ImageVector = when (item.source?.lowercase()) {
    "assignment" -> Icons.AutoMirrored.Filled.Assignment
    "attendance" -> Icons.Default.CheckCircle
    else -> Icons.Default.Notifications
}

private fun centerAccent(item: Notification): Color = when {
    item.source.equals("attendance", true) -> SuccessColor
    item.source.equals("assignment", true) -> PrimaryBlue
    item.type.equals("Warning", true) || item.type.equals("Alert", true) -> WarningColor
    item.type.equals("Error", true) -> ErrorColor
    else -> PrimaryBlue
}
