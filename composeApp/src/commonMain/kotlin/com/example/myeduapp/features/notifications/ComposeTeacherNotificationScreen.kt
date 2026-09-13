package com.example.myeduapp.features.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.platform.rememberFilePickerLauncher
import com.example.myeduapp.core.ui.components.AppBackTopBar
import com.example.myeduapp.core.ui.components.AppButton
import com.example.myeduapp.core.ui.components.AppCard
import com.example.myeduapp.core.ui.components.AppLoaderCompact
import com.example.myeduapp.core.ui.theme.AttendanceDimens
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.features.attendance.AttendanceClassSectionFilters
import com.example.myeduapp.features.attendance.rememberAttendanceClassSectionFilters

class ComposeTeacherNotificationScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        ComposeTeacherNotificationContent(
            onBack = { navigator.pop() },
            onViewSent = {
                pendingOpenSentNotifications = true
                navigator.pop()
            }
        )
    }
}

@Composable
private fun ComposeTeacherNotificationContent(
    onBack: () -> Unit,
    onViewSent: () -> Unit
) {
    val screen = LocalNavigator.currentOrThrow.lastItem
    val viewModel = screen.rememberScreenModel { ComposeTeacherNotificationViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val filters = rememberAttendanceClassSectionFilters()
    val snackbar = remember { SnackbarHostState() }
    val colors = MaterialTheme.colorScheme

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.primary,
        unfocusedBorderColor = colors.outline,
        focusedContainerColor = colors.surface,
        unfocusedContainerColor = colors.surface
    )

    val pickFiles = rememberFilePickerLauncher { files ->
        viewModel.uploadFiles(files)
    }

    LaunchedEffect(filters.selectedGrade, filters.selectedSection, uiState.audienceAll) {
        viewModel.loadStudents(filters.selectedGrade, filters.selectedSection, uiState.audienceAll)
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    val canSubmit = uiState.title.isNotBlank()
        && uiState.description.isNotBlank()
        && filters.isReady
        && (uiState.audienceAll || uiState.selectedStudentIds.isNotEmpty())
        && !uiState.uploading

    Scaffold(
        containerColor = colors.background,
        topBar = { AppBackTopBar(title = "Send Notification", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    label = { Text("Title") },
                    singleLine = true,
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.onDescriptionChange(it) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    label = { Text("Description") },
                    minLines = 3,
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = uiState.optionalDescription,
                    onValueChange = { viewModel.onOptionalDescriptionChange(it) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    label = { Text("Optional description") },
                    minLines = 2,
                    colors = fieldColors
                )

                AttendanceClassSectionFilters(state = filters)

                AppCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Students", fontWeight = FontWeight.SemiBold, color = PrimaryBlue, fontSize = 13.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AudienceTab("All", uiState.audienceAll, Modifier.weight(1f)) {
                                viewModel.setAudienceAll(true)
                            }
                            AudienceTab("Custom", !uiState.audienceAll, Modifier.weight(1f)) {
                                viewModel.setAudienceAll(false)
                            }
                        }
                        if (!uiState.audienceAll) {
                            when {
                                !filters.isReady -> Text(
                                    "Select class and section first.",
                                    fontSize = 12.sp,
                                    color = colors.onSurfaceVariant
                                )
                                uiState.loadingStudents -> Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) { AppLoaderCompact(size = 28.dp) }
                                uiState.students.isEmpty() -> Text(
                                    "No students in this class",
                                    fontSize = 13.sp,
                                    color = colors.onSurfaceVariant
                                )
                                else -> {
                                    Text(
                                        "${uiState.selectedStudentIds.size} selected",
                                        fontSize = 12.sp,
                                        color = colors.onSurfaceVariant
                                    )
                                    uiState.students.forEach { student ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().clickable {
                                                viewModel.toggleStudent(student.id)
                                            },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = student.id in uiState.selectedStudentIds,
                                                onCheckedChange = { checked ->
                                                    viewModel.setStudentChecked(student.id, checked)
                                                }
                                            )
                                            Column {
                                                Text(student.name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                                student.admission_number?.let {
                                                    Text(it, fontSize = 11.sp, color = colors.onSurfaceVariant)
                                                }
                                            }
                                        }
                                        HorizontalDivider(color = colors.outline.copy(alpha = 0.4f))
                                    }
                                }
                            }
                        } else {
                            Text(
                                "Every student in the selected class will be notified.",
                                fontSize = 12.sp,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }

                AppCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Attachments", fontWeight = FontWeight.SemiBold, color = PrimaryBlue, fontSize = 13.sp)
                        OutlinedButton(
                            onClick = { pickFiles() },
                            enabled = !uiState.uploading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(if (uiState.uploading) "Uploading…" else "Add files")
                        }
                        uiState.attachments.forEach { file ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(
                                    Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    file.displayName,
                                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                                IconButton(onClick = { viewModel.removeAttachment(file.file_path) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                }
            }

            Surface(tonalElevation = 4.dp, shadowElevation = 4.dp) {
                AppButton(
                    text = "Send Notification",
                    onClick = { viewModel.setConfirmSend(true) },
                    enabled = canSubmit,
                    isLoading = uiState.submitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AttendanceDimens.ScreenHorizontal, vertical = 8.dp)
                )
            }
        }
    }

    if (uiState.confirmSend) {
        AlertDialog(
            onDismissRequest = { viewModel.setConfirmSend(false) },
            title = { Text("Send Notification?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Are you sure you want to send this notification to the selected students?")
                    if (uiState.attachments.isNotEmpty()) {
                        Text("${uiState.attachments.size} file(s) will be included.")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setConfirmSend(false) }) { Text("Cancel") }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setConfirmSend(false)
                        val grade = filters.selectedGrade ?: return@TextButton
                        val section = filters.selectedSection ?: return@TextButton
                        viewModel.sendNow(grade, section)
                    }
                ) { Text("Send", color = PrimaryBlue, fontWeight = FontWeight.SemiBold) }
            }
        )
    }

    uiState.success?.let { result ->
        val classLabel = result.classLabel ?: result.class_name
            ?: listOfNotNull(result.grade?.let { "Grade $it" }, result.section).joinToString(" - ")
        val whenLabel = result.sent_at?.let { raw ->
            val day = if (raw.length >= 10) raw.take(10) else raw
            val time = if (raw.length >= 16) raw.substring(11, 16) else ""
            val dayPart = if (day == com.example.myeduapp.core.util.DateUtils.today()) "Today" else day
            listOf(dayPart, time).filter { it.isNotBlank() }.joinToString(" ")
        } ?: "Today"
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Notification sent", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(classLabel)
                    Text("${result.student_count ?: 0} students")
                    Text(whenLabel, fontSize = 13.sp, color = colors.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = onViewSent) { Text("View Sent Notifications", color = PrimaryBlue) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.resetForm() }) { Text("Send Another") }
            }
        )
    }
}

@Composable
private fun AudienceTab(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) PrimaryBlue else androidx.compose.ui.graphics.Color.Transparent
    ) {
        Text(
            label,
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = if (selected) androidx.compose.ui.graphics.Color.White else PrimaryBlue,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}
