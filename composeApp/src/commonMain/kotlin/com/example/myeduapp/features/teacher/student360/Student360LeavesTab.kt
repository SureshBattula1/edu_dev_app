package com.example.myeduapp.features.teacher.student360

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.ui.theme.Student360Dimens
import com.example.myeduapp.core.ui.theme.leaveStatusColor
import com.example.myeduapp.data.model.*
import com.example.myeduapp.data.repository.LeaveRepository
import com.example.myeduapp.features.leaves.ApplyLeaveSheet
import com.example.myeduapp.features.leaves.LeaveRecordCard
import com.example.myeduapp.features.leaves.LeaveStatusFilterRow
import kotlinx.coroutines.launch

@Composable
fun Student360LeavesTab(
    userId: Int?,
    isSelfView: Boolean,
    branchId: Int?,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val repository = remember { LeaveRepository() }
    val scope = rememberCoroutineScope()
    val sessionUser = SessionManager.user

    var response by remember { mutableStateOf<LeaveListResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var statusFilter by remember { mutableStateOf<String?>(null) }
    var showApplySheet by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    fun loadLeaves() {
        val id = userId
        if (id == null) {
            isLoading = false
            error = "Student account is not linked. Cannot load leaves."
            return
        }
        scope.launch {
            isLoading = true
            error = null
            repository.getMyLeaves(LeaveCategory.STUDENT, id)
                .onSuccess { response = it }
                .onFailure { err ->
                    error = err.message ?: "Failed to load leaves"
                    response = null
                }
            isLoading = false
        }
    }

    LaunchedEffect(userId) { loadLeaves() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    val allLeaves = response?.data ?: emptyList()
    val leaves = allLeaves.filter { record ->
        statusFilter == null || record.status == statusFilter
    }
    val summary = response?.summary
    val hasNoLeavesAtAll = response != null && allLeaves.isEmpty()

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                if (isSelfView && userId != null) {
                    FloatingActionButton(
                        onClick = { showApplySheet = true },
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Apply leave")
                    }
                }
            }
        ) { padding ->
            when {
                isLoading -> Student360Loading(Modifier.padding(padding))
                error != null -> Student360LeavesError(
                    message = error!!,
                    onRetry = { loadLeaves() },
                    modifier = Modifier.padding(padding)
                )
                hasNoLeavesAtAll -> Student360EmptyTab(
                    title = if (isSelfView) "No leaves yet" else "No leave records",
                    message = if (isSelfView) {
                        "Tap + to apply for your first leave."
                    } else {
                        "This student has not applied for any leaves yet."
                    },
                    modifier = Modifier.padding(padding)
                )
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(Student360Dimens.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(Student360Dimens.SectionSpacing)
                ) {
                    summary?.let { stats ->
                        item {
                            Student360LeaveSummaryCard(summary = stats)
                        }
                    }

                    item {
                        LeaveStatusFilterRow(
                            options = listOf("All", "Pending", "Approved", "Rejected", "Cancelled"),
                            selected = statusFilter,
                            onSelect = { statusFilter = it }
                        )
                    }

                    if (leaves.isEmpty()) {
                        item {
                            Student360EmptyTab(
                                title = "No matching leaves",
                                message = "No ${statusFilter?.lowercase() ?: ""} leave records found.",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        item {
                            Text(
                                "Leave History",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = colorScheme.primary
                            )
                        }
                        items(leaves, key = { it.id }) { leave ->
                            LeaveRecordCard(
                                leave = leave,
                                actions = if (isSelfView && leave.status == "Pending") {
                                    {
                                        OutlinedButton(
                                            onClick = {
                                                scope.launch {
                                                    repository.cancelLeave(leave.id, LeaveCategory.STUDENT)
                                                        .onSuccess {
                                                            snackbarMessage = it
                                                            loadLeaves()
                                                        }
                                                        .onFailure { snackbarMessage = it.message }
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = leaveStatusColor("Cancelled")
                                            )
                                        ) { Text("Cancel") }
                                    }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }

    if (showApplySheet && userId != null) {
        ApplyLeaveSheet(
            category = LeaveCategory.STUDENT,
            onDismiss = { showApplySheet = false },
            isSubmitting = isSubmitting,
            onSubmit = { leaveType, fromDate, toDate, reason, remarks ->
                scope.launch {
                    isSubmitting = true
                    repository.applyStudentLeave(
                        CreateStudentLeaveBody(
                            student_id = userId,
                            from_date = fromDate.take(10),
                            to_date = toDate.take(10),
                            leave_type = leaveType,
                            reason = reason,
                            remarks = remarks.takeIf { it.isNotBlank() },
                            branch_id = branchId ?: sessionUser?.branch_id
                        )
                    ).onSuccess {
                        snackbarMessage = it
                        showApplySheet = false
                        loadLeaves()
                    }.onFailure { snackbarMessage = it.message }
                    isSubmitting = false
                }
            }
        )
    }
}

@Composable
private fun Student360LeaveSummaryCard(summary: LeaveSummary) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Student360Dimens.CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Leave Summary",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Student360LeaveStatChip(
                    label = "Total",
                    value = summary.total_leaves.toString(),
                    color = colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Student360LeaveStatChip(
                    label = "Pending",
                    value = summary.pending.toString(),
                    color = leaveStatusColor("Pending"),
                    modifier = Modifier.weight(1f)
                )
                Student360LeaveStatChip(
                    label = "Approved",
                    value = summary.approved.toString(),
                    color = leaveStatusColor("Approved"),
                    modifier = Modifier.weight(1f)
                )
                Student360LeaveStatChip(
                    label = "Days",
                    value = summary.total_days_taken.toString(),
                    color = colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun Student360LeaveStatChip(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
            Text(label, fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Student360LeavesError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Could not load leaves",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            color = colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Retry")
        }
    }
}
