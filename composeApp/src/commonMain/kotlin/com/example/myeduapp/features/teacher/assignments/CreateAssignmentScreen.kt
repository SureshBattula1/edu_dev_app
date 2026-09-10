package com.example.myeduapp.features.teacher.assignments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.platform.rememberFilePickerLauncher
import com.example.myeduapp.core.sound.showAppNotification
import com.example.myeduapp.core.ui.components.AppBackTopBar
import com.example.myeduapp.core.ui.components.AppButton
import com.example.myeduapp.core.ui.components.AppCard
import com.example.myeduapp.core.ui.components.AppLoaderCompact
import com.example.myeduapp.core.ui.components.FilterOptionDropdown
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryBlue
import com.example.myeduapp.core.util.DateUtils
import com.example.myeduapp.data.model.AssignmentAttachment
import com.example.myeduapp.data.model.CreateAssignmentBody
import com.example.myeduapp.data.model.EligibleStudent
import com.example.myeduapp.data.model.FilterOption
import com.example.myeduapp.data.model.SubjectOption
import com.example.myeduapp.data.model.UpdateAssignmentBody
import com.example.myeduapp.data.repository.AssignmentRepository
import com.example.myeduapp.features.attendance.AttendanceDatePicker
import com.example.myeduapp.features.attendance.rememberAttendanceClassSectionFilters
import kotlinx.coroutines.launch

