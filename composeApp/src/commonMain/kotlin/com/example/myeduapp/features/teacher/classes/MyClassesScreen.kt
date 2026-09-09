package com.example.myeduapp.features.teacher.classes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.myeduapp.data.model.SchoolClass
import com.example.myeduapp.data.repository.ClassRepository

class MyClassesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repository = remember { ClassRepository() }
        var classes by remember { mutableStateOf<List<SchoolClass>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var searchQuery by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            repository.getMyClasses().onSuccess {
                classes = it
                isLoading = false
            }.onFailure {
                isLoading = false
            }
        }

        Scaffold(
            topBar = {
                AppBackTopBar(title = "My Classes", onBack = { navigator.pop() })
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    placeholder = { Text("Search classes...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                } else {
                    val filteredClasses = classes.filter { 
                        it.name.contains(searchQuery, ignoreCase = true) || 
                        it.subject?.contains(searchQuery, ignoreCase = true) == true 
                    }

                    if (filteredClasses.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No classes found", color = SecondaryText)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredClasses) { schoolClass ->
                                ClassCard(schoolClass) {
                                    // Navigate to details
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassCard(schoolClass: SchoolClass, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = schoolClass.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                Surface(
                    color = PrimaryBlue.copy(alpha = 0.1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Section ${schoolClass.section}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = schoolClass.subject ?: "No subject assigned",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp), tint = SecondaryText)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "${schoolClass.student_count} Students", fontSize = 12.sp, color = SecondaryText)
                
                Spacer(modifier = Modifier.width(24.dp))
                
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = SecondaryText)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = schoolClass.schedule ?: "Not scheduled", fontSize = 12.sp, color = SecondaryText)
            }
            
            if (schoolClass.academic_year != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Academic Year: ${schoolClass.academic_year}",
                    fontSize = 11.sp,
                    color = SecondaryText.copy(alpha = 0.7f)
                )
            }
        }
    }
}
