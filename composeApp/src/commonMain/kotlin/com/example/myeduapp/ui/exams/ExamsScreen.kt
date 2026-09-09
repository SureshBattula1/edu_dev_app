package com.example.myeduapp.ui.exams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.myeduapp.data.model.Exam
import com.example.myeduapp.data.model.ExamResult
import com.example.myeduapp.data.repository.ExamRepository
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.datastore.AuthState
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.ui.theme.SuccessColor
import com.example.myeduapp.core.ui.theme.InfoColor

class ExamsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        ExamsScreenContent(onBack = { navigator.pop() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreenContent(onBack: (() -> Unit)? = null) {
    val authState by SessionManager.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user ?: return
    
    val repository = remember { ExamRepository() }
    var upcomingExams by remember { mutableStateOf<List<Exam>>(emptyList()) }
    var results by remember { mutableStateOf<List<ExamResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    
    val tabs = listOf("Upcoming", "Results")

    LaunchedEffect(Unit) {
        isLoading = true
        val examsResult = repository.getUpcomingExams()
        val resultsResult = repository.getStudentResults(user.id)
        
        if (examsResult.isSuccess) upcomingExams = examsResult.getOrNull() ?: emptyList()
        if (resultsResult.isSuccess) results = resultsResult.getOrNull() ?: emptyList()
        
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exams & Results") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = PrimaryBlue
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> UpcomingExamsList(upcomingExams)
                    1 -> ResultsList(results)
                }
            }
        }
    }
}

@Composable
fun UpcomingExamsList(exams: List<Exam>) {
    if (exams.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No upcoming exams found")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(exams) { exam ->
                ExamCard(exam)
            }
        }
    }
}

@Composable
fun ResultsList(results: List<ExamResult>) {
    if (results.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No results published yet")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(results) { result ->
                ResultCard(result)
            }
        }
    }
}

@Composable
fun ExamCard(exam: Exam) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(InfoColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Event, contentDescription = null, tint = InfoColor)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(exam.subject ?: exam.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("${exam.date} • ${exam.time ?: "TBA"}", fontSize = 12.sp, color = SecondaryText)
            }
            
            exam.type?.let {
                Surface(
                    color = InfoColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        it,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = InfoColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ResultCard(result: ExamResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(result.subject, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Score: ${result.marks}/${result.total_marks}", fontSize = 14.sp, color = SecondaryText)
                result.remarks?.let {
                    Text(it, fontSize = 12.sp, color = SecondaryText)
                }
            }
            
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(SuccessColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    result.grade,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessColor
                )
            }
        }
    }
}