class CreateAssignmentScreen(private val assignmentId: String? = null) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        CreateAssignmentContent(
            assignmentId = assignmentId,
            onBack = { navigator.pop() },
            onSaved = { navigator.pop() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAssignmentContent(
    assignmentId: String? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val isEdit = !assignmentId.isNullOrBlank()
    val repository = remember { AssignmentRepository() }
    val filters = rememberAttendanceClassSectionFilters()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme

    var subjects by remember { mutableStateOf<List<SubjectOption>>(emptyList()) }
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var optionalDescription by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(DateUtils.currentLocalDate().toString()) }
    var maxMarks by remember { mutableStateOf("20") }
    var assignmentType by remember { mutableStateOf("Homework") }
    var audienceAll by remember { mutableStateOf(true) }
    var students by remember { mutableStateOf<List<EligibleStudent>>(emptyList()) }
    var selectedStudentIds by remember { mutableStateOf(setOf<String>()) }
    var loadingStudents by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    var uploading by remember { mutableStateOf(false) }
    var loadingDetail by remember { mutableStateOf(isEdit) }
    var successTitle by remember { mutableStateOf<String?>(null) }
    var successWasUpdate by remember { mutableStateOf(false) }
    var attachments by remember { mutableStateOf<List<AssignmentAttachment>>(emptyList()) }
    var pendingSection by remember { mutableStateOf<String?>(null) }

    val typeOptions = listOf("Homework", "Project", "Quiz", "Test", "Other")
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.primary,
        unfocusedBorderColor = colors.outline,
        focusedContainerColor = colors.surface,
        unfocusedContainerColor = colors.surface
    )

    val pickFiles = rememberFilePickerLauncher { files ->
        if (files.isEmpty()) return@rememberFilePickerLauncher
        scope.launch {
            uploading = true
            files.forEach { file ->
                repository.uploadAttachment(file)
                    .onSuccess { attachments = attachments + it }
                    .onFailure { snackbar.showSnackbar(it.message ?: "Could not upload ${file.name}") }
            }
            uploading = false
        }
    }

    LaunchedEffect(assignmentId) {
        if (assignmentId.isNullOrBlank()) return@LaunchedEffect
        loadingDetail = true
        repository.getAssignment(assignmentId)
            .onSuccess { assignment ->
                if (!assignment.can_edit) {
                    snackbar.showSnackbar("Only the teacher who created this assignment can update it.")
                    onBack()
                    return@onSuccess
                }
                title = assignment.title
                description = assignment.description.orEmpty()
                optionalDescription = assignment.instructions.orEmpty()
                dueDate = assignment.due_date
                maxMarks = assignment.max_marks?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "20"
                assignmentType = assignment.assignment_type.ifBlank { "Homework" }
                selectedSubjectId = assignment.subject_id.ifBlank { null }
                attachments = assignment.attachments
                audienceAll = assignment.audience_mode != "custom"
                pendingSection = assignment.section
                assignment.grade?.let(filters.onGradeSelected)
            }
            .onFailure { snackbar.showSnackbar(it.message ?: "Could not load assignment") }
        loadingDetail = false
    }

    LaunchedEffect(pendingSection, filters.sectionOptions) {
        val section = pendingSection ?: return@LaunchedEffect
        if (filters.sectionOptions.any { it.value == section }) {
            filters.onSectionSelected(section)
            pendingSection = null
        }
    }

    LaunchedEffect(filters.selectedGrade) {
        val grade = filters.selectedGrade ?: return@LaunchedEffect
        repository.getSubjects(grade).onSuccess { list ->
            subjects = list
            if (!isEdit && selectedSubjectId != null && list.none { it.id == selectedSubjectId }) {
                selectedSubjectId = null
            }
        }
    }

    LaunchedEffect(filters.selectedGrade, filters.selectedSection, audienceAll, isEdit) {
        if (isEdit || audienceAll) return@LaunchedEffect
        students = emptyList()
        selectedStudentIds = emptySet()
        val grade = filters.selectedGrade ?: return@LaunchedEffect
        val section = filters.selectedSection ?: return@LaunchedEffect
        loadingStudents = true
        repository.getEligibleStudents(grade, section)
            .onSuccess { students = it }
            .onFailure { snackbar.showSnackbar(it.message ?: "Could not load students") }
        loadingStudents = false
    }

    val canSubmit = title.isNotBlank()
        && filters.isReady
        && selectedSubjectId != null
        && dueDate.isNotBlank()
        && (isEdit || audienceAll || selectedStudentIds.isNotEmpty())
        && !uploading
        && !loadingDetail

    Scaffold(
        containerColor = colors.background,
        topBar = { AppBackTopBar(title = if (isEdit) "Edit assignment" else "New assignment", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppCard(modifier = Modifier.fillMaxWidth(), containerColor = colors.surface) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Class", style = MaterialTheme.typography.labelLarge, color = colors.primary, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            FilterOptionDropdown(
                                label = "Class",
                                options = filters.gradeOptions,
                                selectedValue = filters.selectedGrade,
                                onOptionSelected = filters.onGradeSelected,
                                allowAll = false,
                                enabled = !isEdit,
                                modifier = Modifier.weight(1f)
                            )
                            FilterOptionDropdown(
                                label = "Section",
                                options = filters.sectionOptions,
                                selectedValue = filters.selectedSection,
                                onOptionSelected = filters.onSectionSelected,
                                allowAll = false,
                                enabled = !isEdit,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        FilterOptionDropdown(
                            label = "Subject",
                            options = subjects.map { FilterOption(it.id, it.name) },
                            selectedValue = selectedSubjectId,
                            onOptionSelected = { selectedSubjectId = it },
                            allowAll = false,
                            enabled = !isEdit,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                AppCard(modifier = Modifier.fillMaxWidth(), containerColor = colors.surface) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Details", style = MaterialTheme.typography.labelLarge, color = colors.primary, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        OutlinedTextField(
                            value = optionalDescription,
                            onValueChange = { optionalDescription = it },
                            label = { Text("Optional description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        AttendanceDatePicker(
                            selectedDate = dueDate,
                            onDateChange = { dueDate = it },
                            label = "Due date",
                            compact = true
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = maxMarks,
                                onValueChange = { maxMarks = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                label = { Text("Marks") },
                                modifier = Modifier.weight(0.9f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors
                            )
                            FilterOptionDropdown(
                                label = "Type",
                                options = typeOptions.map { FilterOption(it, it) },
                                selectedValue = assignmentType,
                                onOptionSelected = { assignmentType = it ?: "Homework" },
                                allowAll = false,
                                modifier = Modifier.weight(1.1f)
                            )
                        }
                    }
                }

                AppCard(modifier = Modifier.fillMaxWidth(), containerColor = colors.surface) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Attachments (optional)", style = MaterialTheme.typography.labelLarge, color = colors.primary, fontWeight = FontWeight.SemiBold)
                        Text(
                            "PDF, images, Word, Excel, or text — uses the same upload as the website.",
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = pickFiles,
                            enabled = !uploading && !submitting,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uploading) {
                                AppLoaderCompact(size = 18.dp)
                            } else {
                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(if (uploading) "Uploading…" else "Add files")
                        }
                        if (attachments.isEmpty()) {
                            Text("No files attached", fontSize = 12.sp, color = colors.onSurfaceVariant)
                        } else {
                            attachments.forEach { file ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                    Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                                        Text(file.displayName, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                        file.file_size?.let { size ->
                                            Text("${(size / 1024).coerceAtLeast(1)} KB", fontSize = 11.sp, color = colors.onSurfaceVariant)
                                        }
                                    }
                                    IconButton(onClick = { attachments = attachments.filterNot { it.file_path == file.file_path } }) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = colors.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                AppCard(modifier = Modifier.fillMaxWidth(), containerColor = colors.surface) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Students", style = MaterialTheme.typography.labelLarge, color = colors.primary, fontWeight = FontWeight.SemiBold)
                        if (isEdit) {
                            Text(
                                "Class recipients stay the same. Saving will send an updated notification with sound.",
                                fontSize = 12.sp,
                                color = colors.onSurfaceVariant
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SecondaryBlue, RoundedCornerShape(12.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                AudienceTab("All", audienceAll, Modifier.weight(1f)) { audienceAll = true }
                                AudienceTab("Custom", !audienceAll, Modifier.weight(1f)) { audienceAll = false }
                            }
                            if (!audienceAll) {
                                when {
                                    loadingStudents -> {
                                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                            AppLoaderCompact(size = 28.dp)
                                        }
                                    }
                                    students.isEmpty() -> Text(
                                        "No students in this class",
                                        color = colors.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                    else -> {
                                        Text(
                                            "${selectedStudentIds.size} selected",
                                            fontSize = 12.sp,
                                            color = colors.onSurfaceVariant
                                        )
                                        students.forEach { student ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    selectedStudentIds = if (student.id in selectedStudentIds) {
                                                        selectedStudentIds - student.id
                                                    } else {
                                                        selectedStudentIds + student.id
                                                    }
                                                },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = student.id in selectedStudentIds,
                                                    onCheckedChange = { checked ->
                                                        selectedStudentIds = if (checked) selectedStudentIds + student.id else selectedStudentIds - student.id
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
                                Text("Every student in the selected class will be notified.", fontSize = 12.sp, color = colors.onSurfaceVariant)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            AppButton(
                text = if (isEdit) "Save & notify" else "Create & notify",
                onClick = {
                    val grade = filters.selectedGrade ?: return@AppButton
                    val section = filters.selectedSection ?: return@AppButton
                    val subjectId = selectedSubjectId ?: return@AppButton
                    submitting = true
                    scope.launch {
                        val result = if (isEdit && assignmentId != null) {
                            repository.updateAssignment(
                                assignmentId,
                                UpdateAssignmentBody(
                                    title = title.trim(),
                                    description = description.trim().ifBlank { null },
                                    instructions = optionalDescription.trim().ifBlank { null },
                                    due_date = dueDate,
                                    max_marks = maxMarks.toDoubleOrNull(),
                                    assignment_type = assignmentType,
                                    attachments = attachments,
                                    is_published = true,
                                    notify = true
                                )
                            )
                        } else {
                            repository.createAssignment(
                                CreateAssignmentBody(
                                    branch_id = SessionManager.user?.branch_id,
                                    grade = grade,
                                    section = section,
                                    subject_id = subjectId,
                                    title = title.trim(),
                                    description = description.trim().ifBlank { null },
                                    instructions = optionalDescription.trim().ifBlank { null },
                                    due_date = dueDate,
                                    max_marks = maxMarks.toDoubleOrNull(),
                                    assignment_type = assignmentType,
                                    audience_mode = if (audienceAll) "all" else "custom",
                                    student_ids = if (audienceAll) emptyList() else selectedStudentIds.toList(),
                                    is_published = true,
                                    attachments = attachments
                                )
                            )
                        }
                        submitting = false
                        result.onSuccess {
                            val updated = isEdit
                            showAppNotification(
                                if (updated) "Assignment updated" else "Assignment created",
                                if (updated) {
                                    "“${it.title}” — students and teachers were notified again."
                                } else {
                                    "“${it.title}” — students and teachers have been notified."
                                }
                            )
                            successWasUpdate = updated
                            successTitle = it.title
                        }.onFailure {
                            snackbar.showSnackbar(it.message ?: "Failed to save assignment")
                        }
                    }
                },
                enabled = canSubmit,
                isLoading = submitting,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }

    successTitle?.let { saved ->
        AlertDialog(
            onDismissRequest = {
                successTitle = null
                onSaved()
            },
            confirmButton = {
                TextButton(onClick = {
                    successTitle = null
                    onSaved()
                }) { Text("OK") }
            },
            title = { Text(if (successWasUpdate) "Update sent" else "Notification sent", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (successWasUpdate) {
                        "“$saved” was updated. Students and teachers received a new notification with sound."
                    } else {
                        "“$saved” was created. Students and teachers have been notified."
                    }
                )
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
