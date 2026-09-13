package com.example.myeduapp.features.teacher.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.ui.components.AppBackTopBar
import com.example.myeduapp.core.ui.components.AppLoaderFullscreen
import com.example.myeduapp.core.ui.theme.AttendanceDimens
import com.example.myeduapp.core.ui.theme.Background
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SuccessColor
import com.example.myeduapp.core.ui.theme.ErrorColor
import com.example.myeduapp.core.util.DateUtils
import com.example.myeduapp.features.attendance.AttendanceFiltersPanel
import com.example.myeduapp.features.attendance.AttendanceRecordCard
import com.example.myeduapp.features.attendance.AttendanceSectionHeader
import com.example.myeduapp.features.attendance.EmptyAttendanceState
import com.example.myeduapp.features.attendance.OverviewChip
import com.example.myeduapp.features.attendance.rememberAttendanceClassSectionFilters

class TeacherAttendanceScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { TeacherAttendanceViewModel() }
        val uiState by viewModel.uiState.collectAsState()
        val filterState = rememberAttendanceClassSectionFilters()

        LaunchedEffect(filterState.selectedClass, uiState.selectedDate) {
            viewModel.loadAttendance(filterState.selectedClass)
        }

        Scaffold(
            containerColor = Background,
            topBar = {
                AppBackTopBar(
                    title = "View Attendance",
                    onBack = { navigator.pop() },
                    actions = {
                        IconButton(onClick = { navigator.push(CreateAttendanceScreen()) }) {
                            Icon(Icons.Default.Add, contentDescription = "Mark attendance")
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                AttendanceFiltersPanel(
                    state = filterState,
                    selectedDate = uiState.selectedDate,
                    onDateChange = viewModel::onDateChange
                )

                if (!filterState.isReady) {
                    EmptyAttendanceState(
                        title = "Select class & section",
                        message = "Choose a class and section above to view attendance records.",
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (uiState.isLoading) {
                    AppLoaderFullscreen(message = "Loading attendance")
                } else if (uiState.attendanceRecords.isEmpty()) {
                    EmptyAttendanceState(
                        title = "No attendance marked",
                        message = "No records for ${DateUtils.formatDisplay(uiState.selectedDate)}.",
                        modifier = Modifier.fillMaxSize(),
                        actionLabel = "Mark Attendance",
                        onAction = { navigator.push(CreateAttendanceScreen()) }
                    )
                } else {
                    val present = uiState.attendanceRecords.count { it.status == "Present" }
                    val absent = uiState.attendanceRecords.count { it.status == "Absent" }
                    val late = uiState.attendanceRecords.count { it.status == "Late" }

                    Column(modifier = Modifier.fillMaxSize()) {
                        AttendanceSectionHeader(
                            title = filterState.selectedClass?.displayName ?: "Class",
                            subtitle = DateUtils.formatLongDisplay(uiState.selectedDate)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AttendanceDimens.ScreenHorizontal),
                            horizontalArrangement = Arrangement.spacedBy(AttendanceDimens.ChipSpacing)
                        ) {
                            OverviewChip("Present", present.toString(), SuccessColor, Modifier.weight(1f))
                            OverviewChip("Absent", absent.toString(), ErrorColor, Modifier.weight(1f))
                            OverviewChip("Late", late.toString(), PrimaryBlue, Modifier.weight(1f))
                            OverviewChip(
                                "Total",
                                uiState.attendanceRecords.size.toString(),
                                PrimaryBlue,
                                Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = AttendanceDimens.ScreenHorizontal,
                                vertical = 4.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(AttendanceDimens.ListSpacing)
                        ) {
                            items(
                                items = uiState.attendanceRecords,
                                key = { record -> record.listKey }
                            ) { record ->
                                AttendanceRecordCard(record)
                            }
                        }
                    }
                }
            }
        }
    }
}
