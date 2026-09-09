package com.example.myeduapp.features.teacher.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.data.model.Attendance
import com.example.myeduapp.data.model.SchoolClass
import com.example.myeduapp.data.repository.AttendanceRepository
import com.example.myeduapp.data.repository.ClassRepository
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.datastore.AuthState
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.ui.theme.SuccessColor
import com.example.myeduapp.core.ui.theme.ErrorColor
import com.example.myeduapp.core.ui.theme.WarningColor
import com.example.myeduapp.core.ui.theme.InfoColor
import kotlinx.coroutines.launch

class TeacherAttendanceScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TeacherAttendanceScreenContent(
            onBack = { navigator.pop() },
            onMarkAttendance = { navigator.push(CreateAttendanceScreen()) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAttendanceScreenContent(onBack: (() -> Unit)? = null, onMarkAttendance: () -> Unit = {}) {
    val attendanceRepository = remember { AttendanceRepository() }
    val classRepository = remember { ClassRepository() }
    
    var classes by remember { mutableStateOf<List<SchoolClass>>(emptyList()) }
    var selectedClass by remember { mutableStateOf<SchoolClass?>(null) }
    var attendanceRecords by remember { mutableStateOf<List<Attendance>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedDate by remember { mutableStateOf("2026-09-08") } // Mock today
    
    val scope = rememberCoroutineScope()

    // Load classes on start
    LaunchedEffect(Unit) {
        classRepository.getMyClasses().onSuccess {
            classes = it
            selectedClass = it.firstOrNull()
        }
    }

    // Load attendance when class or date changes
    LaunchedEffect(selectedClass, selectedDate) {
        selectedClass?.let {
            isLoading = true
            attendanceRepository.getClassAttendance(it.id, selectedDate).onSuccess { response ->
                attendanceRecords = response.attendance
                isLoading = false
            }.onFailure {
                attendanceRecords = emptyList()
                isLoading = false
            }
        } ?: run {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Class Attendance") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onMarkAttendance) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Mark Attendance", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Class Selector
            if (classes.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = classes.indexOf(selectedClass).coerceAtLeast(0),
                    containerColor = Color.White,
                    contentColor = PrimaryBlue,
                    edgePadding = 16.dp
                ) {
                    classes.forEach { schoolClass ->
                        Tab(
                            selected = selectedClass == schoolClass,
                            onClick = { selectedClass = schoolClass },
                            text = { Text(schoolClass.name) }
                        )
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else {
                if (attendanceRecords.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.GroupOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No students found for this class.", color = SecondaryText)
                            TextButton(onClick = onMarkAttendance) {
                                Text("Mark Attendance Now", color = PrimaryBlue)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            AttendanceSummaryHeader(attendanceRecords)
                        }
                        
                        items(attendanceRecords) { record ->
                            StudentAttendanceRow(record)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceSummaryHeader(records: List<Attendance>) {
    val present = records.count { it.status == "Present" }
    val absent = records.count { it.status == "Absent" }
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryMiniItem("Present", present.toString(), SuccessColor, Modifier.weight(1f))
        SummaryMiniItem("Absent", absent.toString(), ErrorColor, Modifier.weight(1f))
        SummaryMiniItem("Total", records.size.toString(), InfoColor, Modifier.weight(1f))
    }
}

@Composable
fun SummaryMiniItem(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 12.sp, color = SecondaryText)
        }
    }
}

@Composable
fun StudentAttendanceRow(record: Attendance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(record.roll_number ?: "?", fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(record.student_name ?: "Unknown Student", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text("Roll No: ${record.roll_number ?: "N/A"}", fontSize = 12.sp, color = SecondaryText)
            }
            
            val statusColor = when (record.status) {
                "Present" -> SuccessColor
                "Absent" -> ErrorColor
                "Late" -> WarningColor
                else -> InfoColor
            }
            
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = record.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
