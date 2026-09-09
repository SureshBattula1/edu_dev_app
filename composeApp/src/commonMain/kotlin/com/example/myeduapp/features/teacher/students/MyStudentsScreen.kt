package com.example.myeduapp.features.teacher.students

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.ui.components.AppBackTopBar
import com.example.myeduapp.core.ui.components.AppCard
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.features.teacher.student360.Student360Screen
import com.example.myeduapp.data.model.Student
import com.example.myeduapp.data.repository.StudentRepository
import kotlinx.coroutines.delay

class MyStudentsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repository = remember { StudentRepository() }
        var students by remember { mutableStateOf<List<Student>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }

        LaunchedEffect(searchQuery) {
            if (searchQuery.length >= 2 || searchQuery.isEmpty()) {
                isLoading = true
                if (searchQuery.isNotEmpty()) delay(500) // Debounce
                repository.getStudents(searchQuery).onSuccess {
                    students = it
                }
                isLoading = false
            }
        }

        Scaffold(
            topBar = {
                AppBackTopBar(title = "My Students", onBack = { navigator.pop() })
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    placeholder = { Text("Search by name, roll no, adm no...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                } else if (students.isEmpty() && searchQuery.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No students found matching '$searchQuery'", color = SecondaryText)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(students) { student ->
                            StudentCard(student) {
                                // Navigate to Student 360
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentCard(student: Student, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(student.full_name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${student.grade ?: "N/A"} - ${student.section ?: "N/A"}",
                    fontSize = 13.sp,
                    color = SecondaryText
                )
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "Roll: ${student.roll_number ?: "N/A"}",
                        fontSize = 11.sp,
                        color = SecondaryText.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Adm: ${student.admission_number}",
                        fontSize = 11.sp,
                        color = SecondaryText.copy(alpha = 0.7f)
                    )
                }
            }
            
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
