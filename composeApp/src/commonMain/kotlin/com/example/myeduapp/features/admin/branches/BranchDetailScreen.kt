package com.example.myeduapp.features.admin.branches

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.ui.components.AppCard
import com.example.myeduapp.core.ui.components.AppLoaderFullscreen
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.data.model.Branch
import com.example.myeduapp.data.repository.BranchRepository

class BranchDetailScreen(val branchId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        BranchDetailContent(
            branchId = branchId,
            onBack = { navigator.pop() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchDetailContent(
    branchId: String,
    onBack: () -> Unit
) {
    val repository = remember { BranchRepository() }
    var branch by remember { mutableStateOf<Branch?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(branchId) {
        repository.getBranchDetails(branchId)
            .onSuccess {
                branch = it
                error = null
            }
            .onFailure {
                error = it.message
            }
        isLoading = false
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Branch Details", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        if (isLoading) {
            AppLoaderFullscreen(message = "Loading branch details...")
        } else if (error != null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(error ?: "Unknown error", color = colorScheme.error)
            }
        } else if (branch != null) {
            val b = branch!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero Info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(PrimaryBlue, colorScheme.secondary)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                        }
                        Spacer(Modifier.width(20.dp))
                        Column {
                            Text(b.name, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                            Text("ID: ${b.code ?: b.id}", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Statistics
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatSmallCard("Students", b.studentsCount.toString(), Icons.Default.People, PrimaryBlue, Modifier.weight(1f))
                        StatSmallCard("Teachers", b.teachersCount.toString(), Icons.Default.School, Color(0xFF10B981), Modifier.weight(1f))
                        StatSmallCard("Classes", b.classesCount.toString(), Icons.Default.Class, Color(0xFFF59E0B), Modifier.weight(1f))
                    }

                    // Contact Info
                    DetailSection("Contact Information") {
                        InfoRow(Icons.Default.Person, "Principal", b.principalName ?: "N/A")
                        InfoRow(Icons.Default.Phone, "Phone", b.phone ?: "N/A")
                        InfoRow(Icons.Default.Email, "Email", b.email ?: "N/A")
                    }

                    // Location
                    DetailSection("Location") {
                        InfoRow(Icons.Default.LocationOn, "Address", b.address ?: "N/A")
                        InfoRow(Icons.Default.LocationCity, "City", b.city ?: "N/A")
                    }
                    
                    // Administrative
                    DetailSection("Administrative") {
                        InfoRow(Icons.Default.Event, "Created At", b.created_at?.take(10) ?: "N/A")
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = if (b.is_active) Color(0xFF10B981) else SecondaryText, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Status", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Text(if (b.is_active) "Active" else "Inactive", color = if (b.is_active) Color(0xFF10B981) else colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = SecondaryText,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        letterSpacing = 1.sp
    )
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SecondaryText)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun StatSmallCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = SecondaryText, fontSize = 10.sp)
        }
    }
}
