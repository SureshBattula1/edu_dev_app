package com.example.myeduapp.features.teacher.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.ui.components.AppBackTopBar
import com.example.myeduapp.core.ui.components.AppButton
import com.example.myeduapp.core.ui.components.AppLoaderFullscreen
import com.example.myeduapp.core.ui.theme.AttendanceDimens
import com.example.myeduapp.core.ui.theme.Background
import com.example.myeduapp.core.ui.theme.CardBackground
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.util.DateUtils
import com.example.myeduapp.data.model.Attendance
import com.example.myeduapp.data.model.SchoolClass
import com.example.myeduapp.data.model.Student
import com.example.myeduapp.data.repository.AttendanceRepository
import com.example.myeduapp.data.repository.StudentRepository
import com.example.myeduapp.features.attendance.AttendanceFiltersPanel
import com.example.myeduapp.features.attendance.AttendanceStatusRow
import com.example.myeduapp.features.attendance.EmptyAttendanceState
import com.example.myeduapp.features.attendance.rememberAttendanceClassSectionFilters
import kotlinx.coroutines.launch

class CreateAttendanceScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val filterState = rememberAttendanceClassSectionFilters()
        var selectedDate by remember { mutableStateOf(DateUtils.today()) }

        Scaffold(
            containerColor = Background,
            topBar = {
                AppBackTopBar(title = "Mark Attendance", onBack = { navigator.pop() })
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                AttendanceFiltersPanel(
                    state = filterState,
                    selectedDate = selectedDate,
                    onDateChange = { selectedDate = it }
                )

                if (!filterState.isReady) {
                    EmptyAttendanceState(
                        title = "Select class & section",
                        message = "Choose a class and section above to load students.",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    filterState.selectedClass?.let { schoolClass ->
                        MarkAttendanceStep(
                            schoolClass = schoolClass,
                            date = selectedDate,
                            onSuccess = { navigator.pop() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

private fun Student.matchesAttendanceUser(userId: String): Boolean =
    attendanceUserId == userId || user_id == userId || id == userId

private fun applyExistingAttendance(
    students: List<Student>,
    existing: List<Attendance>,
    attendanceStates: MutableMap<String, String>,
    remarksStates: MutableMap<String, String>
): Boolean {
    var applied = false
    existing.forEach { record ->
        val userId = record.student_id?.takeIf { it.isNotBlank() && it != "0" } ?: return@forEach
        val student = students.find { it.matchesAttendanceUser(userId) }
        val key = student?.attendanceUserId ?: userId
        attendanceStates[key] = record.status
        remarksStates[key] = record.remarks ?: ""
        applied = true
    }
    return applied
}

@Composable
fun MarkAttendanceStep(
    schoolClass: SchoolClass,
    date: String,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val studentRepo = remember { StudentRepository() }
    val attendanceRepo = remember { AttendanceRepository() }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var students by remember { mutableStateOf<List<Student>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isUpdateMode by remember { mutableStateOf(false) }
    val attendanceStates = remember { mutableStateMapOf<String, String>() }
    val remarksStates = remember { mutableStateMapOf<String, String>() }
    val statuses = listOf("Present", "Absent", "Late", "Half-Day", "Sick Leave")

    LaunchedEffect(schoolClass.displayGrade, schoolClass.section, date) {
        isLoading = true
        isUpdateMode = false
        attendanceStates.clear()
        remarksStates.clear()

        val list = studentRepo.getStudentsByClass(schoolClass.displayGrade, schoolClass.section)
            .getOrElse { emptyList() }
        students = list

        list.forEach { student ->
            val key = student.attendanceUserId
            attendanceStates[key] = "Present"
            remarksStates[key] = ""
        }

        if (list.isNotEmpty()) {
            attendanceRepo.getClassAttendance(schoolClass, date).onSuccess { result ->
                if (result.attendance.isNotEmpty()) {
                    isUpdateMode = applyExistingAttendance(
                        students = list,
                        existing = result.attendance,
                        attendanceStates = attendanceStates,
                        remarksStates = remarksStates
                    )
                }
            }
        }

        isLoading = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> AppLoaderFullscreen(message = "Loading students")
            students.isEmpty() -> {
                EmptyAttendanceState(
                    title = "No students found",
                    message = "No active students in ${schoolClass.displayName}.",
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AttendanceDimens.ScreenHorizontal, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${schoolClass.displayName} • ${students.size} students",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue
                        )
                        BulkStatusDropdown(
                            statuses = statuses,
                            onApplyToAll = { status ->
                                students.forEach { attendanceStates[it.attendanceUserId] = status }
                            }
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(
                            horizontal = AttendanceDimens.ScreenHorizontal,
                            vertical = 2.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(AttendanceDimens.ListSpacing)
                    ) {
                        items(students, key = { it.attendanceUserId }) { student ->
                            val userKey = student.attendanceUserId
                            AttendanceMarkRow(
                                student = student,
                                status = attendanceStates[userKey] ?: "Present",
                                remarks = remarksStates[userKey] ?: "",
                                statuses = statuses,
                                onStatusChange = { attendanceStates[userKey] = it },
                                onRemarksChange = { remarksStates[userKey] = it }
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 6.dp,
                        shadowElevation = 4.dp,
                        color = CardBackground
                    ) {
                        AppButton(
                            text = when {
                                isSubmitting && isUpdateMode -> "Updating..."
                                isSubmitting -> "Submitting..."
                                isUpdateMode -> "Update Attendance"
                                else -> "Submit Attendance"
                            },
                            onClick = {
                                if (isSubmitting) return@AppButton
                                isSubmitting = true
                                scope.launch {
                                    attendanceRepo.submitStudentAttendance(
                                        schoolClass = schoolClass,
                                        date = date,
                                        students = students,
                                        statusByUserId = attendanceStates.toMap(),
                                        remarksByUserId = remarksStates.toMap(),
                                        isUpdate = isUpdateMode
                                    ).onSuccess {
                                        snackbar.showSnackbar(it)
                                        onSuccess()
                                    }.onFailure {
                                        snackbar.showSnackbar(it.message ?: "Failed to save")
                                    }
                                    isSubmitting = false
                                }
                            },
                            modifier = Modifier
                                .padding(
                                    horizontal = AttendanceDimens.ScreenHorizontal,
                                    vertical = 8.dp
                                )
                                .fillMaxWidth(),
                            isLoading = isSubmitting
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkStatusDropdown(
    statuses: List<String>,
    onApplyToAll: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val bulkOptions = listOf("All Present", "All Absent", "All Late", "All Half-Day", "All Sick Leave")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
        ) {
            Text("Mark All", fontSize = 12.sp, color = PrimaryBlue)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            bulkOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        val status = option.removePrefix("All ").trim()
                        if (statuses.contains(status)) {
                            onApplyToAll(status)
                        }
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AttendanceMarkRow(
    student: Student,
    status: String,
    remarks: String,
    statuses: List<String>,
    onStatusChange: (String) -> Unit,
    onRemarksChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(AttendanceDimens.MarkRowPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(AttendanceDimens.MarkAvatarSize)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        student.roll_number ?: "?",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    student.full_name,
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }

            AttendanceStatusRow(
                statuses = statuses,
                selected = status,
                onSelected = onStatusChange
            )

            OutlinedTextField(
                value = remarks,
                onValueChange = onRemarksChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = AttendanceDimens.MarkRemarksHeight,
                        max = AttendanceDimens.MarkRemarksHeight * 2
                    ),
                placeholder = { Text("Remarks", fontSize = 12.sp) },
                minLines = 1,
                maxLines = 3,
                shape = RoundedCornerShape(8.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Background,
                    focusedContainerColor = Background,
                    focusedBorderColor = PrimaryBlue
                )
            )
        }
    }
}
