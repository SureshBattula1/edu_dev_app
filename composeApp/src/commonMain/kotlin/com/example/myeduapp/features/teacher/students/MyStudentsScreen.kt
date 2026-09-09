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
import com.example.myeduapp.core.ui.components.AppBackTopBar
import com.example.myeduapp.core.ui.components.AppCard
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.features.teacher.student360.Student360Screen
import com.example.myeduapp.data.model.Student
import com.example.myeduapp.data.model.SchoolClass
import com.example.myeduapp.data.repository.ClassRepository
import com.example.myeduapp.data.repository.StudentRepository
import kotlinx.coroutines.delay

class MyStudentsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repository = remember { StudentRepository() }
        val classRepository = remember { ClassRepository() }
        
        var students by remember { mutableStateOf<List<Student>>(emptyList()) }
        var classes by remember { mutableStateOf<List<SchoolClass>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }
        
        var searchQuery by remember { mutableStateOf("") }
        var selectedGrade by remember { mutableStateOf<String?>(null) }
        var selectedSection by remember { mutableStateOf<String?>(null) }

        // Fetch all classes in the branch for dynamic filters
        LaunchedEffect(Unit) {
            classRepository.getBranchClasses().onSuccess {
                classes = it
            }
        }

        // Fetch students when filters change
        LaunchedEffect(searchQuery, selectedGrade, selectedSection) {
            if (searchQuery.length >= 2 || searchQuery.isEmpty()) {
                isLoading = true
                if (searchQuery.isNotEmpty()) delay(500) // Debounce
                repository.getStudents(
                    query = searchQuery.ifBlank { null },
                    grade = selectedGrade,
                    section = selectedSection
                ).onSuccess {
                    students = it
                }
                isLoading = false
            }
        }

        val availableGrades = remember(classes) {
            classes.map { it.name }.distinct().sorted()
        }
        
        val availableSections = remember(classes, selectedGrade) {
            if (selectedGrade == null) {
                classes.map { it.section }.distinct().sorted()
            } else {
                classes.filter { it.name == selectedGrade }.map { it.section }.distinct().sorted()
            }
        }

        Scaffold(
            topBar = {
                AppBackTopBar(title = "My Students", onBack = { navigator.pop() })
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Search Bar
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

                // Filters Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterDropdown(
                        label = "Class",
                        options = availableGrades,
                        selectedOption = selectedGrade,
                        onOptionSelected = { 
                            selectedGrade = it
                            selectedSection = null // Reset section when class changes
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    FilterDropdown(
                        label = "Section",
                        options = availableSections,
                        selectedOption = selectedSection,
                        onOptionSelected = { selectedSection = it },
                        modifier = Modifier.weight(1f)
                    )

                    if (selectedGrade != null || selectedSection != null) {
                        IconButton(
                            onClick = {
                                selectedGrade = null
                                selectedSection = null
                            },
                            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Clear Filters", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                } else if (students.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if (searchQuery.isNotEmpty() || selectedGrade != null || selectedSection != null) 
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption ?: "All $label",
            onValueChange = {},
            readOnly = true,
            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            DropdownMenuItem(
                text = { Text("All $label", style = MaterialTheme.typography.bodyMedium) },
                onClick = {
                    onOptionSelected(null)
                    expanded = false
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodyMedium) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
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
                    text = "${student.grade ?: "N/A"} - ${student.section ?: "N/A"}",
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
