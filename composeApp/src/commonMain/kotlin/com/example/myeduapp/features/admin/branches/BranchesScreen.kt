package com.example.myeduapp.features.admin.branches

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
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
import com.example.myeduapp.core.ui.components.AppLoaderFullscreen
import com.example.myeduapp.core.ui.components.BigBridz3DIconCard
import com.example.myeduapp.core.ui.components.BigBridzEmptyState
import com.example.myeduapp.core.ui.components.BigBridzErrorState
import com.example.myeduapp.core.ui.icons.BigBridzIcon
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.data.model.Branch
import com.example.myeduapp.data.repository.BranchRepository

class BranchesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        BranchesContent(
            onBack = { navigator.pop() },
            onBranchClick = { branch -> navigator.push(BranchDetailScreen(branch.id)) }
        )
    }
}

@Composable
fun BranchesContent(
    onBack: () -> Unit,
    onBranchClick: (Branch) -> Unit
) {
    val repository = remember { BranchRepository() }
    var branches by remember { mutableStateOf<List<Branch>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        repository.getAllBranches()
            .onSuccess {
                branches = it
                error = null
            }
            .onFailure {
                error = it.message
            }
        isLoading = false
    }

    Scaffold(
        topBar = {
            AppBackTopBar(title = "Branches", onBack = onBack)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (isLoading) {
            AppLoaderFullscreen(message = "Loading branches...")
        } else if (error != null) {
            BigBridzErrorState(
                title = "Error Loading Branches",
                message = error ?: "Unknown error occurred.",
                onRetry = {
                    isLoading = true
                    // reload trigger
                }
            )
        } else if (branches.isEmpty()) {
            BigBridzEmptyState(
                icon = BigBridzIcon.Branches,
                title = "No Branches Found",
                message = "No branch campus records are available."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(branches, key = { it.id }) { branch ->
                    BranchListItem(branch = branch, onClick = { onBranchClick(branch) })
                }
            }
        }
    }
}

@Composable
fun BranchListItem(branch: Branch, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BigBridz3DIconCard(
                icon = BigBridzIcon.Branches,
                iconSize = 36.dp,
                containerSize = 48.dp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = branch.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (branch.code != null) {
                    Text(
                        text = "Code: ${branch.code}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = SecondaryText)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = branch.city ?: "N/A",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(14.dp), tint = PrimaryBlue)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (branch.studentsCount > 0) branch.studentsCount.toString() else "—",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
                Text("Students", style = MaterialTheme.typography.labelSmall, color = SecondaryText, fontSize = 9.sp)
                
                // Show if there are teachers too, to verify data flow
                if (branch.teachersCount > 0) {
                    Text(
                        text = "${branch.teachersCount} Teachers",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981),
                        fontSize = 9.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(8.dp))
            
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = SecondaryText)
        }
    }
}
