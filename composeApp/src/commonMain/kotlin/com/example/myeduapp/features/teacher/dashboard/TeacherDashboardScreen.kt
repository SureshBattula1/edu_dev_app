package com.example.myeduapp.features.teacher.dashboard

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.example.myeduapp.core.ui.filters.rememberAcademicYearFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    user: User,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    unreadCount: Int = 0
) {
    val academicYearFilter = rememberAcademicYearFilter()
    val academicYearLabel = academicYearFilter.selectedYear?.name?.uppercase() ?: "ACADEMIC YEAR"

    AuthorizationWrapper(requiredRoles = listOf(UserRole.TEACHER)) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { _ ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Sunrise Academic Hero Header
                item {
                    TeacherHeroHeader(user, academicYearLabel, onMenuClick, onNotificationClick, unreadCount)
                }

                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Summary Cards
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            AppStatCard("Total Classes", "6", PrimaryBlue, Modifier.weight(1f))
                            AppStatCard("Total Students", "150", InfoColor, Modifier.weight(1f))
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            AppStatCard("Today's Classes", "4", SuccessColor, Modifier.weight(1f))
                            AppStatCard("Attendance", "92%", WarningColor, Modifier.weight(1f))
                        }

                        // Quick Actions
                        Text("QUICK ACTIONS", style = MaterialTheme.typography.headlineMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            QuickActionItem("Attendance", Icons.Default.CheckCircle, SuccessColor) { onNavigate(Route.Attendance.path) }
                            QuickActionItem("Students", Icons.Default.Groups, InfoColor) { onNavigate("my_students") }
                            QuickActionItem("Assignments", Icons.AutoMirrored.Filled.Assignment, WarningColor) { onNavigate("assignments") }
                            QuickActionItem("Exams", Icons.Default.Quiz, ErrorColor) { onNavigate(Route.Exams.path) }
                        }

                        AppCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onNavigate(Route.Notifications.path) }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = InfoColor.copy(alpha = 0.12f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        BadgedBox(badge = {
                                            if (unreadCount > 0) Badge { Text(if (unreadCount > 9) "9+" else "$unreadCount") }
                                        }) {
                                            Icon(Icons.Default.Notifications, contentDescription = null, tint = InfoColor)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Notification Center", style = MaterialTheme.typography.titleLarge, fontSize = 16.sp)
                                    Text(
                                        if (unreadCount > 0) "$unreadCount unread · open the full center"
                                        else "Search, filters, and all alerts",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SecondaryText
                                    )
                                }
                            }
                        }

                        // Today's Timetable Preview
                        DashboardSectionHeader("TODAY'S TIMETABLE", "View All") { onNavigate(Route.Timetable.path) }
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            TimetableMiniCard("09:00 AM", "Mathematics", "Class 10-A", "Room 102")
                            TimetableMiniCard("11:30 AM", "Physics", "Class 12-B", "Lab 2")
                        }

                        // Upcoming
                        Text("UPCOMING", style = MaterialTheme.typography.headlineMedium)
                        AppCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                UpcomingItem("Mid-Term Exam", "Starts in 2 days", Icons.Default.Event, InfoColor)
                                UpcomingItem("Math Assignment", "15 submissions pending", Icons.Default.Description, WarningColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherHeroHeader(
    user: User,
    academicYearLabel: String,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    unreadCount: Int = 0
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF007CC4), Color(0xFF299EF2))
                )
            )
            .padding(horizontal = 24.dp)
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                }
                
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        academicYearLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }

                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    BadgedBox(badge = {
                        if (unreadCount > 0) Badge { Text(if (unreadCount > 9) "9+" else "$unreadCount") }
                    }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(60.dp).background(Color.White.copy(alpha = 0.2f), CircleShape).padding(2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "WELCOME BACK,",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                    Text(
                        user.name.uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontSize = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionItem(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(20.dp),
            color = color.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(28.dp))
            }
        }
        Text(
            label, 
            style = MaterialTheme.typography.bodySmall, 
            color = SecondaryText, 
            modifier = Modifier.padding(top = 8.dp),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DashboardSectionHeader(title: String, actionText: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        TextButton(onClick = onActionClick) {
            Text(actionText, color = PrimaryBlue, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun TimetableMiniCard(time: String, subject: String, className: String, room: String) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(time, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)))
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(subject, style = MaterialTheme.typography.titleLarge)
                Text("$className • $room", style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
            }
        }
    }
}

@Composable
fun UpcomingItem(title: String, subtitle: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleLarge, fontSize = 14.sp)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
        }
    }
}
