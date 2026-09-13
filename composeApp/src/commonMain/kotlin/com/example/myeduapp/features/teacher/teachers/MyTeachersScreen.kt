 package com.example.myeduapp.features.teacher.teachers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.ui.components.*
import com.example.myeduapp.core.ui.filters.rememberAcademicYearFilter
import com.example.myeduapp.core.ui.icons.BigBridzIcon
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.data.model.FilterOption
import com.example.myeduapp.data.model.GradeOption
import com.example.myeduapp.data.model.Teacher
import com.example.myeduapp.data.model.UserRole
import com.example.myeduapp.data.repository.BranchRepository
import com.example.myeduapp.data.repository.ClassRepository
import com.example.myeduapp.data.repository.TeacherRepository
import kotlinx.coroutines.delay

class MyTeachersScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repository = remember { TeacherRepository() }
        val branchRepository = remember { BranchRepository() }
        val classRepository = remember { ClassRepository() }
        val academicYearFilter = rememberAcademicYearFilter()
        val role = SessionManager.user?.userRole
        val showBranchFilter = role == UserRole.SUPER_ADMIN

        var teachers by remember { mutableStateOf<List<Teacher>>(emptyList()) }
        var branchOptions by remember { mutableStateOf<List<FilterOption>>(emptyList()) }
        var grades by remember { mutableStateOf<List<GradeOption>>(emptyList()) }
        var sections by remember { mutableStateOf<List<FilterOption>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var searchQuery by remember { mutableStateOf("") }
        var selectedBranchId by remember { mutableStateOf<String?>(null) }
        var selectedGrade by remember { mutableStateOf<String?>(null) }
        var selectedSection by remember { mutableStateOf<String?>(null) }

        val gradeOptions = remember(grades) {
            grades.map { FilterOption(value = it.value, label = it.label) }
        }
        val effectiveBranchId = if (showBranchFilter) selectedBranchId?.toIntOrNull() else SessionManager.user?.branch_id

        LaunchedEffect(showBranchFilter) {
            if (showBranchFilter) {
                branchRepository.getBranchFilterOptions().onSuccess { branchOptions = it }
            }
        }

        LaunchedEffect(effectiveBranchId) {
            classRepository.getGrades(branchId = effectiveBranchId)
                .onSuccess { grades = it }
            selectedGrade = null
            selectedSection = null
        }

        LaunchedEffect(selectedGrade, effectiveBranchId) {
            classRepository.getSections(grade = selectedGrade, branchId = effectiveBranchId)
                .onSuccess { sections = it }
            if (selectedGrade == null) selectedSection = null
        }

        LaunchedEffect(
            searchQuery,
            effectiveBranchId,
            selectedGrade,
            selectedSection
        ) {
            if (searchQuery.length >= 2 || searchQuery.isEmpty()) {
                isLoading = true
                errorMessage = null
                teachers = emptyList()
                if (searchQuery.isNotEmpty()) delay(500)
                
                repository.getTeachers(
                    query = searchQuery.ifBlank { null },
                    branchId = effectiveBranchId,
                    grade = selectedGrade,
                    section = selectedSection
                ).onSuccess {
                    teachers = it
                    errorMessage = null
                }.onFailure {
                    errorMessage = it.message ?: "Failed to load teachers"
                }
                isLoading = false
            }
        }

        val hasActiveFilters = selectedBranchId != null || selectedGrade != null || selectedSection != null

        Scaffold(
            topBar = {
                AppBackTopBar(title = "Teachers", onBack = { navigator.pop() })
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search teachers...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterOptionDropdown(
                        label = "Academic Year",
                        options = academicYearFilter.options,
                        selectedValue = academicYearFilter.selectedYearId,
                        onOptionSelected = { yearId ->
                            yearId?.let { academicYearFilter.onYearSelected(it) }
                        },
                        allowAll = false,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (showBranchFilter) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterOptionDropdown(
                            label = "Branch",
                            options = branchOptions,
                            selectedValue = selectedBranchId,
                            onOptionSelected = {
                                selectedBranchId = it
                                selectedGrade = null
                                selectedSection = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterOptionDropdown(
                        label = "Class",
                        options = gradeOptions,
                        selectedValue = selectedGrade,
                        onOptionSelected = {
                            selectedGrade = it
                            selectedSection = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FilterOptionDropdown(
                        label = "Section",
                        options = sections,
                        selectedValue = selectedSection,
                        onOptionSelected = { selectedSection = it },
                        modifier = Modifier.weight(1f)
                    )
                    if (hasActiveFilters) {
                        ClearFiltersButton(
                            onClear = {
                                selectedBranchId = null
                                selectedGrade = null
                                selectedSection = null
                            }
                        )
                    }
                }

                if (isLoading) {
                    AppLoaderFullscreen(message = "Loading teachers...")
                } else if (errorMessage != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                } else if (teachers.isEmpty()) {
                    BigBridzEmptyState(
                        icon = BigBridzIcon.Teachers,
                        title = "No Teachers Found",
                        message = if (searchQuery.isNotEmpty() || hasActiveFilters)
                            "No teacher records match your active search or filters."
                        else "No teacher records available."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(teachers, key = { it.id }) { teacher ->
                            TeacherCard(
                                teacher = teacher,
                                onClick = { navigator.push(TeacherProfileScreen(teacher)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeacherCard(
    teacher: Teacher,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
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
                            text = "Class: $classTeacher",
                            fontSize = 11.sp,
                            color = SecondaryText.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Icon(Icons.Default.ChevronRight, contentDescription = "View Details", tint = Color.LightGray)
        }
    }
}
