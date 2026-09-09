package com.example.myeduapp.features.teacher.profile

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.datastore.AuthState
import com.example.myeduapp.core.ui.components.AppCard
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.ui.theme.PrimaryText
import com.example.myeduapp.data.model.User
import com.example.myeduapp.data.model.UserRole
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Edit

class ProfileScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        ProfileScreenContent(
            onBack = { navigator.pop() },
            onNavigateToEdit = { navigator.push(EditProfileScreen()) },
            onNavigateToChangePassword = { navigator.push(ChangePasswordScreen()) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    onBack: (() -> Unit)? = null,
    onNavigateToEdit: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {}
) {
    val authState by SessionManager.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user ?: return
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Profile", "Professional", "Activity")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "TEACHER PROFILE", 
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.padding(start = 8.dp).size(40.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToEdit,
                        modifier = Modifier.padding(end = 8.dp).size(40.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue),
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ProfileHeroHeader(user)
            }
            
            item {
                ProfileTabs(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            item {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "TabContent"
                ) { targetTab ->
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            when (targetTab) {
                                0 -> PersonalDetailsSection(user, onNavigateToChangePassword)
                                1 -> ProfessionalDetailsSection(user)
                                2 -> ContentActivitySection(user)
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun ProfileHeroHeader(user: User) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF007CC4), Color(0xFF299EF2))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .padding(4.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = PrimaryBlue
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = user.name.ifBlank { "NA" }.uppercase(),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontSize = 20.sp
            )
            
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(50),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = user.userRole.name.replace("_", " "),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun ProfileTabs(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 24.dp, end = 24.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = PrimaryBlue,
            divider = {},
            indicator = {},
            modifier = Modifier.padding(4.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Tab(
                    selected = isSelected,
                    onClick = { onTabSelected(index) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) PrimaryBlue else Color.Transparent)
                        .height(40.dp),
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isSelected) Color.White else Color(0xFF64748B),
                            maxLines = 1
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1.2f),
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryText
        )
        Text(
            text = ":",
            modifier = Modifier.width(20.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryText
        )
        Text(
            text = value,
            modifier = Modifier.weight(2f),
            style = MaterialTheme.typography.bodyLarge,
            color = SecondaryText
        )
    }
}

@Composable
fun PersonalDetailsSection(user: User, onChangePassword: () -> Unit) {
    Column {
        ProfileInfoRow("Full Name", user.name.ifBlank { "NA" })
        ProfileInfoRow("Employee ID", user.employee_id ?: "NA")
        ProfileInfoRow("Date of Birth", user.dob ?: "NA")
        ProfileInfoRow("Gender", user.gender ?: "NA")
        ProfileInfoRow("Phone", user.phone ?: "NA")
        ProfileInfoRow("Email", user.email.ifBlank { "NA" })
        ProfileInfoRow("Address", user.address ?: "NA")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onChangePassword,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = PrimaryBlue)
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("CHANGE PASSWORD", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun ProfessionalDetailsSection(user: User) {
    Column {
        ProfileInfoRow("Employee ID", user.employee_id ?: "NA")
        ProfileInfoRow("Department", user.department ?: "NA")
        ProfileInfoRow("Designation", user.designation ?: "NA")
        ProfileInfoRow("Qualification", user.qualification ?: "NA")
        ProfileInfoRow("Experience", user.experience ?: "NA")
        ProfileInfoRow("Joining Date", user.joining_date ?: "NA")
        ProfileInfoRow("Subjects", if (user.subjects.isEmpty()) "NA" else user.subjects.joinToString(", "))
        ProfileInfoRow("Classes Assigned", if (user.classes.isEmpty()) "NA" else user.classes.joinToString(", "))
    }
}

@Composable
fun ContentActivitySection(user: User) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        ActivitySectionHeader("SUBJECTS TEACHING")
        if (user.subjects.isEmpty()) {
            Text("NA", style = MaterialTheme.typography.bodyLarge, color = SecondaryText)
        } else {
            SimpleTagCloud(user.subjects)
        }
        
        ActivitySectionHeader("CLASSES")
        if (user.classes.isEmpty()) {
            Text("NA", style = MaterialTheme.typography.bodyLarge, color = SecondaryText)
        } else {
            SimpleTagCloud(user.classes)
        }
        
        ActivitySectionHeader("CONTENT CREATED")
        ActivityGrid(listOf(
            "Assignments" to Icons.AutoMirrored.Filled.Assignment,
            "Study Materials" to Icons.Default.Book,
            "Worksheets" to Icons.Default.Description,
            "Question Papers" to Icons.Default.Quiz
        ))
        
        ActivitySectionHeader("RECENT ACTIVITY")
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Activity remains static or we show a placeholder since backend activity feed isn't wired yet
            RecentActivityItem("No recent activity", "NA", Icons.Default.Info)
        }
    }
}

@Composable
fun ActivitySectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun SimpleTagCloud(tags: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tags.forEach { tag ->
            Surface(
                color = Color(0xFFE8F2FF),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, Color(0xFF007CC4).copy(alpha = 0.2f))
            ) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.titleSmall,
                    color = PrimaryBlue,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ActivityGrid(items: List<Pair<String, ImageVector>>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item ->
                    AppCard(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(item.second, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(item.first, style = MaterialTheme.typography.titleLarge, fontSize = 13.sp)
                        }
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun RecentActivityItem(title: String, time: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(time, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
        }
    }
}
