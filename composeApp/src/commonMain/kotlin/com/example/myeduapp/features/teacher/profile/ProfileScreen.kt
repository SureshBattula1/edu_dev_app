package com.example.myeduapp.features.teacher.profile

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.datastore.AuthState
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.ui.theme.PrimaryText
import com.example.myeduapp.core.ui.components.AppCard
import com.example.myeduapp.data.model.User
import com.example.myeduapp.data.model.UserRole

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
    val tabs = listOf("Profile", "Professional Details", "Content & Activity")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "Teacher Profile", 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(
                            Icons.Default.Edit, 
                            contentDescription = "Edit",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                ),
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
                TeacherProfileHeader(user)
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
                            .padding(horizontal = 16.dp),
                        elevation = 2f
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            when (targetTab) {
                                0 -> PersonalDetailsSection(user, onNavigateToChangePassword)
                                1 -> ProfessionalDetailsSection(user)
                                2 -> ContentActivitySection()
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
fun TeacherProfileHeader(user: User) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        // Blue Background overlapping part
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(PrimaryBlue)
        )
        
        // Avatar
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = PrimaryBlue
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = user.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            
            Text(
                text = user.userRole.name.replace("_", " "),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryBlue
            )
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
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 4.dp,
        color = Color.White
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
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (isSelected) PrimaryBlue else Color.Transparent)
                        .height(36.dp),
                    text = {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else PrimaryText,
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
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = PrimaryText
        )
        Text(
            text = ":",
            modifier = Modifier.width(20.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = PrimaryText
        )
        Text(
            text = value,
            modifier = Modifier.weight(2f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = SecondaryText
        )
    }
}

@Composable
fun PersonalDetailsSection(user: User, onChangePassword: () -> Unit) {
    Column {
        ProfileInfoRow("Full Name", user.name)
        ProfileInfoRow("Employee ID", "EMP001")
        ProfileInfoRow("Date of Birth", "15 Aug 1995")
        ProfileInfoRow("Gender", "Male")
        ProfileInfoRow("Phone", user.phone ?: "Not Provided")
        ProfileInfoRow("Email", user.email)
        ProfileInfoRow("Address", "123 School Lane, Education City")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedButton(
            onClick = onChangePassword,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFEEEEEE))
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryBlue)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Change Password", color = PrimaryText)
        }
    }
}

@Composable
fun ProfessionalDetailsSection(user: User) {
    Column {
        ProfileInfoRow("Employee ID", "EMP001")
        ProfileInfoRow("Department", "Mathematics")
        ProfileInfoRow("Designation", "Senior Teacher")
        ProfileInfoRow("Qualification", "M.Sc, B.Ed")
        ProfileInfoRow("Experience", "5 Years")
        ProfileInfoRow("Joining Date", "10 Jun 2021")
        ProfileInfoRow("Subjects", "Mathematics, Science")
        ProfileInfoRow("Classes Assigned", "Grade 6, Grade 7")
        ProfileInfoRow("Class Teacher", "Grade 7-A")
    }
}

@Composable
fun ContentActivitySection() {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        ActivitySectionHeader("Subjects Teaching")
        SimpleTagCloud(listOf("Mathematics", "Science"))
        
        ActivitySectionHeader("Classes")
        SimpleTagCloud(listOf("Grade 6", "Grade 7"))
        
        ActivitySectionHeader("Content Created")
        ActivityGrid(listOf(
            "Assignments" to Icons.AutoMirrored.Filled.Assignment,
            "Study Materials" to Icons.Default.Book,
            "Worksheets" to Icons.Default.Description,
            "Question Papers" to Icons.Default.Quiz
        ))
        
        ActivitySectionHeader("Recent Activity")
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            RecentActivityItem("Assignment created", "10 mins ago", Icons.Default.AddCircle)
            RecentActivityItem("Attendance updated", "2 hours ago", Icons.Default.CheckCircle)
            RecentActivityItem("Study material uploaded", "Yesterday", Icons.Default.CloudUpload)
        }
    }
}

@Composable
fun ActivitySectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = PrimaryText,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun SimpleTagCloud(tags: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tags.forEach { tag ->
            Surface(
                color = PrimaryBlue.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.1f))
            ) {
                Text(
                    text = tag,
                    fontSize = 13.sp,
                    color = PrimaryBlue,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFF0F0F0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(item.second, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(item.first, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F7FA)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(time, fontSize = 12.sp, color = SecondaryText)
        }
    }
}
