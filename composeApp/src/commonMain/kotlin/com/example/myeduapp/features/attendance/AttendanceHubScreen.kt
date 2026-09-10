package com.example.myeduapp.features.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.ui.theme.AttendanceHeroBackground
import com.example.myeduapp.core.ui.theme.Background
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SuccessColor
import com.example.myeduapp.core.ui.theme.WarningColor
import com.example.myeduapp.data.model.UserRole
import com.example.myeduapp.features.teacher.attendance.TeacherAttendanceOverviewScreen
import com.example.myeduapp.features.teacher.attendance.TeacherAttendanceScreen

class AttendanceHubScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val user = SessionManager.user
        val role = user?.userRole ?: UserRole.STUDENT

        Scaffold(
            containerColor = Background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Box {
                    AttendanceHeroBackground()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Text("Attendance Center", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(Modifier.width(48.dp))
                        }
                        Spacer(Modifier.height(20.dp))
                        Text("Hello, ${user?.name ?: "User"}", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                        Text(
                            when (role) {
                                UserRole.TEACHER -> "Manage class & personal attendance"
                                UserRole.STUDENT -> "Track your attendance records"
                                UserRole.STAFF -> "View & mark your attendance"
                                else -> "Attendance overview"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (role) {
                        UserRole.TEACHER -> {
                            Text("CLASS ATTENDANCE", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 12.sp)
                            AttendanceActionCard(
                                title = "Today’s Attendance",
                                subtitle = "Class/section overview — tap to mark or update",
                                icon = Icons.Default.EditCalendar,
                                accent = SuccessColor,
                                onClick = { navigator.push(TeacherAttendanceOverviewScreen()) }
                            )
                            AttendanceActionCard(
                                title = "View Attendance",
                                subtitle = "Filter by class, section & date",
                                icon = Icons.Default.Visibility,
                                accent = PrimaryBlue,
                                onClick = { navigator.push(TeacherAttendanceScreen()) }
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("MY ATTENDANCE", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 12.sp)
                            AttendanceActionCard(
                                title = "View My Attendance",
                                subtitle = "Your personal attendance history",
                                icon = Icons.Default.Person,
                                accent = PrimaryBlue,
                                onClick = { navigator.push(ViewTeacherSelfAttendanceScreen()) }
                            )
                            AttendanceActionCard(
                                title = "Mark My Attendance",
                                subtitle = "Submit today's self attendance",
                                icon = Icons.Default.CheckCircle,
                                accent = WarningColor,
                                onClick = { navigator.push(MarkTeacherSelfAttendanceScreen()) }
                            )
                        }
                        UserRole.STUDENT -> {
                            AttendanceActionCard(
                                title = "View My Attendance",
                                subtitle = "Daily records, percentage & summary",
                                icon = Icons.Default.CalendarMonth,
                                accent = PrimaryBlue,
                                onClick = { navigator.push(ViewStudentAttendanceScreen()) }
                            )
                        }
                        UserRole.STAFF, UserRole.ACCOUNTANT -> {
                            AttendanceActionCard(
                                title = "View My Attendance",
                                subtitle = "Your staff attendance records",
                                icon = Icons.Default.Visibility,
                                accent = PrimaryBlue,
                                onClick = { navigator.push(ViewTeacherSelfAttendanceScreen()) }
                            )
                            AttendanceActionCard(
                                title = "Mark My Attendance",
                                subtitle = "Submit today's attendance status",
                                icon = Icons.Default.EditCalendar,
                                accent = SuccessColor,
                                onClick = { navigator.push(MarkTeacherSelfAttendanceScreen()) }
                            )
                        }
                        else -> {
                            AttendanceActionCard(
                                title = "Today’s Attendance",
                                subtitle = "Class/section overview — tap to mark or update",
                                icon = Icons.Default.EditCalendar,
                                accent = SuccessColor,
                                onClick = { navigator.push(TeacherAttendanceOverviewScreen()) }
                            )
                            AttendanceActionCard(
                                title = "View Class Attendance",
                                subtitle = "Filter by class, section & date",
                                icon = Icons.Default.Groups,
                                accent = PrimaryBlue,
                                onClick = { navigator.push(TeacherAttendanceScreen()) }
                            )
                        }
                    }
                }
            }
        }
    }
}
