package com.example.myeduapp.features.teacher.marks

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.ui.components.AppBackTopBar
import com.example.myeduapp.core.ui.components.AppButton
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.data.model.Student

class MarksScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var step by remember { mutableStateOf(1) }
        
        var selectedExam by remember { mutableStateOf("") }
        var selectedClass by remember { mutableStateOf("") }
        
        Scaffold(
            topBar = {
                AppBackTopBar(
                    title = if (step == 1) "Select Exam" else "Enter Marks",
                    onBack = { if (step > 1) step-- else navigator.pop() }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (step == 1) {
                    ExamSelectionStep(onNext = { e, c ->
                        selectedExam = e
                        selectedClass = c
                        step = 2
                    })
                } else {
                    EnterMarksStep(
                        examName = selectedExam,
                        className = selectedClass,
                        onSuccess = { navigator.pop() }
                    )
                }
            }
        }
    }
}

@Composable
fun ExamSelectionStep(onNext: (String, String) -> Unit) {
    var examName by remember { mutableStateOf("Mid-Term Exam") }
    var className by remember { mutableStateOf("Class 10-A") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Academic Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        
        OutlinedTextField(
            value = examName,
            onValueChange = { examName = it },
            label = { Text("Exam") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = className,
            onValueChange = { className = it },
            label = { Text("Class & Section") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        AppButton(
            text = "Load Students",
            onClick = { onNext(examName, className) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EnterMarksStep(examName: String, className: String, onSuccess: () -> Unit) {
    // Mock students
    val students = remember {
        listOf(
            Student("1", "101", "Rahul", "Kumar", "ADM001", "1"),
            Student("2", "102", "Suresh", "Reddy", "ADM002", "2"),
            Student("3", "103", "Anil", "Kumar", "ADM003", "3")
        )
    }
    
    val marksStates = remember { mutableStateMapOf<String, String>() }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().background(PrimaryBlue.copy(alpha = 0.05f)).padding(16.dp)) {
            Column {
                Text(examName, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                Text(className, fontSize = 14.sp, color = SecondaryText)
            }
        }
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(students) { student ->
                MarkInputItem(
                    student = student,
                    marks = marksStates[student.id] ?: "",
                    onMarksChange = { marksStates[student.id] = it }
                )
            }
        }
        
        Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 4.dp) {
            AppButton(
                text = "Save Marks",
                onClick = onSuccess,
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            )
        }
    }
}

@Composable
fun MarkInputItem(student: Student, marks: String, onMarksChange: (String) -> Unit) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(student.full_name, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text("Roll No: ${student.roll_number}", fontSize = 12.sp, color = SecondaryText)
            }
            
            OutlinedTextField(
                value = marks,
                onValueChange = onMarksChange,
                modifier = Modifier.width(80.dp),
                placeholder = { Text("0") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                textStyle = TextStyle(textAlign = TextAlign.Center)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            Text("/ 100", fontSize = 12.sp, color = SecondaryText)
        }
    }
}
