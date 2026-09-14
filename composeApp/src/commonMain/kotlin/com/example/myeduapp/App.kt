package com.example.myeduapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.myeduapp.core.ui.theme.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.transitions.SlideTransition
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.datastore.AuthState
import com.example.myeduapp.features.auth.login.LoginScreen
import com.example.myeduapp.features.auth.splash.SplashContent
import com.example.myeduapp.features.teacher.dashboard.TeacherDashboardScreen
import com.example.myeduapp.features.teacher.students.MyStudentsScreen
import com.example.myeduapp.features.teacher.teachers.MyTeachersScreen
import com.example.myeduapp.features.teacher.assignments.AssignmentsScreen
import com.example.myeduapp.features.notifications.IncomingNotificationBanner
import com.example.myeduapp.features.notifications.NotificationCenterScreen
import com.example.myeduapp.data.model.Notification
import com.example.myeduapp.data.repository.CommunicationRepository
import com.example.myeduapp.core.sound.showAppNotification
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.myeduapp.features.teacher.exams.*
import com.example.myeduapp.features.teacher.marks.MarksScreen
import com.example.myeduapp.features.teacher.notices.*
import com.example.myeduapp.features.teacher.communication.TeacherCommunicationScreen
import com.example.myeduapp.features.teacher.dashboard.DashboardScreen
import com.example.myeduapp.features.teacher.attendance.*
import com.example.myeduapp.features.attendance.AttendanceHubScreen
import com.example.myeduapp.features.teacher.fees.*
import com.example.myeduapp.features.teacher.leaves.TeacherLeaveScreen
import com.example.myeduapp.features.leaves.LeaveHubScreen
import com.example.myeduapp.features.teacher.timetable.*
import com.example.myeduapp.features.teacher.profile.*
import com.example.myeduapp.features.teacher.student360.Student360Screen
import com.example.myeduapp.core.ui.theme.MyEduAppTheme
import com.example.myeduapp.ui.screens.PlaceholderScreen
import com.example.myeduapp.core.navigation.AppNavigation
import com.example.myeduapp.core.navigation.Route
import com.example.myeduapp.core.ui.components.NetworkAvatar
import com.example.myeduapp.core.ui.icons.BigBridz3DIcon
import com.example.myeduapp.core.ui.icons.BigBridzIconRegistry
import com.example.myeduapp.data.repository.AuthRepository
import com.example.myeduapp.data.repository.StudentRepository
import com.example.myeduapp.data.model.User
import com.example.myeduapp.data.model.UserRole
import com.example.myeduapp.features.admin.branches.BranchesScreen
import com.example.myeduapp.features.admin.branches.BranchDetailScreen
import kotlinx.coroutines.launch

