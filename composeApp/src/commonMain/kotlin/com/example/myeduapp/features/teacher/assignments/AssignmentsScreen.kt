package com.example.myeduapp.features.teacher.assignments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.myeduapp.data.model.Assignment
import com.example.myeduapp.data.repository.AssignmentRepository

class AssignmentsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repository = remember { AssignmentRepository() }
        var assignments by remember { mutableStateOf<List<Assignment>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            repository.getAssignments().onSuccess {
                assignments = it
                isLoading = false
            }.onFailure {
                isLoading = false
            }
        }

        Scaffold(
            topBar = {
                AppBackTopBar(title = "Assignments", onBack = { navigator.pop() })
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { /* Navigate to create */ },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Assignment")
                }
            }
        ) { padding ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (assignments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No assignments found", color = SecondaryText)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(assignments) { assignment ->
                        AssignmentCard(assignment) {
                            // Navigate to details/submissions
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssignmentCard(assignment: Assignment, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = assignment.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                Surface(
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = assignment.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${assignment.class_name}-${assignment.section} • ${assignment.subject}",
                fontSize = 13.sp,
                color = SecondaryText
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = SecondaryText)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Due: ${assignment.due_date}", fontSize = 12.sp, color = SecondaryText)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = SecondaryText)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "${assignment.submission_count} Submissions", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
