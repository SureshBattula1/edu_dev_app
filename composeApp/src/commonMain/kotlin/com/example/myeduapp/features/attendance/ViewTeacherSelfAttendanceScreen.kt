package com.example.myeduapp.features.attendance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class ViewTeacherSelfAttendanceScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { ViewTeacherSelfAttendanceViewModel() }
        val uiState by viewModel.uiState.collectAsState()

        MyAttendanceScreen(
            records = uiState.records,
            overview = uiState.overview,
            isLoading = uiState.isLoading,
            error = uiState.error,
            emptyTitle = "No records yet",
            emptyMessage = "Your staff attendance history will appear here.",
            onBack = { navigator.pop() }
        )
    }
}
