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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.datastore.AuthState
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.ui.components.AppLoaderFullscreen
import com.example.myeduapp.core.ui.theme.LeaveDimens
import com.example.myeduapp.data.model.LeaveCategory

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
    val screen = LocalNavigator.currentOrThrow.lastItem
    val viewModel = screen.rememberScreenModel(tag = category.name) { MyLeavesViewModel(category) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(category, user.id, uiState.statusFilter) {
        viewModel.loadLeaves(user.id)
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
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
                onClick = { viewModel.setShowApplySheet(true) },
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
                uiState.summary?.let { LeaveSummaryRow(it) }
                LeaveStatusFilterRow(
                    options = listOf("All", "Pending", "Approved", "Rejected", "Cancelled"),
                    selected = uiState.statusFilter,
                    onSelect = { viewModel.setStatusFilter(it) }
                )
            }

            when {
                uiState.isLoading -> AppLoaderFullscreen(message = "Loading leaves")
                uiState.leaves.isEmpty() -> LeaveEmptyState(
                    title = "No leave records",
                    message = "Tap + to submit a new leave application.",
                    modifier = Modifier.fillMaxSize()
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(LeaveDimens.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.leaves, key = { it.id }) { leave ->
                        LeaveRecordCard(
                            leave = leave,
                            actions = if (leave.status == "Pending") {
                                {
                                    OutlinedButton(
                                        onClick = { viewModel.cancelLeave(leave.id, user.id) },
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

    if (uiState.showApplySheet) {
        ApplyLeaveSheet(
            category = category,
            onDismiss = { viewModel.setShowApplySheet(false) },
            isSubmitting = uiState.isSubmitting,
            onSubmit = { leaveType, fromDate, toDate, reason, remarks ->
                viewModel.applyLeave(
                    userId = user.id,
                    branchId = user.branch_id,
                    leaveType = leaveType,
                    fromDate = fromDate,
                    toDate = toDate,
                    reason = reason,
                    remarks = remarks
                )
            }
        )
    }
}
