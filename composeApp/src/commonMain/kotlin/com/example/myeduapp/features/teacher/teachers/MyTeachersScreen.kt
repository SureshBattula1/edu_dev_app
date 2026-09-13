package com.example.myeduapp.features.teacher.teachers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.ui.components.AppBackTopBar
import com.example.myeduapp.core.ui.components.AppCard
import com.example.myeduapp.core.ui.components.AppLoaderFullscreen
import com.example.myeduapp.core.ui.components.ClearFiltersButton
import com.example.myeduapp.core.ui.components.FilterOptionDropdown
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.data.model.Teacher

/**
 * Teachers list mirrors Students filters by role:
 * - SuperAdmin: Branch
 * - BranchAdmin: Class + Section (class-teacher assignment)
 */
class MyTeachersScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { MyTeachersViewModel() }
        val uiState by viewModel.uiState.collectAsState()

        Scaffold(
            topBar = {
                AppBackTopBar(title = "Teachers", onBack = { navigator.pop() })
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search by name, employee id...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                if (uiState.showBranchFilter) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterOptionDropdown(
                            label = "Branch",
                            options = uiState.branchOptions,
                            selectedValue = uiState.selectedBranchId,
                            onOptionSelected = viewModel::onBranchSelected,
                            modifier = Modifier.weight(1f)
                        )
                        if (uiState.hasActiveFilters) {
                            ClearFiltersButton(onClear = viewModel::clearBranchFilter)
                        }
                    }
                }

                if (uiState.showClassSectionFilters) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterOptionDropdown(
                            label = "Class",
                            options = uiState.gradeOptions,
                            selectedValue = uiState.selectedGrade,
                            onOptionSelected = viewModel::onGradeSelected,
                            modifier = Modifier.weight(1f)
                        )
                        FilterOptionDropdown(
                            label = "Section",
                            options = uiState.sections,
                            selectedValue = uiState.selectedSection,
                            onOptionSelected = viewModel::onSectionSelected,
                            modifier = Modifier.weight(1f)
                        )
                        if (uiState.hasActiveFilters) {
                            ClearFiltersButton(onClear = viewModel::clearClassFilters)
                        }
                    }
                }

                if (uiState.isLoading) {
                    AppLoaderFullscreen(message = "Loading teachers")
                } else if (uiState.teachers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if (uiState.searchQuery.isNotEmpty() || uiState.hasActiveFilters)
                                "No teachers found with current filters"
                            else "No teachers available",
                            color = SecondaryText
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.teachers) { teacher ->
                            TeacherCard(teacher)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeacherCard(teacher: Teacher) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(teacher.fullName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = teacher.designation ?: teacher.category_type ?: "Teacher",
                    fontSize = 13.sp,
                    color = SecondaryText
                )
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "Emp: ${teacher.employee_id ?: "N/A"}",
                        fontSize = 11.sp,
                        color = SecondaryText.copy(alpha = 0.7f)
                    )
                    teacher.branchName?.let { name ->
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            color = SecondaryText.copy(alpha = 0.7f)
                        )
                    }
                    val classTeacher = listOfNotNull(
                        teacher.class_teacher_of_grade,
                        teacher.class_teacher_of_section
                    ).joinToString(" - ")
                    if (classTeacher.isNotBlank()) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = classTeacher,
                            fontSize = 11.sp,
                            color = SecondaryText.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
