package com.example.myeduapp.features.teacher.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.ui.theme.SuccessColor
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

class CreateAttendanceScreen(
    private val initialGrade: String? = null,
    private val initialSection: String? = null,
    private val initialDate: String? = null
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val filterState = rememberAttendanceClassSectionFilters()
        var selectedDate by remember {
            mutableStateOf(initialDate?.takeIf { it.isNotBlank() } ?: DateUtils.today())
        }
        var pendingSection by remember { mutableStateOf(initialSection) }

        LaunchedEffect(initialGrade) {
            initialGrade?.takeIf { it.isNotBlank() }?.let(filterState.onGradeSelected)
        }
        LaunchedEffect(pendingSection, filterState.sectionOptions) {
            val section = pendingSection ?: return@LaunchedEffect
            if (filterState.sectionOptions.any { it.value == section }) {
                filterState.onSectionSelected(section)
                pendingSection = null
            }
        }

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
    var loadError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isUpdateMode by remember { mutableStateOf(false) }
    var confirmSubmit by remember { mutableStateOf(false) }
    val attendanceStates = remember { mutableStateMapOf<String, String>() }
    val remarksStates = remember { mutableStateMapOf<String, String>() }
    val statuses = listOf("Present", "Absent", "Late", "Half-Day", "Sick Leave")

    val presentCount by remember {
        derivedStateOf {
            students.count { (attendanceStates[it.attendanceUserId] ?: "Present") == "Present" }
        }
    }
    val absentCount by remember {
        derivedStateOf {
            students.count { (attendanceStates[it.attendanceUserId] ?: "Present") == "Absent" }
        }
    }

    suspend fun loadStudents() {
        isLoading = true
        loadError = null
        isUpdateMode = false
        attendanceStates.clear()
        remarksStates.clear()

        val listResult = studentRepo.getStudentsByClass(schoolClass.displayGrade, schoolClass.section)
        val list = listResult.getOrElse {
            loadError = it.message ?: "Could not load students"
            emptyList()
        }
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

    LaunchedEffect(schoolClass.displayGrade, schoolClass.section, date) {
        loadStudents()
    }

    fun submitNow() {
        if (isSubmitting) return
        isSubmitting = true
        scope.launch {
            attendanceRepo.submitStudentAttendance(
                schoolClass = schoolClass,
                date = date,
                students = students,
                statusByUserId = attendanceStates.toMap(),
                remarksByUserId = remarksStates.toMap(),
                isUpdate = isUpdateMode
            ).onSuccess { message ->
                snackbar.showSnackbar(message)
                onSuccess()
            }.onFailure {
                snackbar.showSnackbar(it.message ?: "Failed to save")
            }
            isSubmitting = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> AppLoaderFullscreen(message = "Loading students")
            loadError != null -> {
                EmptyAttendanceState(
                    title = "Could not load",
                    message = loadError ?: "Please try again.",
                    modifier = Modifier.fillMaxSize()
                )
                TextButton(
                    onClick = { scope.launch { loadStudents() } },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                ) { Text("Retry", color = PrimaryBlue) }
            }
            students.isEmpty() -> {
                EmptyAttendanceState(
                    title = "No students found",
                    message = "No active students in ${schoolClass.displayName}.",
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AttendanceDimens.ScreenHorizontal, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                schoolClass.displayName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${students.size} students",
                                fontSize = 12.sp,
                                color = SecondaryText
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Present $presentCount", fontSize = 12.sp, color = SuccessColor, fontWeight = FontWeight.SemiBold)
                            Text("Absent $absentCount", fontSize = 12.sp, color = SecondaryText, fontWeight = FontWeight.SemiBold)
                            if (isUpdateMode) {
                                Text("Attendance already submitted", fontSize = 11.sp, color = PrimaryBlue)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    students.forEach { attendanceStates[it.attendanceUserId] = "Present" }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text("Mark All Present", fontSize = 12.sp, color = PrimaryBlue)
                            }
                            OutlinedButton(
                                onClick = {
                                    students.forEach { attendanceStates[it.attendanceUserId] = "Absent" }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.heightIn(min = 32.dp)
                            ) {
                                Text("Unmark All", fontSize = 12.sp, color = PrimaryBlue)
                            }
                        }
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
                            onClick = { confirmSubmit = true },
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

    if (confirmSubmit) {
        AlertDialog(
            onDismissRequest = { confirmSubmit = false },
            title = { Text("Submit Attendance?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(schoolClass.displayName)
                    Text("Present: $presentCount · Absent: $absentCount")
                    if (isUpdateMode) {
                        Text("Attendance already submitted — this will update existing records.", fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmSubmit = false }) { Text("Cancel") }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmSubmit = false
                        submitNow()
                    }
                ) { Text("Submit", color = PrimaryBlue, fontWeight = FontWeight.SemiBold) }
            }
        )
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
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(0.dp)
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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    student.full_name,
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
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
                maxLines = 2,
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
