package com.example.myeduapp.features.teacher.student360

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.ui.components.AppCard
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.data.model.Student

@OptIn(ExperimentalMaterial3Api::class)
class Student360Screen(private val student: Student) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("Overview", "Attendance", "Fees", "Exams", "Marks")

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Student 360") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PrimaryBlue,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                StudentHeader(student)
                
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = PrimaryBlue,
                    edgePadding = 16.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
                
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                    when (selectedTab) {
                        0 -> StudentOverviewTab(student)
                        else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("$ ${tabs[selectedTab]} Coming Soon")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentHeader(student: Student) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(student.full_name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    "Class ${student.grade}-${student.section} • Roll: ${student.roll_number}",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    "Admission No: ${student.admission_number}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun StudentOverviewTab(student: Student) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryMiniCard("Attendance", "92%", PrimaryBlue, Modifier.weight(1f))
                SummaryMiniCard("Avg Marks", "84%", Color(0xFF4CAF50), Modifier.weight(1f))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryMiniCard("Assignments", "8 / 10", Color(0xFFFFC107), Modifier.weight(1f))
                SummaryMiniCard("Performance", "Improving", Color(0xFF2196F3), Modifier.weight(1f))
            }
        }
        
        item {
            Text("Latest Activity", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        item {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActivityItem("Marked Present", "Today, 09:15 AM", Icons.Default.CheckCircle, Color(0xFF4CAF50))
                    ActivityItem("Submitted Math Assignment", "Yesterday", Icons.AutoMirrored.Filled.Assignment, Color(0xFFFFC107))
                    ActivityItem("Scored 85/100 in Physics", "2 days ago", Icons.Default.Grade, Color(0xFF2196F3))
                }
            }
        }
    }
}

@Composable
fun SummaryMiniCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 12.sp, color = SecondaryText)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun ActivityItem(title: String, time: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(time, fontSize = 12.sp, color = SecondaryText)
        }
    }
}
