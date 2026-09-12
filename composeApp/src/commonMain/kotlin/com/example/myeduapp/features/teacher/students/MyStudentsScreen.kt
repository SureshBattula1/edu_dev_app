package com.example.myeduapp.features.teacher.students

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.myeduapp.core.ui.components.AppBackTopBar
import com.example.myeduapp.core.ui.components.AppCard
import com.example.myeduapp.core.ui.components.AppLoader
import com.example.myeduapp.core.ui.components.AppLoaderFullscreen
import com.example.myeduapp.core.ui.components.ClearFiltersButton
import com.example.myeduapp.core.ui.components.FilterOptionDropdown
import com.example.myeduapp.core.ui.filters.rememberAcademicYearFilter
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.data.model.FilterOption
import com.example.myeduapp.data.model.GradeOption
import com.example.myeduapp.data.model.Student
import com.example.myeduapp.data.model.UserRole
import com.example.myeduapp.data.repository.BranchRepository
import com.example.myeduapp.data.repository.ClassRepository
import com.example.myeduapp.data.repository.StudentRepository
import com.example.myeduapp.features.teacher.student360.Student360Screen
import kotlinx.coroutines.delay

class MyStudentsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repository = remember { StudentRepository() }
        val classRepository = remember { ClassRepository() }
        val branchRepository = remember { BranchRepository() }
        val academicYearFilter = rememberAcademicYearFilter()
        val role = SessionManager.user?.userRole
        val showBranchFilter = role == UserRole.SUPER_ADMIN

        var students by remember { mutableStateOf<List<Student>>(emptyList()) }
        var branchOptions by remember { mutableStateOf<List<FilterOption>>(emptyList()) }
        var grades by remember { mutableStateOf<List<GradeOption>>(emptyList()) }
        var sections by remember { mutableStateOf<List<FilterOption>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }

        var searchQuery by remember { mutableStateOf("") }
        var selectedBranchId by remember { mutableStateOf<String?>(null) }
        var selectedGrade by remember { mutableStateOf<String?>(null) }
        var selectedSection by remember { mutableStateOf<String?>(null) }

        val gradeOptions = remember(grades) {
            grades.map { FilterOption(value = it.value, label = it.label) }
        }
        val selectedBranchInt = selectedBranchId?.toIntOrNull()

        LaunchedEffect(showBranchFilter) {
            if (showBranchFilter) {
                branchRepository.getBranchFilterOptions().onSuccess { branchOptions = it }
            }
        }

        LaunchedEffect(academicYearFilter.selectedYearId, selectedBranchId, showBranchFilter) {
            if (!academicYearFilter.isReady) return@LaunchedEffect
            classRepository.getGrades(branchId = if (showBranchFilter) selectedBranchInt else null)
                .onSuccess { grades = it }
            selectedGrade = null
            selectedSection = null
        }

        LaunchedEffect(selectedGrade, selectedBranchId, showBranchFilter) {
            classRepository.getSections(
                grade = selectedGrade,
                branchId = if (showBranchFilter) selectedBranchInt else null
            ).onSuccess { sections = it }
            if (selectedGrade == null) selectedSection = null
        }

        LaunchedEffect(
            academicYearFilter.isReady,
            academicYearFilter.selectedYearId,
            searchQuery,
            selectedBranchId,
            selectedGrade,
            selectedSection
        ) {
            if (!academicYearFilter.isReady || academicYearFilter.selectedYearId == null) return@LaunchedEffect
            if (searchQuery.length >= 2 || searchQuery.isEmpty()) {
                isLoading = true
                students = emptyList()
                if (searchQuery.isNotEmpty()) delay(500)
                val yearId = academicYearFilter.selectedYearId
                academicYearFilter.selectedYear?.let { year ->
                    SessionManager.setAcademicYear(year.id, year.name)
                }
                repository.getStudents(
                    query = searchQuery.ifBlank { null },
                    grade = selectedGrade,
                    section = selectedSection,
                    academicYearId = yearId,
                    branchId = if (showBranchFilter) selectedBranchInt else null
                ).onSuccess { students = it }
                isLoading = false
            }
        }

        val hasActiveFilters = academicYearFilter.selectedYearId != null ||
            selectedBranchId != null ||
            selectedGrade != null ||
            selectedSection != null

        Scaffold(
            topBar = {
                AppBackTopBar(title = "Students", onBack = { navigator.pop() })
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search by name, roll no...") },
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

                if (academicYearFilter.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AppLoader(size = 56.dp, message = "Loading years")
                    }
                } else if (academicYearFilter.error != null) {
                    Text(
                        text = academicYearFilter.error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
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
                                val current = academicYearFilter.academicYears.find { it.is_current }
                                    ?: academicYearFilter.academicYears.firstOrNull()
                                current?.let { academicYearFilter.onYearSelected(it.id) }
                                selectedBranchId = null
                                selectedGrade = null
                                selectedSection = null
                            }
                        )
                    }
                }

                if (isLoading) {
                    AppLoaderFullscreen(message = "Loading students")
                } else if (students.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if (searchQuery.isNotEmpty() || hasActiveFilters)
                                "No students found with current filters"
                            else "No students available",
                            color = SecondaryText
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(students) { student ->
                            StudentCard(student) {
                                navigator.push(Student360Screen(student))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentCard(student: Student, onClick: () -> Unit) {
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
                Text(student.full_name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${student.displayGradeLabel ?: student.displayGrade ?: "N/A"} - ${student.displaySection ?: "N/A"}",
                    fontSize = 13.sp,
                    color = SecondaryText
                )
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "Roll: ${student.roll_number ?: "N/A"}",
                        fontSize = 11.sp,
                        color = SecondaryText.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Adm: ${student.admission_number}",
                        fontSize = 11.sp,
                        color = SecondaryText.copy(alpha = 0.7f)
                    )
                }
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
