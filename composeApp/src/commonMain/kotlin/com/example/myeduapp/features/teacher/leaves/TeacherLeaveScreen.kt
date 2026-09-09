package com.example.myeduapp.features.teacher.leaves

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
import com.example.myeduapp.data.model.LeaveRequest
import com.example.myeduapp.data.repository.LeaveRepository
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.datastore.AuthState
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.ui.theme.SuccessColor
import com.example.myeduapp.core.ui.theme.ErrorColor
import com.example.myeduapp.core.ui.theme.WarningColor
import kotlinx.coroutines.launch

class TeacherLeaveScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TeacherLeaveScreenContent(onBack = { navigator.pop() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherLeaveScreenContent(onBack: (() -> Unit)? = null) {
    val authState by SessionManager.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user ?: return
    
    val repository = remember { LeaveRepository() }
    var leaves by remember { mutableStateOf<List<LeaveRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showApplyForm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        repository.getStudentLeaves(user.id).onSuccess {
            leaves = it
            isLoading = false
        }.onFailure {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leaves") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showApplyForm = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Apply Leave")
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (leaves.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No leave history found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(leaves) { leave ->
                        LeaveCard(leave)
                    }
                }
            }
        }

        if (showApplyForm) {
            ApplyLeaveDialog(
                onDismiss = { showApplyForm = false },
                onSubmit = { leave ->
                    scope.launch {
                        repository.applyLeave(leave).onSuccess {
                            showApplyForm = false
                            // Refresh list
                            isLoading = true
                            repository.getStudentLeaves(user.id).onSuccess { leaves = it }
                            isLoading = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun LeaveCard(leave: LeaveRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(leave.leave_type, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("${leave.start_date} to ${leave.end_date}", fontSize = 14.sp, color = SecondaryText)
                }
                
                val (statusColor, statusBg) = when (leave.status) {
                    "Approved" -> SuccessColor to SuccessColor.copy(alpha = 0.1f)
                    "Rejected" -> ErrorColor to ErrorColor.copy(alpha = 0.1f)
                    else -> WarningColor to WarningColor.copy(alpha = 0.1f)
                }
                
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        leave.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Reason:", fontSize = 12.sp, color = SecondaryText)
            Text(leave.reason, fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyLeaveDialog(onDismiss: () -> Unit, onSubmit: (LeaveRequest) -> Unit) {
    var leaveType by remember { mutableStateOf("Sick Leave") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apply for Leave") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = leaveType,
                    onValueChange = { leaveType = it },
                    label = { Text("Leave Type") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("End Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(LeaveRequest(
                        start_date = startDate,
                        end_date = endDate,
                        leave_type = leaveType,
                        reason = reason
                    ))
                },
                enabled = startDate.isNotBlank() && endDate.isNotBlank() && reason.isNotBlank()
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
