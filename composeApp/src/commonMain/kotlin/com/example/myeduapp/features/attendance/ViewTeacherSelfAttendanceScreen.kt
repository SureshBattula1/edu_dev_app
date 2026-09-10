package com.example.myeduapp.features.attendance

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.data.model.Attendance
import com.example.myeduapp.data.model.AttendanceOverview
import com.example.myeduapp.data.repository.AttendanceRepository

class ViewTeacherSelfAttendanceScreen : Screen {
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
            repository.getTeacherAttendance(userId)
                .onSuccess { records = it }
                .onFailure { error = it.message }
            repository.getTeacherOverview(userId)
                .onSuccess { overview = it }
            isLoading = false
        }

        MyAttendanceScreen(
            records = records,
            overview = overview,
            isLoading = isLoading,
            error = error,
            emptyTitle = "No records yet",
            emptyMessage = "Your staff attendance history will appear here.",
            onBack = { navigator.pop() }
        )
    }
}
