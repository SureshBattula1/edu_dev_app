package com.example.myeduapp.features.teacher.notices

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.data.model.Announcement
import com.example.myeduapp.data.repository.CommunicationRepository
import com.example.myeduapp.core.ui.components.AppLoaderFullscreen
import com.example.myeduapp.core.ui.components.BigBridzEmptyState
import com.example.myeduapp.core.ui.components.BigBridzErrorState
import com.example.myeduapp.core.ui.icons.BigBridzIcon
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText

class TeacherNoticesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TeacherNoticesScreenContent(onBack = { navigator.pop() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherNoticesScreenContent(onBack: (() -> Unit)? = null) {
    val repository = remember { CommunicationRepository() }
    var noticeList by remember { mutableStateOf<List<Announcement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        repository.getAnnouncements().onSuccess {
            noticeList = it
            isLoading = false
        }.onFailure {
            error = it.message
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notices & Announcements") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                AppLoaderFullscreen(message = "Loading notices")
            } else if (error != null) {
                BigBridzErrorState(
                    title = "Error Loading Notices",
                    message = error ?: "Failed to retrieve announcements.",
                    onRetry = {
                        isLoading = true
                    }
                )
            } else if (noticeList.isEmpty()) {
                BigBridzEmptyState(
                    icon = BigBridzIcon.Notices,
                    title = "No Notices Found",
                    message = "No school announcements or notices have been published."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(noticeList) { notice ->
                        NoticeCard(notice)
                    }
                }
            }
        }
    }
}

@Composable
fun NoticeCard(notice: Announcement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryBlue)
            Spacer(modifier = Modifier.height(4.dp))
            Text(notice.content, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                notice.author?.let {
                    Text("By: $it", fontSize = 12.sp, color = SecondaryText)
                }
                Text(notice.published_at, fontSize = 12.sp, color = SecondaryText)
            }
        }
    }
}
