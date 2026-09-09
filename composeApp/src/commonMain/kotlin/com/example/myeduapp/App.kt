package com.example.myeduapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.transitions.SlideTransition
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.datastore.AuthState
import com.example.myeduapp.features.auth.login.LoginScreen
import com.example.myeduapp.features.auth.splash.SplashScreen
import com.example.myeduapp.features.teacher.dashboard.TeacherDashboardScreen
import com.example.myeduapp.features.teacher.classes.MyClassesScreen
import com.example.myeduapp.features.teacher.students.MyStudentsScreen
import com.example.myeduapp.features.teacher.assignments.AssignmentsScreen
import com.example.myeduapp.features.teacher.exams.*
import com.example.myeduapp.features.teacher.marks.MarksScreen
import com.example.myeduapp.features.teacher.notices.*
import com.example.myeduapp.features.teacher.communication.TeacherCommunicationScreen
import com.example.myeduapp.features.teacher.dashboard.DashboardScreen
import com.example.myeduapp.features.teacher.attendance.*
import com.example.myeduapp.features.teacher.fees.*
import com.example.myeduapp.features.teacher.leaves.*
import com.example.myeduapp.features.teacher.timetable.*
import com.example.myeduapp.features.teacher.profile.*
import com.example.myeduapp.core.ui.theme.MyEduAppTheme
import com.example.myeduapp.ui.screens.PlaceholderScreen
import com.example.myeduapp.core.navigation.AppNavigation
import com.example.myeduapp.core.navigation.Route
import com.example.myeduapp.data.repository.AuthRepository
import com.example.myeduapp.data.model.UserRole
import kotlinx.coroutines.launch

