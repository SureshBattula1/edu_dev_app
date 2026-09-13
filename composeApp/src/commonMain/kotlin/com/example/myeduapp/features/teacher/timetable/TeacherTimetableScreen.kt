package com.example.myeduapp.features.teacher.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.myeduapp.data.model.TimetableSlot
import com.example.myeduapp.data.repository.TimetableRepository
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.datastore.AuthState
import com.example.myeduapp.core.ui.components.AppLoaderFullscreen
import com.example.myeduapp.core.ui.components.BigBridzEmptyState
import com.example.myeduapp.core.ui.icons.BigBridzIcon
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText

class TeacherTimetableScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TeacherTimetableScreenContent(onBack = { navigator.pop() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherTimetableScreenContent(onBack: (() -> Unit)? = null) {
    val authState by SessionManager.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user ?: return
    
    val repository = remember { TimetableRepository() }
    var slots by remember { mutableStateOf<List<TimetableSlot>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedDay by remember { mutableStateOf("Monday") }
    
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    LaunchedEffect(Unit) {
        // Use user's actual grade/section if available, otherwise default to 1/A for demo
        val grade = user.branch_id?.toString() ?: "1" // Just a placeholder logic
        repository.getTimetable(grade, "A").onSuccess {
            slots = it
            isLoading = false
        }.onFailure {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timetable") },
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
            ScrollableTabRow(
                selectedTabIndex = days.indexOf(selectedDay),
                containerColor = Color.White,
                contentColor = PrimaryBlue,
                edgePadding = 16.dp
            ) {
                days.forEach { day ->
                    Tab(
                        selected = selectedDay == day,
                        onClick = { selectedDay = day },
                        text = { Text(day) }
                    )
                }
            }
            
            if (isLoading) {
                AppLoaderFullscreen(message = "Loading timetable")
            } else {
                val daySlots = slots.filter { it.day.equals(selectedDay, ignoreCase = true) }
                
                if (daySlots.isEmpty()) {
                    BigBridzEmptyState(
                        icon = BigBridzIcon.Timetable,
                        title = "No Classes Scheduled",
                        message = "No timetable slots found for $selectedDay."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(daySlots) { slot ->
                            TimetableCard(slot)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimetableCard(slot: TimetableSlot) {
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(slot.start_time, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                Text("to", fontSize = 10.sp, color = SecondaryText)
                Text(slot.end_time, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0xFFEEEEEE)))
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column {
                Text(slot.subject, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = SecondaryText)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(slot.teacher ?: "Unknown", fontSize = 12.sp, color = SecondaryText)
                    
                    slot.room?.let {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Default.Room, contentDescription = null, modifier = Modifier.size(14.dp), tint = SecondaryText)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(it, fontSize = 12.sp, color = SecondaryText)
                    }
                }
            }
        }
    }
}
