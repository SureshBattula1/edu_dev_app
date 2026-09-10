package com.example.myeduapp.features.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.ui.components.AppBackTopBar
import com.example.myeduapp.core.ui.components.AppButton
import com.example.myeduapp.core.ui.theme.AttendanceDimens
import com.example.myeduapp.core.ui.theme.Background
import com.example.myeduapp.core.ui.theme.CardBackground
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.util.DateUtils
import com.example.myeduapp.data.repository.AttendanceRepository
import kotlinx.coroutines.launch

class MarkTeacherSelfAttendanceScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repository = remember { AttendanceRepository() }
        val scope = rememberCoroutineScope()
        val snackbar = remember { SnackbarHostState() }

        var selectedDate by remember { mutableStateOf(DateUtils.today()) }
        var selectedStatus by remember { mutableStateOf("Present") }
        var isSubmitting by remember { mutableStateOf(false) }

        val statuses = listOf("Present", "Absent", "Late", "Half-Day", "Sick Leave", "Leave")

        Scaffold(
            containerColor = Background,
            topBar = { AppBackTopBar(title = "Mark My Attendance", onBack = { navigator.pop() }) },
            snackbarHost = { SnackbarHost(snackbar) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(AttendanceDimens.ScreenHorizontal),
                verticalArrangement = Arrangement.spacedBy(AttendanceDimens.ItemSpacing)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(AttendanceDimens.CardPadding),
                        verticalArrangement = Arrangement.spacedBy(AttendanceDimens.ItemSpacing)
                    ) {
                        AttendanceDatePicker(
                            selectedDate = selectedDate,
                            onDateChange = { selectedDate = it },
                            label = "Attendance Date"
                        )
                        Text(
                            "Select Status",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue
                        )
                        AttendanceStatusGrid(
                            statuses = statuses,
                            selected = selectedStatus,
                            onSelected = { selectedStatus = it }
                        )
                    }
                }
                AppButton(
                    text = if (isSubmitting) "Saving..." else "Submit Attendance",
                    onClick = {
                        if (isSubmitting) return@AppButton
                        isSubmitting = true
                        scope.launch {
                            repository.submitTeacherSelfAttendance(selectedDate, selectedStatus)
                                .onSuccess {
                                    snackbar.showSnackbar(it)
                                    navigator.pop()
                                }
                                .onFailure { snackbar.showSnackbar(it.message ?: "Failed to submit") }
                            isSubmitting = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = isSubmitting
                )
            }
        }
    }
}
