package com.example.myeduapp.features.teacher.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myeduapp.core.ui.components.AppCard
import com.example.myeduapp.core.ui.components.AppStatCard
import com.example.myeduapp.ui.components.AuthorizationWrapper
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.ui.theme.InfoColor
import com.example.myeduapp.core.ui.theme.SuccessColor
import com.example.myeduapp.core.ui.theme.WarningColor
import com.example.myeduapp.core.ui.theme.ErrorColor
import com.example.myeduapp.data.model.User
import com.example.myeduapp.core.navigation.Route
import com.example.myeduapp.data.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    user: User,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    AuthorizationWrapper(requiredRoles = listOf(UserRole.TEACHER)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("MyEduApp", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = onNotificationClick) {
                            BadgedBox(badge = { Badge { Text("3") } }) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue, titleContentColor = Color.White)
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Summary Cards
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AppStatCard("Total Classes", "6", PrimaryBlue, Modifier.weight(1f))
                        AppStatCard("Total Students", "150", InfoColor, Modifier.weight(1f))
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AppStatCard("Today's Classes", "4", SuccessColor, Modifier.weight(1f))
                        AppStatCard("Attendance", "92%", WarningColor, Modifier.weight(1f))
                    }
                }

                // Quick Actions
                item {
                    Text("Quick Actions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        QuickActionItem("Attendance", Icons.Default.CheckCircle, SuccessColor) { onNavigate(Route.Attendance.path) }
                        QuickActionItem("Students", Icons.Default.Groups, InfoColor) { onNavigate("my_students") }
                        QuickActionItem("Assignments", Icons.AutoMirrored.Filled.Assignment, WarningColor) { onNavigate("assignments") }
                        QuickActionItem("Exams", Icons.Default.Quiz, ErrorColor) { onNavigate(Route.Exams.path) }
                    }
                }

                // Today's Timetable Preview
                item {
                    DashboardSectionHeader("Today's Timetable", "View All") { onNavigate(Route.Timetable.path) }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TimetableMiniCard("09:00 AM", "Mathematics", "Class 10-A", "Room 102")
                        TimetableMiniCard("11:30 AM", "Physics", "Class 12-B", "Lab 2")
                    }
                }

                // Upcoming
                item {
                    Text("Upcoming", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            UpcomingItem("Mid-Term Exam", "Starts in 2 days", Icons.Default.Event, InfoColor)
                            UpcomingItem("Math Assignment", "15 submissions pending", Icons.Default.Description, WarningColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionItem(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f))
        ) {
            Icon(icon, contentDescription = label, tint = color)
        }
        Text(label, fontSize = 12.sp, color = SecondaryText, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun DashboardSectionHeader(title: String, actionText: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        TextButton(onClick = onActionClick) {
            Text(actionText, color = PrimaryBlue)
        }
    }
}

@Composable
fun TimetableMiniCard(time: String, subject: String, className: String, room: String) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(time, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(subject, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text("$className • $room", fontSize = 12.sp, color = SecondaryText)
            }
        }
    }
}

@Composable
fun UpcomingItem(title: String, subtitle: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 12.sp, color = SecondaryText)
        }
    }
}
