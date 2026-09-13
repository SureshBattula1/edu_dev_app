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
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.transitions.SlideTransition
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.datastore.AuthState
import com.example.myeduapp.features.auth.AppViewModel
import com.example.myeduapp.features.auth.login.LoginScreen
import com.example.myeduapp.features.auth.splash.SplashContent
import com.example.myeduapp.features.main.MainViewModel
import com.example.myeduapp.features.teacher.dashboard.TeacherDashboardScreen
import com.example.myeduapp.features.teacher.students.MyStudentsScreen
import com.example.myeduapp.features.teacher.teachers.MyTeachersScreen
import com.example.myeduapp.features.teacher.assignments.AssignmentsScreen
import com.example.myeduapp.features.notifications.IncomingNotificationBanner
import com.example.myeduapp.features.notifications.NotificationCenterScreen
import kotlinx.coroutines.launch
import com.example.myeduapp.features.teacher.exams.*
import com.example.myeduapp.features.teacher.marks.MarksScreen
import com.example.myeduapp.features.teacher.notices.*
import com.example.myeduapp.features.teacher.communication.TeacherCommunicationScreen
import com.example.myeduapp.features.teacher.dashboard.DashboardScreen
import com.example.myeduapp.features.attendance.AttendanceHubScreen
import com.example.myeduapp.features.teacher.fees.*
import com.example.myeduapp.features.leaves.LeaveHubScreen
import com.example.myeduapp.features.teacher.timetable.*
import com.example.myeduapp.features.teacher.profile.*
import com.example.myeduapp.features.teacher.student360.Student360Screen
import com.example.myeduapp.core.ui.theme.MyEduAppTheme
import com.example.myeduapp.ui.screens.PlaceholderScreen
import com.example.myeduapp.core.navigation.AppNavigation
import com.example.myeduapp.core.navigation.Route
import com.example.myeduapp.core.ui.components.NetworkAvatar
import com.example.myeduapp.data.model.User
import com.example.myeduapp.data.model.UserRole

@Composable
fun App() {
    MyEduAppTheme {
        val authState by SessionManager.authState.collectAsState()
        val appViewModel = remember { AppViewModel() }
        var splashDone by remember { mutableStateOf(false) }

        DisposableEffect(appViewModel) {
            onDispose { appViewModel.clear() }
        }

        LaunchedEffect(Unit) {
            appViewModel.checkAuth()
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
private fun DrawerProfileAvatar(user: User, avatarUrl: String?) {
    val colorScheme = MaterialTheme.colorScheme
    val resolvedUrl = avatarUrl ?: user.avatar?.takeIf { it.isNotBlank() }

    NetworkAvatar(
        url = resolvedUrl,
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
        val viewModel = rememberScreenModel { MainViewModel() }
        val uiState by viewModel.uiState.collectAsState()
        val scope = rememberCoroutineScope()
        val colorScheme = MaterialTheme.colorScheme

        val user = (authState as? AuthState.Authenticated)?.user ?: return
        val role = user.userRole

        val drawerItems = AppNavigation.getDrawerItems(role)

        LaunchedEffect(user.id) {
            viewModel.startUnreadPolling(user.id.toString())
            viewModel.resolveDrawerAvatar(user, role)
        }

        if (uiState.showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showLogoutDialog(false) },
                title = { Text("Logout") },
                text = { Text("Are you sure you want to log out from MyEduApp?") },
                confirmButton = {
                    TextButton(onClick = { viewModel.logout() }) {
                        Text("Logout", color = colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showLogoutDialog(false) }) {
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
                    drawerContainerColor = colorScheme.surface,
                    drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
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
                                .padding(24.dp)
                        ) {
                            DrawerProfileAvatar(user = user, avatarUrl = uiState.drawerAvatarUrl)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(user.name, style = MaterialTheme.typography.titleLarge, color = colorScheme.onSurface)
                            Text(role.name.replace("_", " "), style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = colorScheme.outline.copy(alpha = 0.5f))

                        drawerItems.forEach { item ->
                            NavigationDrawerItem(
                                icon = { Icon(item.icon, contentDescription = null) },
                                label = { Text(item.label, style = MaterialTheme.typography.bodyMedium) },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    when (item.route) {
                                        Route.Dashboard -> { /* Already on dashboard root */ }
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
                                    unselectedTextColor = colorScheme.onSurface
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
                                viewModel.showLogoutDialog(true)
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
                            val targetScreen: Screen = when (route) {
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
                        unreadCount = uiState.unreadCount
                    )
                } else {
                    DashboardScreen(
                        onNavigate = { route ->
                            val targetScreen: Screen = when (route) {
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
                        unreadCount = uiState.unreadCount
                    )
                }

                uiState.incomingAlert?.let { alert ->
                    Box(
                        modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).statusBarsPadding()
                    ) {
                        IncomingNotificationBanner(
                            notification = alert,
                            onOpen = {
                                viewModel.dismissIncomingAlert()
                                navigator.push(NotificationCenterScreen())
                            },
                            onDismiss = { viewModel.dismissIncomingAlert() }
                        )
                    }
                }
            }
        }
    }
}

data class NavigationItem(val label: String, val icon: ImageVector)

fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
