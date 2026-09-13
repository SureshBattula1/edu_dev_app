package com.example.myeduapp.features.leaves

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.ui.components.AppLoaderFullscreen
import com.example.myeduapp.core.ui.theme.LeaveDimens
import com.example.myeduapp.core.ui.theme.leaveStatusColor
import com.example.myeduapp.data.model.LeaveCategory
import com.example.myeduapp.data.model.LeaveRecord

class LeaveApprovalsScreen(private val category: LeaveCategory) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        LeaveApprovalsScreenContent(category = category, onBack = { navigator.pop() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveApprovalsScreenContent(
    category: LeaveCategory,
    onBack: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val screen = LocalNavigator.currentOrThrow.lastItem
    val viewModel = screen.rememberScreenModel(tag = category.name) { LeaveApprovalsViewModel(category) }
    val uiState by viewModel.uiState.collectAsState()

    var remarksDialogLeave by remember { mutableStateOf<LeaveRecord?>(null) }
    var remarksDialogAction by remember { mutableStateOf<String?>(null) }
    var remarksText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(category, uiState.statusFilter) { viewModel.loadLeaves() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    val title = when (category) {
        LeaveCategory.STUDENT -> "Student Leave Approvals"
        LeaveCategory.TEACHER -> "Teacher Leave Approvals"
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
                    .padding(LeaveDimens.ScreenPadding)
            ) {
                LeaveStatusFilterRow(
                    options = listOf("Pending", "Approved", "Rejected", "All"),
                    selected = uiState.statusFilter,
                    onSelect = { viewModel.setStatusFilter(it) }
                )
            }

            when {
                uiState.isLoading -> AppLoaderFullscreen(message = "Loading leave requests")
                uiState.leaves.isEmpty() -> LeaveEmptyState(
                    title = "No requests found",
                    message = "There are no ${uiState.statusFilter?.lowercase() ?: ""} leave requests right now.",
                    modifier = Modifier.fillMaxSize()
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(LeaveDimens.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.leaves, key = { it.id }) { leave ->
                        val isProcessing = uiState.processingId == leave.id
                        LeaveRecordCard(
                            leave = leave,
                            showApplicant = true,
                            actions = if (leave.status == "Pending") {
                                {
                                    OutlinedButton(
                                        onClick = {
                                            remarksDialogLeave = leave
                                            remarksDialogAction = "Rejected"
                                            remarksText = ""
                                        },
                                        enabled = !isProcessing,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = leaveStatusColor("Rejected")
                                        )
                                    ) { Text("Reject") }

                                    Button(
                                        onClick = {
                                            remarksDialogLeave = leave
                                            remarksDialogAction = "Approved"
                                            remarksText = ""
                                        },
                                        enabled = !isProcessing,
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Approve") }
                                }
                            } else null
                        )
                    }
                }
            }
        }
    }

    remarksDialogLeave?.let { leave ->
        val action = remarksDialogAction ?: return@let
        AlertDialog(
            onDismissRequest = {
                remarksDialogLeave = null
                remarksDialogAction = null
            },
            title = { Text(if (action == "Approved") "Approve Leave" else "Reject Leave") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${leave.applicantName} • ${leave.leave_type}")
                    OutlinedTextField(
                        value = remarksText,
                        onValueChange = { remarksText = it },
                        label = { Text("Remarks (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        remarksDialogLeave = null
                        remarksDialogAction = null
                        viewModel.processLeave(
                            leaveId = leave.id,
                            approve = action == "Approved",
                            remarks = remarksText.takeIf { it.isNotBlank() }
                        )
                    }
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = {
                    remarksDialogLeave = null
                    remarksDialogAction = null
                }) { Text("Cancel") }
            }
        )
    }
}