@Composable
fun App() {
    MyEduAppTheme {
        val authState by SessionManager.authState.collectAsState()
        val authRepository = remember { AuthRepository() }
        
        LaunchedEffect(Unit) {
            authRepository.checkAuth()
        }
        
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (authState) {
                is AuthState.Idle, is AuthState.Loading -> {
                    Navigator(SplashScreen())
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

class MainScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authState by SessionManager.authState.collectAsState()
        val authRepository = remember { AuthRepository() }
        val scope = rememberCoroutineScope()
        
        val user = (authState as? AuthState.Authenticated)?.user ?: return
        val role = user.userRole
        
        val navItems = AppNavigation.getBottomNavItems(role)
        val drawerItems = AppNavigation.getDrawerItems(role)

        var showLogoutDialog by remember { mutableStateOf(false) }

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
                        Text("Logout", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        val defaultHomeRoute = AppNavigation.defaultHomeRoute(role)
        val initialSelectedIndex = navItems.indexOfFirst { it.route == defaultHomeRoute }.coerceAtLeast(0)
        var selectedItem by remember { mutableStateOf(initialSelectedIndex) }
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
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
                                    navigator.push(ProfileScreen())
                                }
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(64.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(role.name.replace("_", " "), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        drawerItems.forEach { item ->
                            NavigationDrawerItem(
                                icon = { Icon(item.icon, contentDescription = null) },
                                label = { Text(item.label) },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    when (item.route) {
                                        Route.Dashboard -> { selectedItem = 0 }
                                        Route.MyClasses -> navigator.push(MyClassesScreen())
                                        Route.MyStudents -> navigator.push(MyStudentsScreen())
                                        Route.Assignments -> navigator.push(AssignmentsScreen())
                                        Route.Exams -> navigator.push(TeacherExamsScreen())
                                        Route.Marks -> navigator.push(MarksScreen())
                                        Route.Leaves -> navigator.push(TeacherLeaveScreen())
                                        Route.Timetable -> navigator.push(TeacherTimetableScreen())
                                        Route.Notices -> navigator.push(TeacherNoticesScreen())
                                        Route.Profile -> navigator.push(ProfileScreen())
                                        Route.Attendance -> navigator.push(TeacherAttendanceScreen())
                                        Route.Fees -> navigator.push(TeacherFeesScreen())
                                        else -> navigator.push(PlaceholderScreen(item.label))
                                    }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        NavigationDrawerItem(
                            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                            label = { Text("Logout") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showLogoutDialog = true
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        ) {
            Scaffold(
                bottomBar = {
                    if (navItems.isNotEmpty()) {
                        NavigationBar(
                            modifier = Modifier.height(56.dp),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                            windowInsets = WindowInsets(0, 0, 0, 0)
                        ) {
                            navItems.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label, fontSize = 12.sp) },
                                    selected = selectedItem == index,
                                    onClick = { selectedItem = index }
                                )
                            }
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    val currentItem = navItems.getOrNull(selectedItem)
                    val currentRoute = currentItem?.route ?: Route.Dashboard

                    when (currentRoute) {
                        Route.Dashboard -> {
                            if (role == UserRole.TEACHER) {
                                TeacherDashboardScreen(
                                    user = user,
                                    onNavigate = { route ->
                                        val targetScreen: Screen = when(route) {
                                            Route.Profile.path -> ProfileScreen()
                                            Route.MyClasses.path -> MyClassesScreen()
                                            Route.MyStudents.path -> MyStudentsScreen()
                                            Route.Assignments.path -> AssignmentsScreen()
                                            Route.Exams.path -> TeacherExamsScreen()
                                            Route.Marks.path -> MarksScreen()
                                            Route.Attendance.path -> TeacherAttendanceScreen()
                                            Route.Fees.path -> TeacherFeesScreen()
                                            Route.Leaves.path -> TeacherLeaveScreen()
                                            Route.Timetable.path -> TeacherTimetableScreen()
                                            Route.Notices.path -> TeacherCommunicationScreen()
                                            else -> PlaceholderScreen(route.replace("_", " ").capitalize())
                                        }
                                        navigator.push(targetScreen)
                                    },
                                    onMenuClick = { scope.launch { drawerState.open() } },
                                    onNotificationClick = { /* Handle notifications */ }
                                )
                            } else {
                                DashboardScreen(
                                    onNavigate = { route ->
                                        val targetScreen: Screen = when(route) {
                                            Route.Profile.path -> ProfileScreen()
                                            Route.MyClasses.path -> MyClassesScreen()
                                            Route.MyStudents.path -> MyStudentsScreen()
                                            Route.Attendance.path -> TeacherAttendanceScreen()
                                            Route.Fees.path -> TeacherFeesScreen()
                                            Route.Exams.path -> TeacherExamsScreen()
                                            Route.Leaves.path -> TeacherLeaveScreen()
                                            Route.Timetable.path -> TeacherTimetableScreen()
                                            Route.Notices.path -> TeacherCommunicationScreen()
                                            else -> PlaceholderScreen(route.replace("_", " ").capitalize())
                                        }
                                        navigator.push(targetScreen)
                                    },
                                    onMenuClick = { scope.launch { drawerState.open() } },
                                    onNotificationClick = { /* Handle notifications */ }
                                )
                            }
                        }
                        Route.Attendance -> {
                            TeacherAttendanceScreenContent(
                                onBack = null,
                                onMarkAttendance = { navigator.push(CreateAttendanceScreen()) }
                            )
                        }
                        Route.Fees -> {
                            TeacherFeesScreenContent(onBack = null)
                        }
                        Route.Exams -> {
                            TeacherExamsScreenContent(onBack = null)
                        }
                        Route.Leaves -> {
                            TeacherLeaveScreenContent(onBack = null)
                        }
                        Route.Notices -> {
                            TeacherNoticesScreenContent(onBack = null)
                        }
                        else -> {
                            val title = currentItem?.label ?: "Screen"
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("$title Screen Coming Soon")
                            }
                        }
                    }
                }
            }
        }
    }
}

data class NavigationItem(val label: String, val icon: ImageVector)

fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
