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
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.ui.components.AppBackTopBar
import com.example.myeduapp.core.ui.components.AppButton
import com.example.myeduapp.core.ui.theme.AttendanceDimens
import com.example.myeduapp.core.ui.theme.Background
import com.example.myeduapp.core.ui.theme.CardBackground
import com.example.myeduapp.core.ui.theme.PrimaryBlue

class MarkTeacherSelfAttendanceScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { MarkTeacherSelfAttendanceViewModel() }
        val uiState by viewModel.uiState.collectAsState()
        val snackbar = remember { SnackbarHostState() }

        LaunchedEffect(uiState.snackbarMessage, uiState.navigateBack) {
            uiState.snackbarMessage?.let { message ->
                snackbar.showSnackbar(message)
                viewModel.consumeSnackbar()
            }
            if (uiState.navigateBack) {
                viewModel.consumeNavigateBack()
                navigator.pop()
            }
        }

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
                            selectedDate = uiState.selectedDate,
                            onDateChange = viewModel::onDateChange,
                            label = "Attendance Date"
                        )
                        Text(
                            "Select Status",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue
                        )
                        AttendanceStatusGrid(
                            statuses = viewModel.statuses,
                            selected = uiState.selectedStatus,
                            onSelected = viewModel::onStatusChange
                        )
                    }
                }
                AppButton(
                    text = if (uiState.isSubmitting) "Saving..." else "Submit Attendance",
                    onClick = viewModel::submit,
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = uiState.isSubmitting
                )
            }
        }
    }
}
