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
import com.example.myeduapp.data.model.User
import com.example.myeduapp.core.navigation.Route
import com.example.myeduapp.core.ui.components.BigBridzDashboardModuleCard
import com.example.myeduapp.data.model.UserRole
import com.example.myeduapp.core.ui.filters.rememberAcademicYearFilter
import com.example.myeduapp.core.ui.icons.BigBridzIconRegistry

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
    val colorScheme = MaterialTheme.colorScheme

    AuthorizationWrapper(requiredRoles = listOf(UserRole.TEACHER)) {
        Scaffold(
            containerColor = colorScheme.background
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
                            AppStatCard("Total Classes", "6", colorScheme.primary, Modifier.weight(1f))
                            AppStatCard("Total Students", "150", colorScheme.secondary, Modifier.weight(1f))
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            AppStatCard("Today's Classes", "4", colorScheme.tertiary, Modifier.weight(1f))
                            AppStatCard("Attendance", "92%", colorScheme.error, Modifier.weight(1f))
                        }

                        // Quick Actions
                        Text("QUICK ACTIONS", style = MaterialTheme.typography.headlineMedium, color = colorScheme.onSurface)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            QuickActionItem("Attendance", Icons.Default.CheckCircle, colorScheme.primary) { onNavigate(Route.Attendance.path) }
                            QuickActionItem("Students", Icons.Default.Groups, colorScheme.secondary) { onNavigate("my_students") }
                            QuickActionItem("Assignments", Icons.AutoMirrored.Filled.Assignment, colorScheme.tertiary) { onNavigate("assignments") }
                            QuickActionItem("Exams", Icons.Default.Quiz, colorScheme.error) { onNavigate(Route.Exams.path) }
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
                                    color = colorScheme.primaryContainer.copy(alpha = 0.12f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        BadgedBox(badge = {
                                            if (unreadCount > 0) Badge { Text(if (unreadCount > 9) "9+" else "$unreadCount") }
                                        }) {
                                            Icon(Icons.Default.Notifications, contentDescription = null, tint = colorScheme.primary)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Notification Center", style = MaterialTheme.typography.titleLarge, fontSize = 16.sp, color = colorScheme.onSurface)
                                    Text(
                                        if (unreadCount > 0) "$unreadCount unread · open the full center"
                                        else "Search, filters, and all alerts",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onSurfaceVariant
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
                        Text("UPCOMING", style = MaterialTheme.typography.headlineMedium, color = colorScheme.onSurface)
                        AppCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                UpcomingItem("Mid-Term Exam", "Starts in 2 days", Icons.Default.Event, colorScheme.primary)
                                UpcomingItem("Math Assignment", "15 submissions pending", Icons.Default.Description, colorScheme.tertiary)
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
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(colorScheme.primary, colorScheme.secondary)
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
                    modifier = Modifier.size(40.dp).background(colorScheme.onPrimary.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = colorScheme.onPrimary)
                }
                
                Surface(
                    color = colorScheme.onPrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        academicYearLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.onPrimary,
                        fontSize = 10.sp
                    )
                }

                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.size(40.dp).background(colorScheme.onPrimary.copy(alpha = 0.2f), CircleShape)
                ) {
                    BadgedBox(badge = {
                        if (unreadCount > 0) Badge { Text(if (unreadCount > 9) "9+" else "$unreadCount") }
                    }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = colorScheme.onPrimary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(60.dp).background(colorScheme.onPrimary.copy(alpha = 0.2f), CircleShape).padding(2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(colorScheme.surface, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "WELCOME BACK,",
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.onPrimary.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                    Text(
                        user.name.uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = colorScheme.onPrimary,
                        fontSize = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionItem(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    val icon3D = BigBridzIconRegistry.forRoute(label)
    BigBridzDashboardModuleCard(
        icon = icon3D,
        title = label,
        onClick = onClick
    )
}

@Composable
fun DashboardSectionHeader(title: String, actionText: String, onActionClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = colorScheme.onSurface)
        TextButton(onClick = onActionClick) {
            Text(actionText, color = colorScheme.primary, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun TimetableMiniCard(time: String, subject: String, className: String, room: String) {
    val colorScheme = MaterialTheme.colorScheme
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(time, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Box(modifier = Modifier.width(1.dp).height(32.dp).background(colorScheme.outline.copy(alpha = 0.5f)))
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(subject, style = MaterialTheme.typography.titleLarge, color = colorScheme.onSurface)
                Text("$className • $room", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun UpcomingItem(title: String, subtitle: String, icon: ImageVector, color: Color) {
    val colorScheme = MaterialTheme.colorScheme
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
            Text(title, style = MaterialTheme.typography.titleLarge, fontSize = 14.sp, color = colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
        }
    }
}
