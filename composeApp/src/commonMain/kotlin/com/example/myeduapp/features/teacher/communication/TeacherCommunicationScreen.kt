package com.example.myeduapp.features.teacher.communication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.data.model.Notification
import com.example.myeduapp.data.model.Holiday
import com.example.myeduapp.data.repository.CommunicationRepository
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.ui.theme.InfoColor
import com.example.myeduapp.core.ui.theme.WarningColor

class TeacherCommunicationScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TeacherCommunicationScreenContent(onBack = { navigator.pop() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherCommunicationScreenContent(onBack: (() -> Unit)? = null) {
    val repository = remember { CommunicationRepository() }
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var holidays by remember { mutableStateOf<List<Holiday>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    
    val tabs = listOf("Notifications", "Holidays")

    LaunchedEffect(Unit) {
        isLoading = true
        val notifResult = repository.getNotifications()
        val holidayResult = repository.getHolidays()
        
        if (notifResult.isSuccess) notifications = notifResult.getOrNull() ?: emptyList()
        if (holidayResult.isSuccess) holidays = holidayResult.getOrNull() ?: emptyList()
        
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Communications") },
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = PrimaryBlue
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> NotificationsList(notifications)
                    1 -> HolidaysList(holidays)
                }
            }
        }
    }
}

@Composable
fun NotificationsList(notifications: List<Notification>) {
    if (notifications.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No notifications")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notifications) { notification ->
                NotificationCard(notification)
            }
        }
    }
}

@Composable
fun HolidaysList(holidays: List<Holiday>) {
    if (holidays.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No upcoming holidays")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(holidays) { holiday ->
                HolidayCard(holiday)
            }
        }
    }
}

@Composable
fun NotificationCard(notification: Notification) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (notification.is_read) Color.White else InfoColor.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(InfoColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = InfoColor)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(notification.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(notification.message, fontSize = 14.sp, color = SecondaryText)
                Spacer(modifier = Modifier.height(8.dp))
                Text(notification.date, fontSize = 11.sp, color = SecondaryText.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun HolidayCard(holiday: Holiday) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(WarningColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.BeachAccess, contentDescription = null, tint = WarningColor)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(holiday.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(holiday.date, fontSize = 14.sp, color = SecondaryText)
                holiday.description?.let {
                    Text(it, fontSize = 12.sp, color = SecondaryText)
                }
            }
        }
    }
}
