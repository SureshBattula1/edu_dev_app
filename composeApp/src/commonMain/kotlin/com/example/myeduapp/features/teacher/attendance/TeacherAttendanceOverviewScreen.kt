package com.example.myeduapp.features.teacher.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.ui.components.AppBackTopBar
import com.example.myeduapp.core.ui.components.AppButton
import com.example.myeduapp.core.ui.components.AppCard
import com.example.myeduapp.core.ui.components.AppLoaderCompact
import com.example.myeduapp.core.ui.components.AppLoaderFullscreen
import com.example.myeduapp.core.ui.theme.AttendanceDimens
import com.example.myeduapp.core.ui.theme.Background
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.ui.theme.SuccessColor
import com.example.myeduapp.data.model.AttendanceNotifyReceipts
import com.example.myeduapp.data.model.ClassAttendanceStatus
import com.example.myeduapp.features.attendance.AttendanceDatePicker
import com.example.myeduapp.features.attendance.EmptyAttendanceState

class TeacherAttendanceOverviewScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { TeacherAttendanceOverviewViewModel() }
        val uiState by viewModel.uiState.collectAsState()
        val snackbar = remember { SnackbarHostState() }

        LaunchedEffect(uiState.snackbarMessage) {
            uiState.snackbarMessage?.let { message ->
                snackbar.showSnackbar(message)
                viewModel.consumeSnackbar()
            }
        }

        Scaffold(
            containerColor = Background,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                AppBackTopBar(
                    title = "Today’s Attendance",
                    onBack = { navigator.pop() },
                    actions = {
                        IconButton(onClick = { viewModel.load() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                AttendanceDatePicker(
                    selectedDate = uiState.date,
                    onDateChange = viewModel::onDateChange,
                    label = "Date",
                    compact = true,
                    modifier = Modifier.padding(horizontal = AttendanceDimens.ScreenHorizontal, vertical = 8.dp)
                )

                if (uiState.canNotify && !uiState.loading && uiState.rows.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AttendanceDimens.ScreenHorizontal),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = viewModel::selectAllCreated,
                            enabled = uiState.createdRows.isNotEmpty()
                        ) { Text("Select all created", fontSize = 12.sp, color = PrimaryBlue) }
                        TextButton(
                            onClick = viewModel::uncheckAll,
                            enabled = uiState.selected.isNotEmpty()
                        ) { Text("Uncheck all", fontSize = 12.sp, color = PrimaryBlue) }
                        Spacer(modifier = Modifier.weight(1f))
                        Text("${uiState.selectedCreated.size} selected", fontSize = 11.sp, color = SecondaryText)
                    }
                }

                when {
                    uiState.loading -> AppLoaderFullscreen(message = "Loading classes")
                    uiState.error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(uiState.error ?: "Could not load", color = SecondaryText, fontSize = 13.sp)
                            TextButton(onClick = { viewModel.load() }) { Text("Retry", color = PrimaryBlue) }
                        }
                    }
                    uiState.rows.isEmpty() -> EmptyAttendanceState(
                        title = "No classes found",
                        message = "There are no class/section groups for this branch.",
                        modifier = Modifier.fillMaxSize()
                    )
                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(
                                horizontal = AttendanceDimens.ScreenHorizontal,
                                vertical = 4.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(AttendanceDimens.ListSpacing)
                        ) {
                            items(uiState.rows, key = { it.selectionKey }) { row ->
                                val key = row.selectionKey
                                val checkboxEnabled = uiState.canNotify && row.created
                                AppCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        navigator.push(
                                            CreateAttendanceScreen(
                                                initialGrade = row.grade,
                                                initialSection = row.section,
                                                initialDate = uiState.date
                                            )
                                        )
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (uiState.canNotify) {
                                            Checkbox(
                                                checked = key in uiState.selected,
                                                onCheckedChange = { checked ->
                                                    viewModel.toggleSelected(key, checked, row.created)
                                                },
                                                enabled = checkboxEnabled
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                            Text(
                                                row.displayName,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = if (row.created) PrimaryBlue else SecondaryText
                                            )
                                            Text("${row.student_count} students", fontSize = 12.sp, color = SecondaryText)
                                            if (row.created) {
                                                Text(
                                                    "Present ${row.present} · Absent ${row.absent}",
                                                    fontSize = 11.sp,
                                                    color = SecondaryText
                                                )
                                            } else {
                                                Text("Attendance not created", fontSize = 11.sp, color = SecondaryText)
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            if (row.created) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = "Created",
                                                    tint = SuccessColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Default.RadioButtonUnchecked,
                                                    contentDescription = "Not created",
                                                    tint = SecondaryText,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            if (uiState.canNotify && row.notify_sent) {
                                                Text(
                                                    "Sent",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = SuccessColor,
                                                    modifier = Modifier.clickable { viewModel.openReceipts(row) }
                                                )
                                            } else if (uiState.canNotify && row.created) {
                                                Text("Not sent", fontSize = 10.sp, color = SecondaryText)
                                            } else if (!row.created) {
                                                Text("Disabled", fontSize = 10.sp, color = SecondaryText)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (uiState.canNotify) {
                            Surface(
                                tonalElevation = 4.dp,
                                shadowElevation = 4.dp,
                                color = Color.White
                            ) {
                                AppButton(
                                    text = "Send Notification",
                                    onClick = { viewModel.setConfirmSend(true) },
                                    enabled = uiState.selectedCreated.isNotEmpty() && !uiState.sending,
                                    isLoading = uiState.sending,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = AttendanceDimens.ScreenHorizontal,
                                            vertical = 8.dp
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uiState.confirmSend) {
            AlertDialog(
                onDismissRequest = { viewModel.setConfirmSend(false) },
                title = { Text("Send attendance notifications?", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Send Present/Absent status to students in ${uiState.selectedCreated.size} selected class(es)? " +
                            "Only classes with created attendance will be notified."
                    )
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setConfirmSend(false) }) { Text("Cancel") }
                },
                confirmButton = {
                    TextButton(onClick = viewModel::sendNotifications) {
                        Text("Send", color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }

        uiState.receiptsFor?.let { row ->
            AttendanceNotifyReceiptsDialog(
                row = row,
                loading = uiState.receiptsLoading,
                data = uiState.receiptsData,
                error = uiState.receiptsError,
                onDismiss = viewModel::dismissReceipts
            )
        }
    }
}

@Composable
private fun AttendanceNotifyReceiptsDialog(
    row: ClassAttendanceStatus,
    loading: Boolean,
    data: AttendanceNotifyReceipts?,
    error: String?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
                    .heightIn(max = 480.dp)
            ) {
                Text(row.displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryBlue)
                Text("Student notification status", fontSize = 12.sp, color = SecondaryText)
                Spacer(modifier = Modifier.height(8.dp))
                when {
                    loading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        AppLoaderCompact(size = 28.dp)
                    }
                    error != null -> Text(error, color = SecondaryText, fontSize = 13.sp)
                    else -> {
                        Text(
                            "${data?.sent_count ?: 0} of ${data?.total ?: 0} sent",
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            data?.students.orEmpty().forEach { student ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            student.name.ifBlank { "Student" },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            listOfNotNull(
                                                student.roll_number?.let { "Roll $it" },
                                                student.status
                                            ).joinToString(" · "),
                                            fontSize = 11.sp,
                                            color = SecondaryText
                                        )
                                    }
                                    Text(
                                        if (student.sent) "Sent" else "Not sent",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (student.sent) SuccessColor else SecondaryText
                                    )
                                }
                                HorizontalDivider(color = SecondaryText.copy(alpha = 0.2f))
                            }
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Close", color = PrimaryBlue)
                }
            }
        }
    }
}
