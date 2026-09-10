package com.example.myeduapp.features.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myeduapp.core.ui.components.AppLoaderFullscreen
import com.example.myeduapp.data.model.Attendance
import com.example.myeduapp.data.model.AttendanceOverview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAttendanceScreen(
    records: List<Attendance>,
    overview: AttendanceOverview?,
    isLoading: Boolean,
    error: String?,
    emptyTitle: String,
    emptyMessage: String,
    onBack: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Attendance", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Calendar view & summary",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primary,
                    titleContentColor = colorScheme.onPrimary,
                    navigationIconContentColor = colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        when {
            isLoading -> AppLoaderFullscreen(
                modifier = Modifier.padding(padding),
                message = "Loading attendance"
            )
            error != null -> ThemedEmptyAttendanceState(
                title = "Unable to load",
                message = error,
                modifier = Modifier.padding(padding)
            )
            records.isEmpty() -> ThemedEmptyAttendanceState(
                title = emptyTitle,
                message = emptyMessage,
                modifier = Modifier.padding(padding)
            )
            else -> AttendanceCalendarView(
                records = records,
                overview = overview,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun ThemedEmptyAttendanceState(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            color = colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
