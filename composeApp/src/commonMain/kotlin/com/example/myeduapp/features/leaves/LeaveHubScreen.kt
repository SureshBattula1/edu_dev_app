package com.example.myeduapp.features.leaves

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.ui.theme.LeaveDimens
import com.example.myeduapp.core.ui.theme.LeaveHeroBackground
import com.example.myeduapp.data.model.LeaveCategory
import com.example.myeduapp.data.model.UserRole

class LeaveHubScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        LeaveHubScreenContent(onBack = { navigator.pop() }, onNavigate = { navigator.push(it) })
    }
}

@Composable
fun LeaveHubScreenContent(
    onBack: () -> Unit,
    onNavigate: (Screen) -> Unit
) {
    val user = SessionManager.user
    val role = user?.userRole ?: UserRole.STUDENT
    val colorScheme = MaterialTheme.colorScheme

    val canApplyStudent = LeaveAccess.canApplyStudentLeave(role)
    val canApplyTeacher = LeaveAccess.canApplyTeacherLeave(role)
    val canApproveStudents = LeaveAccess.canApproveStudentLeaves(role)
    val canApproveTeachers = LeaveAccess.canApproveTeacherLeaves(role)

    Scaffold(containerColor = colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Box {
                LeaveHeroBackground()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colorScheme.onPrimary
                            )
                        }
                        Text(
                            "Leave Center",
                            color = colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.width(48.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Hello, ${user?.name ?: "User"}",
                        color = colorScheme.onPrimary.copy(alpha = 0.85f),
                        fontSize = 14.sp
                    )
                    Text(
                        when {
                            canApproveStudents && canApproveTeachers -> "Manage leave requests"
                            canApproveStudents -> "Review student leave requests"
                            canApplyStudent -> "Apply and track your leaves"
                            canApplyTeacher -> "Apply and track your leaves"
                            else -> "Leave management"
                        },
                        color = colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(LeaveDimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (canApplyStudent) {
                    LeaveHubActionCard(
                        title = "My Leaves",
                        subtitle = "Apply leave and view your history",
                        icon = Icons.Default.EventNote,
                        onClick = { onNavigate(MyLeavesScreen(LeaveCategory.STUDENT)) }
                    )
                }

                if (canApplyTeacher) {
                    LeaveHubActionCard(
                        title = "My Leaves",
                        subtitle = "Apply teacher leave and view history",
                        icon = Icons.Default.EventNote,
                        onClick = { onNavigate(MyLeavesScreen(LeaveCategory.TEACHER)) }
                    )
                }

                if (canApproveStudents) {
                    LeaveHubActionCard(
                        title = "Student Approvals",
                        subtitle = "Approve or reject student leave requests",
                        icon = Icons.Default.HowToReg,
                        onClick = { onNavigate(LeaveApprovalsScreen(LeaveCategory.STUDENT)) }
                    )
                }

                if (canApproveTeachers) {
                    LeaveHubActionCard(
                        title = "Teacher Approvals",
                        subtitle = "Approve or reject teacher leave requests",
                        icon = Icons.Default.SupervisorAccount,
                        onClick = { onNavigate(LeaveApprovalsScreen(LeaveCategory.TEACHER)) }
                    )
                }
            }
        }
    }
}