@Composable
fun App() {
    MyEduAppTheme {
        val authState by SessionManager.authState.collectAsState()
        val authRepository = remember { AuthRepository() }
        var splashDone by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            authRepository.checkAuth()
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (!splashDone) {
                SplashContent(onFinished = { splashDone = true })
            } else {
                when (authState) {
                    is AuthState.Idle, is AuthState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is AuthState.Authenticated -> {
                        Navigator(MainScreen()) { navigator ->
                            SlideTransition(navigator)
                        }
                    }
                    else -> {
                        Navigator(LoginScreen()) { navigator ->
                            SlideTransition(navigator)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerProfileAvatar(user: User, role: UserRole) {
    var avatarUrl by remember(user.id) { mutableStateOf(user.avatar?.takeIf { it.isNotBlank() }) }
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(user.id, user.avatar, role) {
        avatarUrl = user.avatar?.takeIf { it.isNotBlank() }
        if (avatarUrl == null && role == UserRole.STUDENT) {
            StudentRepository().resolveStudentForUser(user)
                .onSuccess { avatarUrl = it.avatar?.takeIf { a -> a.isNotBlank() } }
        }
    }

    NetworkAvatar(
        url = avatarUrl,
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(colorScheme.primary.copy(alpha = 0.1f)),
        contentDescription = user.name,
        placeholder = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    )
}

class MainScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authState by SessionManager.authState.collectAsState()
        val authRepository = remember { AuthRepository() }
        val scope = rememberCoroutineScope()
        val colorScheme = MaterialTheme.colorScheme
        
        val user = (authState as? AuthState.Authenticated)?.user ?: return
        val role = user.userRole
        
        val drawerItems = AppNavigation.getDrawerItems(role)

        var showLogoutDialog by remember { mutableStateOf(false) }
        var unreadCount by remember { mutableStateOf(0) }
        var incomingAlert by remember { mutableStateOf<Notification?>(null) }
        var knownUnreadIds by remember { mutableStateOf<Set<String>>(emptySet()) }
        val communicationRepository = remember { CommunicationRepository() }

        LaunchedEffect(user.id) {
            var primed = false
            while (true) {
                communicationRepository.getNotifications(unreadOnly = true, limit = 20).onSuccess { unread ->
                    val ids = unread.map { it.id }.toSet()
                    unreadCount = unread.size
                    if (primed) {
                        val newest = unread.firstOrNull { it.id !in knownUnreadIds }
                        if (newest != null) {
                            showAppNotification(newest.title, newest.message)
                            incomingAlert = newest
                        }
                    }
                    knownUnreadIds = ids
                    primed = true
                }
                delay(8_000)
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Logout") },
                text = { Text("Are you sure you want to log out from MyEduApp?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            scope.launch {
                                authRepository.logout()
                            }
                        }
                    ) {
                        Text("Logout", color = colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = CardBackground,
                    drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch { drawerState.close() }
                                    if (role == UserRole.STUDENT) {
                                        navigator.push(Student360Screen())
                                    } else {
                                        navigator.push(ProfileScreen())
                                    }
                                }
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                        ) {
                            DrawerProfileAvatar(user = user, role = role)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryText)
                            Text(role.name.replace("_", " "), style = MaterialTheme.typography.bodyMedium, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                            Text("BigBridz Schools", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = OutlineSoft)
                        
                        drawerItems.forEach { item ->
                            val icon3D = BigBridzIconRegistry.forRoute(item.route.path)
                            NavigationDrawerItem(
                                icon = {
                                    BigBridz3DIcon(
                                        icon = icon3D,
                                        size = 24.dp
                                    )
                                },
                                label = { Text(item.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    when (item.route) {
                                        Route.Dashboard -> { /* Already on dashboard root */ }
                                        Route.Branches -> navigator.push(BranchesScreen())
                                        Route.MyStudents -> navigator.push(MyStudentsScreen())
                                        Route.Teachers -> navigator.push(MyTeachersScreen())
                                        Route.Assignments -> navigator.push(AssignmentsScreen())
                                        Route.Notifications -> navigator.push(NotificationCenterScreen())
                                        Route.Exams -> navigator.push(TeacherExamsScreen())
                                        Route.Marks -> navigator.push(MarksScreen())
                                        Route.Leaves -> navigator.push(LeaveHubScreen())
                                        Route.Timetable -> navigator.push(TeacherTimetableScreen())
                                        Route.Notices -> navigator.push(TeacherNoticesScreen())
                                        Route.Profile -> navigator.push(ProfileScreen())
                                        Route.Student360 -> navigator.push(Student360Screen())
                                        Route.Attendance -> navigator.push(AttendanceHubScreen())
                                        Route.Fees -> navigator.push(TeacherFeesScreen())
                                        else -> navigator.push(PlaceholderScreen(item.label))
                                    }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedIconColor = colorScheme.onSurfaceVariant,
                                    unselectedTextColor = PrimaryText
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        NavigationDrawerItem(
                            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                            label = { Text("Logout", style = MaterialTheme.typography.bodyMedium) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showLogoutDialog = true
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedIconColor = colorScheme.error,
                                unselectedTextColor = colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (role == UserRole.TEACHER) {
                    TeacherDashboardScreen(
                        user = user,
                        onNavigate = { route ->
                            val targetScreen: Screen = when(route) {
                                Route.Profile.path -> ProfileScreen()
                                Route.MyStudents.path -> MyStudentsScreen()
                                Route.Teachers.path -> MyTeachersScreen()
                                Route.Assignments.path -> AssignmentsScreen()
                                Route.Notifications.path -> NotificationCenterScreen()
                                Route.Exams.path -> TeacherExamsScreen()
                                Route.Marks.path -> MarksScreen()
                                Route.Attendance.path -> AttendanceHubScreen()
                                Route.Fees.path -> TeacherFeesScreen()
                                Route.Leaves.path -> LeaveHubScreen()
                                Route.Timetable.path -> TeacherTimetableScreen()
                                Route.Notices.path -> TeacherCommunicationScreen()
                                else -> PlaceholderScreen(route.replace("_", " ").capitalize())
                            }
                            navigator.push(targetScreen)
                        },
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onNotificationClick = { navigator.push(NotificationCenterScreen()) },
                        unreadCount = unreadCount
                    )
                } else {
                    DashboardScreen(
                        onNavigate = { route ->
                            val targetScreen: Screen = when(route) {
                                Route.Branches.path -> BranchesScreen()
                                Route.Profile.path -> ProfileScreen()
                                Route.Student360.path -> Student360Screen()
                                Route.MyStudents.path -> MyStudentsScreen()
                                Route.Teachers.path -> MyTeachersScreen()
                                Route.Assignments.path -> AssignmentsScreen()
                                Route.Notifications.path -> NotificationCenterScreen()
                                Route.Attendance.path -> AttendanceHubScreen()
                                Route.Fees.path -> TeacherFeesScreen()
                                Route.Exams.path -> TeacherExamsScreen()
                                Route.Leaves.path -> LeaveHubScreen()
                                Route.Timetable.path -> TeacherTimetableScreen()
                                Route.Notices.path -> TeacherCommunicationScreen()
                                else -> PlaceholderScreen(route.replace("_", " ").capitalize())
                            }
                            navigator.push(targetScreen)
                        },
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onNotificationClick = { navigator.push(NotificationCenterScreen()) },
                        unreadCount = unreadCount
                    )
                }

                incomingAlert?.let { alert ->
                    Box(
                        modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).statusBarsPadding()
                    ) {
                        IncomingNotificationBanner(
                            notification = alert,
                            onOpen = {
                                incomingAlert = null
                                navigator.push(NotificationCenterScreen())
                            },
                            onDismiss = { incomingAlert = null }
                        )
                    }
                }
            }
        }
    }
}

data class NavigationItem(val label: String, val icon: ImageVector)

fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
