package com.example.myeduapp.features.teacher.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myeduapp.data.model.UserRole
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.datastore.AuthState
import com.example.myeduapp.data.repository.DashboardRepository
import com.example.myeduapp.data.model.DashboardResponse
import com.example.myeduapp.data.model.DashboardEvent
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.ui.theme.InfoColor
import com.example.myeduapp.core.ui.theme.SuccessColor
import com.example.myeduapp.core.ui.theme.WarningColor
import com.example.myeduapp.core.ui.theme.ErrorColor
import com.example.myeduapp.core.navigation.AppNavigation
import com.example.myeduapp.core.navigation.NavItem
import com.example.myeduapp.core.navigation.Route
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    val authState by SessionManager.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user ?: return
    val role = user.userRole
    
    val repository = remember { DashboardRepository() }
    var dashboardData by remember { mutableStateOf<DashboardResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repository.getDashboard().onSuccess {
            dashboardData = it
            isLoading = false
        }.onFailure {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("MyEduApp", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onNotificationClick) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    WelcomeSection(user.name, role)
                }
                
                item {
                    StatsSection(role, dashboardData)
                }
                
                item {
                    QuickActionsGrid(role, onNavigate)
                }
            }
        }
    }
}

@Composable
fun WelcomeSection(name: String, role: UserRole) {
    Column {
        Text("Welcome back,", fontSize = 16.sp, color = SecondaryText)
        Text(name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(role.name.replace("_", " "), fontSize = 14.sp, color = PrimaryBlue, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StatsSection(role: UserRole, data: DashboardResponse?) {
    val stats = data?.data
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (role) {
                UserRole.SUPER_ADMIN -> {
                    StatCard("Total Students", stats?.overview?.get("total_students")?.let { (it as? JsonPrimitive)?.contentOrNull } ?: "0", PrimaryBlue, Modifier.weight(1f))
                    StatCard("Branches", stats?.overview?.get("total_branches")?.let { (it as? JsonPrimitive)?.contentOrNull } ?: "0", InfoColor, Modifier.weight(1f))
                }
                UserRole.BRANCH_ADMIN -> {
                    StatCard("Students", stats?.overview?.get("total_students")?.let { (it as? JsonPrimitive)?.contentOrNull } ?: "0", PrimaryBlue, Modifier.weight(1f))
                    StatCard("Teachers", stats?.overview?.get("total_teachers")?.let { (it as? JsonPrimitive)?.contentOrNull } ?: "0", InfoColor, Modifier.weight(1f))
                }
                UserRole.TEACHER -> {
                    StatCard("My Classes", stats?.quick_stats?.get("classes")?.let { (it as? JsonPrimitive)?.contentOrNull } ?: "0", PrimaryBlue, Modifier.weight(1f))
                    StatCard("My Students", stats?.quick_stats?.get("students")?.let { (it as? JsonPrimitive)?.contentOrNull } ?: "0", InfoColor, Modifier.weight(1f))
                }
                UserRole.ACCOUNTANT -> {
                    StatCard("Today", stats?.financial?.get("today")?.let { (it as? JsonPrimitive)?.contentOrNull } ?: "$0", SuccessColor, Modifier.weight(1f))
                    StatCard("Pending", stats?.fees?.get("total_pending")?.let { (it as? JsonPrimitive)?.contentOrNull } ?: "$0", ErrorColor, Modifier.weight(1f))
                }
                UserRole.STUDENT -> {
                    val attendance = stats?.attendance?.students?.percentage ?: 0f
                    StatCard("Attendance", "${attendance.toInt()}%", if (attendance > 75) SuccessColor else ErrorColor, Modifier.weight(1f))
                    StatCard("Pending Fee", stats?.fees?.get("total_pending")?.let { (it as? JsonPrimitive)?.contentOrNull } ?: "$0", ErrorColor, Modifier.weight(1f))
                }
                UserRole.STAFF -> {
                    StatCard("Total Students", stats?.overview?.get("total_students")?.let { (it as? JsonPrimitive)?.contentOrNull } ?: "0", PrimaryBlue, Modifier.weight(1f))
                    StatCard("Today Presence", stats?.attendance?.teachers?.present_days?.toString() ?: "0", SuccessColor, Modifier.weight(1f))
                }
                UserRole.PARENT -> {
                    StatCard("Children", stats?.overview?.get("total_students")?.let { (it as? JsonPrimitive)?.contentOrNull } ?: "0", PrimaryBlue, Modifier.weight(1f))
                    StatCard("Pending Fee", stats?.fees?.get("total_pending")?.let { (it as? JsonPrimitive)?.contentOrNull } ?: "$0", ErrorColor, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 12.sp, color = SecondaryText)
        }
    }
}

@Composable
fun QuickActionsGrid(role: UserRole, onNavigate: (String) -> Unit) {
    val items = AppNavigation.getDrawerItems(role).filter { it.route != Route.Dashboard && it.route != Route.Profile }
    
    Column {
        Text("Quick Actions", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        
        // Using a simple Column/Row layout since LazyVerticalGrid inside LazyColumn is tricky
        items.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    ActionCard(item, Modifier.weight(1f), onNavigate)
                }
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ActionCard(item: NavItem, modifier: Modifier = Modifier, onNavigate: (String) -> Unit) {
    Card(
        modifier = modifier.clickable { onNavigate(item.route.path) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = item.label, tint = PrimaryBlue)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(item.label, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        }
    }
}
