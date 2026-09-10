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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.datastore.SessionManager
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
import com.example.myeduapp.core.util.DateUtils
import com.example.myeduapp.data.model.AttendanceNotifyClass
import com.example.myeduapp.data.model.AttendanceNotifyReceipts
import com.example.myeduapp.data.model.ClassAttendanceStatus
import com.example.myeduapp.data.model.UserRole
import com.example.myeduapp.data.repository.AttendanceRepository
import com.example.myeduapp.features.attendance.AttendanceDatePicker
import com.example.myeduapp.features.attendance.EmptyAttendanceState
import kotlinx.coroutines.launch

class TeacherAttendanceOverviewScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repository = remember { AttendanceRepository() }
        val scope = rememberCoroutineScope()
        val snackbar = remember { SnackbarHostState() }
        val canNotify = SessionManager.user?.userRole in listOf(UserRole.BRANCH_ADMIN, UserRole.SUPER_ADMIN)

        var date by remember { mutableStateOf(DateUtils.today()) }
        var rows by remember { mutableStateOf<List<ClassAttendanceStatus>>(emptyList()) }
        var selected by remember { mutableStateOf(setOf<String>()) }
        var loading by remember { mutableStateOf(true) }
        var sending by remember { mutableStateOf(false) }
        var confirmSend by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var receiptsFor by remember { mutableStateOf<ClassAttendanceStatus?>(null) }

        val createdRows = rows.filter { it.created }
        val selectedCreated = selected.filter { key -> createdRows.any { it.selectionKey == key } }

        suspend fun load() {
            loading = true
            error = null
            repository.getClassStatus(date)
                .onSuccess {
                    rows = it
                    selected = selected.filter { key -> it.any { row -> row.created && row.selectionKey == key } }.toSet()
                }
                .onFailure { error = it.message ?: "Could not load attendance" }
            loading = false
        }

        LaunchedEffect(date) {
            selected = emptySet()
            load()
        }

        Scaffold(
            containerColor = Background,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                AppBackTopBar(
                    title = "Today’s Attendance",
                    onBack = { navigator.pop() },
                    actions = {
                        IconButton(onClick = { scope.launch { load() } }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                AttendanceDatePicker(
                    selectedDate = date,
                    onDateChange = { date = it },
                    label = "Date",
                    compact = true,
                    modifier = Modifier.padding(horizontal = AttendanceDimens.ScreenHorizontal, vertical = 8.dp)
                )

                if (canNotify && !loading && rows.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AttendanceDimens.ScreenHorizontal),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { selected = createdRows.map { it.selectionKey }.toSet() },
                            enabled = createdRows.isNotEmpty()
                        ) { Text("Select all created", fontSize = 12.sp, color = PrimaryBlue) }
                        TextButton(
                            onClick = { selected = emptySet() },
                            enabled = selected.isNotEmpty()
                        ) { Text("Uncheck all", fontSize = 12.sp, color = PrimaryBlue) }
                        Spacer(modifier = Modifier.weight(1f))
                        Text("${selectedCreated.size} selected", fontSize = 11.sp, color = SecondaryText)
                    }
                }

                when {
                    loading -> AppLoaderFullscreen(message = "Loading classes")
                    error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(error ?: "Could not load", color = SecondaryText, fontSize = 13.sp)
                            TextButton(onClick = { scope.launch { load() } }) { Text("Retry", color = PrimaryBlue) }
                        }
                    }
                    rows.isEmpty() -> EmptyAttendanceState(
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
                            items(rows, key = { it.selectionKey }) { row ->
                                val key = row.selectionKey
                                val checkboxEnabled = canNotify && row.created
                                AppCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        navigator.push(
                                            CreateAttendanceScreen(
                                                initialGrade = row.grade,
                                                initialSection = row.section,
                                                initialDate = date
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
                                        if (canNotify) {
                                            Checkbox(
                                                checked = key in selected,
                                                onCheckedChange = { checked ->
                                                    if (!row.created) return@Checkbox
                                                    selected = if (checked) selected + key else selected - key
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
                                            if (canNotify && row.notify_sent) {
                                                Text(
                                                    "Sent",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = SuccessColor,
                                                    modifier = Modifier.clickable { receiptsFor = row }
                                                )
                                            } else if (canNotify && row.created) {
                                                Text("Not sent", fontSize = 10.sp, color = SecondaryText)
                                            } else if (!row.created) {
                                                Text("Disabled", fontSize = 10.sp, color = SecondaryText)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (canNotify) {
                            Surface(
                                tonalElevation = 4.dp,
                                shadowElevation = 4.dp,
                                color = Color.White
                            ) {
                                AppButton(
                                    text = "Send Notification",
                                    onClick = { confirmSend = true },
                                    enabled = selectedCreated.isNotEmpty() && !sending,
                                    isLoading = sending,
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

        if (confirmSend) {
            AlertDialog(
                onDismissRequest = { confirmSend = false },
                title = { Text("Send attendance notifications?", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Send Present/Absent status to students in ${selectedCreated.size} selected class(es)? " +
                            "Only classes with created attendance will be notified."
                    )
                },
                dismissButton = {
                    TextButton(onClick = { confirmSend = false }) { Text("Cancel") }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            confirmSend = false
                            val classes = rows
                                .filter { it.selectionKey in selectedCreated && it.created }
                                .mapNotNull { row ->
                                    val grade = row.grade?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                                    val section = row.section?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                                    AttendanceNotifyClass(grade = grade, section = section)
                                }
                            if (classes.isEmpty()) return@TextButton
                            sending = true
                            scope.launch {
                                repository.notifyStudents(date, classes)
                                    .onSuccess { result ->
                                        snackbar.showSnackbar(
                                            "Sent to ${result.student_count} student(s)" +
                                                if (result.skipped_no_login > 0) " · ${result.skipped_no_login} skipped (no login)" else ""
                                        )
                                        selected = emptySet()
                                        load()
                                    }
                                    .onFailure {
                                        snackbar.showSnackbar(it.message ?: "Failed to send")
                                    }
                                sending = false
                            }
                        }
                    ) { Text("Send", color = PrimaryBlue, fontWeight = FontWeight.SemiBold) }
                }
            )
        }

        receiptsFor?.let { row ->
            AttendanceNotifyReceiptsDialog(
                date = date,
                row = row,
                repository = repository,
                onDismiss = { receiptsFor = null }
            )
        }
    }
}

@Composable
private fun AttendanceNotifyReceiptsDialog(
    date: String,
    row: ClassAttendanceStatus,
    repository: AttendanceRepository,
    onDismiss: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var data by remember { mutableStateOf<AttendanceNotifyReceipts?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(row.selectionKey, date) {
        loading = true
        error = null
        repository.getNotifyReceipts(date, row.grade.orEmpty(), row.section.orEmpty())
            .onSuccess { data = it }
            .onFailure { error = it.message ?: "Could not load status" }
        loading = false
    }

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
                    error != null -> Text(error ?: "", color = SecondaryText, fontSize = 13.sp)
                    else -> {
                        val receipts = data
                        Text(
                            "${receipts?.sent_count ?: 0} of ${receipts?.total ?: 0} sent",
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
                            receipts?.students.orEmpty().forEach { student ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(student.name.ifBlank { "Student" }, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
