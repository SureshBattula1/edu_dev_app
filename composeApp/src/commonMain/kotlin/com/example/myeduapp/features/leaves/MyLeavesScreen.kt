package com.example.myeduapp.features.leaves

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.datastore.AuthState
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.ui.components.AppLoaderFullscreen
import com.example.myeduapp.core.ui.theme.LeaveDimens
import com.example.myeduapp.data.model.*
import com.example.myeduapp.data.repository.LeaveRepository
import kotlinx.coroutines.launch

class MyLeavesScreen(private val category: LeaveCategory) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        MyLeavesScreenContent(category = category, onBack = { navigator.pop() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLeavesScreenContent(
    category: LeaveCategory,
    onBack: () -> Unit
) {
    val authState by SessionManager.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user ?: return
    val colorScheme = MaterialTheme.colorScheme
    val repository = remember { LeaveRepository() }
    val scope = rememberCoroutineScope()

    var leaves by remember { mutableStateOf<List<LeaveRecord>>(emptyList()) }
    var summary by remember { mutableStateOf<LeaveSummary?>(null) }
    var statusFilter by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showApplySheet by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    fun loadLeaves() {
        scope.launch {
            isLoading = true
            repository.getMyLeaves(category, user.id)
                .onSuccess { response ->
                    leaves = response.data.filter { record ->
                        statusFilter == null || record.status == statusFilter
                    }
                    summary = response.summary
                }
                .onFailure { snackbarMessage = it.message }
            isLoading = false
        }
    }

    LaunchedEffect(category, user.id, statusFilter) { loadLeaves() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    val title = when (category) {
        LeaveCategory.STUDENT -> "My Leaves"
        LeaveCategory.TEACHER -> "My Leaves"
    }

    Scaffold(
        containerColor = colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primary,
                    titleContentColor = colorScheme.onPrimary,
                    navigationIconContentColor = colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showApplySheet = true },
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Apply leave")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surface)
                    .padding(LeaveDimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                summary?.let { LeaveSummaryRow(it) }
                LeaveStatusFilterRow(
                    options = listOf("All", "Pending", "Approved", "Rejected", "Cancelled"),
                    selected = statusFilter,
                    onSelect = { statusFilter = it }
                )
            }

            when {
                isLoading -> AppLoaderFullscreen(message = "Loading leaves")
                leaves.isEmpty() -> LeaveEmptyState(
                    title = "No leave records",
                    message = "Tap + to submit a new leave application.",
                    modifier = Modifier.fillMaxSize()
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(LeaveDimens.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(leaves, key = { it.id }) { leave ->
                        LeaveRecordCard(
                            leave = leave,
                            actions = if (leave.status == "Pending") {
                                {
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                repository.cancelLeave(leave.id, category)
                                                    .onSuccess {
                                                        snackbarMessage = it
                                                        loadLeaves()
                                                    }
                                                    .onFailure { snackbarMessage = it.message }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Cancel") }
                                }
                            } else null
                        )
                    }
                }
            }
        }
    }

    if (showApplySheet) {
        ApplyLeaveSheet(
            category = category,
            onDismiss = { showApplySheet = false },
            isSubmitting = isSubmitting,
            onSubmit = { leaveType, fromDate, toDate, reason, remarks ->
                scope.launch {
                    isSubmitting = true
                    val result = when (category) {
                        LeaveCategory.STUDENT -> repository.applyStudentLeave(
                            CreateStudentLeaveBody(
                                student_id = user.id,
                                from_date = fromDate.take(10),
                                to_date = toDate.take(10),
                                leave_type = leaveType,
                                reason = reason,
                                remarks = remarks.takeIf { it.isNotBlank() },
                                branch_id = user.branch_id
                            )
                        )
                        LeaveCategory.TEACHER -> repository.applyTeacherLeave(
                            CreateTeacherLeaveBody(
                                teacher_id = user.id,
                                from_date = fromDate.take(10),
                                to_date = toDate.take(10),
                                leave_type = leaveType,
                                reason = reason,
                                remarks = remarks.takeIf { it.isNotBlank() },
                                branch_id = user.branch_id
                            )
                        )
                    }
                    isSubmitting = false
                    result.onSuccess {
                        snackbarMessage = it
                        showApplySheet = false
                        loadLeaves()
                    }.onFailure { snackbarMessage = it.message }
                }
            }
        )
    }
}
