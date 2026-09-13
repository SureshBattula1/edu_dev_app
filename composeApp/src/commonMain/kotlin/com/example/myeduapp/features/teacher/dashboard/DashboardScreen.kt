package com.example.myeduapp.features.teacher.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myeduapp.data.model.UserRole
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.datastore.AuthState
import com.example.myeduapp.data.repository.DashboardRepository
import com.example.myeduapp.data.model.DashboardResponse
import com.example.myeduapp.data.model.attendancePercent
import com.example.myeduapp.data.model.pendingFeeLabel
import com.example.myeduapp.core.navigation.AppNavigation
import com.example.myeduapp.core.navigation.NavItem
import com.example.myeduapp.core.navigation.Route
import com.example.myeduapp.core.ui.components.AppCard
import com.example.myeduapp.core.ui.components.BigBridzDashboardModuleCard
import com.example.myeduapp.core.ui.components.BigBridzStatCard
import com.example.myeduapp.core.ui.icons.BigBridzIcon
import com.example.myeduapp.core.ui.icons.BigBridzIconRegistry
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    unreadCount: Int = 0
) {
    val authState by SessionManager.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user ?: return
    val role = user.userRole
    val colorScheme = MaterialTheme.colorScheme
    
    val repository = remember { DashboardRepository() }
    var dashboardData by remember { mutableStateOf<DashboardResponse?>(null) }

    LaunchedEffect(Unit) {
        repository.getDashboard().onSuccess {
            dashboardData = it
        }
    }

    Scaffold(
        containerColor = colorScheme.background
    ) { _ ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                GenericHeroHeader(user.name, role, onMenuClick, onNotificationClick, unreadCount)
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    StatsSection(role, dashboardData)

                    Text("QUICK ACTIONS", style = MaterialTheme.typography.headlineMedium, color = colorScheme.onSurface)
                    QuickActionsGrid(role, onNavigate)
                }
            }
        }
    }
}

@Composable
fun GenericHeroHeader(name: String, role: UserRole, onMenuClick: () -> Unit, onNotificationClick: () -> Unit, unreadCount: Int = 0) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
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
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Column {
                Text(
                    "WELCOME BACK,",
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.onPrimary.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
                Text(
                    name.uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = colorScheme.onPrimary,
                    fontSize = 22.sp
                )
                Surface(
                    color = colorScheme.onPrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        role.name.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.onPrimary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatsSection(role: UserRole, data: DashboardResponse?) {
    val stats = data?.data
    val iconRegistry = BigBridzIconRegistry
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (role) {
                UserRole.SUPER_ADMIN -> {
                    DashboardStatCard("Total Students", stats?.overview?.get("total_students")?.let { (it as? JsonPrimitive)?.contentOrNull } ?: "0", iconRegistry.Students, Modifier.weight(1f))
                    DashboardStatCard("Branches", stats?.overview?.get("total_branches")?.let { (it as? JsonPrimitive)?.contentOrNull } ?: "0", iconRegistry.Branches, Modifier.weight(1f))
                }
                UserRole.BRANCH_ADMIN -> {
                    DashboardStatCard("Students", stats?.overview?.get("total_students")?.let { (it as? JsonPrimitive)?.contentOrNull } ?: "0", iconRegistry.Students, Modifier.weight(1f))
                    DashboardStatCard("Teachers", stats?.overview?.get("total_teachers")?.let { (it as? JsonPrimitive)?.contentOrNull } ?: "0", iconRegistry.Teachers, Modifier.weight(1f))
                }
                UserRole.TEACHER -> {
                    DashboardStatCard("My Students", stats?.quick_stats?.get("students")?.let { (it as? JsonPrimitive)?.contentOrNull } ?: "0", iconRegistry.Students, Modifier.weight(1f))
                    val attendance = stats.attendancePercent()
                    DashboardStatCard("Attendance", "$attendance%", iconRegistry.Attendance, Modifier.weight(1f))
                }
                UserRole.ACCOUNTANT -> {
                    DashboardStatCard("Today", stats?.financial?.get("today")?.let { (it as? JsonPrimitive)?.contentOrNull } ?: "₹0", iconRegistry.Payments, Modifier.weight(1f))
                    DashboardStatCard("Pending", stats.pendingFeeLabel(), iconRegistry.Fees, Modifier.weight(1f))
                }
                UserRole.STUDENT -> {
                    val attendance = stats.attendancePercent()
                    DashboardStatCard("Attendance", "$attendance%", iconRegistry.Attendance, Modifier.weight(1f))
                    DashboardStatCard("Pending Fee", stats.pendingFeeLabel(), iconRegistry.Fees, Modifier.weight(1f))
                }
                UserRole.STAFF -> {
                    DashboardStatCard("Total Students", stats?.overview?.get("total_students")?.let { (it as? JsonPrimitive)?.contentOrNull } ?: "0", iconRegistry.Students, Modifier.weight(1f))
                    DashboardStatCard("Today Presence", stats?.attendance?.teachers?.present_days?.toString() ?: "0", iconRegistry.Attendance, Modifier.weight(1f))
                }
                UserRole.PARENT -> {
                    val attendance = stats.attendancePercent()
                    DashboardStatCard("Attendance", "$attendance%", iconRegistry.Attendance, Modifier.weight(1f))
                    DashboardStatCard("Pending Fee", stats.pendingFeeLabel(), iconRegistry.Fees, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun DashboardStatCard(label: String, value: String, icon: BigBridzIcon, modifier: Modifier = Modifier) {
    BigBridzStatCard(
        icon = icon,
        value = value,
        label = label,
        modifier = modifier
    )
}

@Composable
fun QuickActionsGrid(role: UserRole, onNavigate: (String) -> Unit) {
    val items = AppNavigation.getDrawerItems(role).filter { it.route != Route.Dashboard && it.route != Route.Profile }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(4).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    ActionCard(item, Modifier.weight(1f), onNavigate)
                }
                repeat(4 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ActionCard(item: NavItem, modifier: Modifier = Modifier, onNavigate: (String) -> Unit) {
    val icon3D = BigBridzIconRegistry.forRoute(item.route.path)
    BigBridzDashboardModuleCard(
        icon = icon3D,
        title = item.label,
        modifier = modifier,
        onClick = { onNavigate(item.route.path) }
    )
}
