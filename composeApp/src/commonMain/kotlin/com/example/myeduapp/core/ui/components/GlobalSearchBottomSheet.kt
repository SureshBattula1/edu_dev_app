package com.example.myeduapp.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myeduapp.core.ui.icons.BigBridz3DIcon
import com.example.myeduapp.core.ui.icons.BigBridzIcon
import com.example.myeduapp.core.ui.icons.BigBridzIconRegistry
import com.example.myeduapp.core.ui.theme.*

data class SearchResultItem(
    val title: String,
    val subtitle: String,
    val route: String,
    val icon: BigBridzIcon
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchBottomSheet(
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val allModules = remember {
        listOf(
            SearchResultItem("Students Directory", "Search & view student profiles", "my_students", BigBridzIcon.Students),
            SearchResultItem("Teachers Directory", "Search & view faculty staff", "teachers", BigBridzIcon.Teachers),
            SearchResultItem("Attendance Hub", "View & mark daily attendance", "attendance", BigBridzIcon.Attendance),
            SearchResultItem("Fees & Payments", "Fee dues & payment records", "fees", BigBridzIcon.Fees),
            SearchResultItem("Assignments", "Class homework & submissions", "assignments", BigBridzIcon.EmptyAssignments),
            SearchResultItem("Exams & Tests", "Exam schedules & marks entry", "exams", BigBridzIcon.EmptyExams),
            SearchResultItem("Daily Timetable", "Class schedules & room slots", "timetable", BigBridzIcon.Timetable),
            SearchResultItem("Notices & Circulars", "School announcements", "notices", BigBridzIcon.Notices),
            SearchResultItem("Notifications", "Alerts & inbox campaigns", "notifications", BigBridzIcon.EmptyNotifications),
            SearchResultItem("My Profile", "Account details & security", "profile", BigBridzIcon.Profile),
            SearchResultItem("Branches", "School campus branches", "branches", BigBridzIcon.Branches)
        )
    }

    val filteredResults = remember(query) {
        if (query.isBlank()) {
            emptyList()
        } else {
            allModules.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.subtitle.contains(query, ignoreCase = true) ||
                it.route.contains(query, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = CardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            BigBridzSearchBar(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Search...",
                modifier = Modifier.fillMaxWidth()
            )

            if (filteredResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(0.65f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredResults, key = { it.title }) { item ->
                        AppCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDismiss()
                                    onNavigate(item.route)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BigBridz3DIcon(
                                    icon = item.icon,
                                    size = 28.dp
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryText
                                    )
                                    Text(
                                        text = item.subtitle,
                                        fontSize = 12.sp,
                                        color = SecondaryText
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = SecondaryText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
