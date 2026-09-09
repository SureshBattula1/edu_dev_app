package com.example.myeduapp.features.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.ui.components.AppBackTopBar
import com.example.myeduapp.core.ui.theme.Background
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.data.model.Attendance
import com.example.myeduapp.data.model.AttendanceOverview
import com.example.myeduapp.data.repository.AttendanceRepository

class ViewStudentAttendanceScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val userId = SessionManager.user?.id ?: return
        val repository = remember { AttendanceRepository() }

        var records by remember { mutableStateOf<List<Attendance>>(emptyList()) }
        var overview by remember { mutableStateOf<AttendanceOverview?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(userId) {
            isLoading = true
            repository.getStudentAttendance(userId)
                .onSuccess { records = it }
                .onFailure { error = it.message }
            repository.getStudentOverview(userId)
                .onSuccess { overview = it }
            isLoading = false
        }

        Scaffold(
            containerColor = Background,
            topBar = { AppBackTopBar(title = "My Attendance", onBack = { navigator.pop() }) }
        ) { padding ->
            when {
                isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
                error != null -> EmptyAttendanceState(
                    title = "Unable to load",
                    message = error ?: "Please try again",
                    modifier = Modifier.padding(padding)
                )
                records.isEmpty() -> EmptyAttendanceState(
                    title = "No records yet",
                    message = "Your attendance records will appear here once marked by teachers.",
                    modifier = Modifier.padding(padding)
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    overview?.let { item { AttendanceOverviewGrid(it) } }
                    items(records) { record -> AttendanceRecordCard(record) }
                }
            }
        }
    }
}
