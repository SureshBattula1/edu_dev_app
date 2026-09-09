package com.example.myeduapp.features.teacher.attendance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.example.myeduapp.core.ui.components.AppBackTopBar
import com.example.myeduapp.core.ui.components.AppButton
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.ui.theme.SuccessColor
import com.example.myeduapp.core.ui.theme.ErrorColor
import com.example.myeduapp.data.model.Student
import com.example.myeduapp.data.model.SchoolClass
import com.example.myeduapp.data.model.Attendance
import com.example.myeduapp.data.repository.ClassRepository
import com.example.myeduapp.data.repository.StudentRepository
import com.example.myeduapp.data.repository.AttendanceRepository
import kotlinx.coroutines.launch

class CreateAttendanceScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var step by remember { mutableStateOf(1) }
        var selectedClass by remember { mutableStateOf<SchoolClass?>(null) }
        var selectedDate by remember { mutableStateOf("2026-09-08") } // Mock today
        
        Scaffold(
            topBar = {
                AppBackTopBar(
                    title = if (step == 1) "Select Class" else "Mark Attendance",
                    onBack = { if (step > 1) step-- else navigator.pop() }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (step == 1) {
                    ClassSelectionStep(onNext = { schoolClass ->
                        selectedClass = schoolClass
                        step = 2
                    })
                } else {
                    selectedClass?.let { schoolClass ->
                        MarkAttendanceStep(
                            schoolClass = schoolClass,
                            date = selectedDate,
                            onSuccess = { navigator.pop() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClassSelectionStep(onNext: (SchoolClass) -> Unit) {
    val repository = remember { ClassRepository() }
    var classes by remember { mutableStateOf<List<SchoolClass>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repository.getMyClasses().onSuccess {
            classes = it
            isLoading = false
        }.onFailure {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
    } else if (classes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No classes assigned to you.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Select a class to take attendance", fontSize = 16.sp, color = SecondaryText)
            }
            items(classes) { schoolClass ->
                Card(
                    onClick = { onNext(schoolClass) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(schoolClass.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Section: ${schoolClass.section} • Subject: ${schoolClass.subject ?: "N/A"}", color = SecondaryText, fontSize = 14.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                    }
                }
            }
        }
    }
}

@Composable
fun MarkAttendanceStep(schoolClass: SchoolClass, date: String, onSuccess: () -> Unit) {
    val studentRepo = remember { StudentRepository() }
    val attendanceRepo = remember { AttendanceRepository() }
    val scope = rememberCoroutineScope()
    
    var students by remember { mutableStateOf<List<Student>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val attendanceStates = remember { mutableStateMapOf<Int, String>() }

    LaunchedEffect(schoolClass.id) {
        studentRepo.getStudentsByClass(schoolClass.name, schoolClass.section).onSuccess {
            students = it
            it.forEach { student -> attendanceStates[student.id] = "Present" }
            isLoading = false
        }.onFailure {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("${schoolClass.name} - ${schoolClass.section}", fontWeight = FontWeight.Bold)
                    Text("Date: $date", fontSize = 14.sp, color = SecondaryText)
                }
                TextButton(onClick = { students.forEach { attendanceStates[it.id] = "Present" } }) {
                    Text("All Present")
                }
            }
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(students) { student ->
                    val status = attendanceStates[student.id] ?: "Present"
                    AttendanceMarkRow(
                        student = student,
                        status = status,
                        onStatusChange = { attendanceStates[student.id] = it }
                    )
                }
            }
            
            Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 8.dp) {
                AppButton(
                    text = "Submit Attendance",
                    onClick = {
                        scope.launch {
                            val records = students.map {
                                Attendance(
                                    student_id = it.id,
                                    date = date,
                                    status = attendanceStates[it.id] ?: "Present"
                                )
                            }
                            attendanceRepo.submitAttendance(schoolClass.id, date, records).onSuccess {
                                onSuccess()
                            }
                        }
                    },
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun AttendanceMarkRow(student: Student, status: String, onStatusChange: (String) -> Unit) {
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
                Text(student.roll_number ?: "?", fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(student.full_name, modifier = Modifier.weight(1f), fontSize = 15.sp)
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallStatusButton("P", status == "Present", SuccessColor) { onStatusChange("Present") }
                SmallStatusButton("A", status == "Absent", ErrorColor) { onStatusChange("Absent") }
            }
        }
    }
}

@Composable
fun SmallStatusButton(text: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) color else Color.Transparent,
            contentColor = if (isSelected) Color.White else color
        ),
        border = BorderStroke(1.dp, color)
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}
