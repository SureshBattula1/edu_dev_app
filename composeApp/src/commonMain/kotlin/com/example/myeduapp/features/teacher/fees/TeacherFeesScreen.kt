package com.example.myeduapp.features.teacher.fees

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
import com.example.myeduapp.data.model.FeeDue
import com.example.myeduapp.data.model.FeePayment
import com.example.myeduapp.data.model.FeeSummary
import com.example.myeduapp.data.repository.FeeRepository
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.datastore.AuthState
import com.example.myeduapp.core.ui.components.AppLoaderFullscreen
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.ui.theme.SuccessColor
import com.example.myeduapp.core.ui.theme.ErrorColor
import com.example.myeduapp.core.ui.theme.WarningColor

class TeacherFeesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TeacherFeesScreenContent(onBack = { navigator.pop() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherFeesScreenContent(onBack: (() -> Unit)? = null) {
    val authState by SessionManager.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user ?: return
    
    val repository = remember { FeeRepository() }
    var dues by remember { mutableStateOf<List<FeeDue>>(emptyList()) }
    var payments by remember { mutableStateOf<List<FeePayment>>(emptyList()) }
    var feeSummary by remember { mutableStateOf(FeeSummary()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    
    val tabs = listOf("Dues", "History")

    LaunchedEffect(Unit) {
        isLoading = true
        repository.getStudentFees(user.id).onSuccess { fees ->
            dues = fees.dues
            payments = fees.payments
            feeSummary = fees.summary
        }
        
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fees & Payments") },
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
            FeeSummaryHeader(feeSummary)
            
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
                AppLoaderFullscreen(message = "Loading fees")
            } else {
                when (selectedTab) {
                    0 -> DuesList(dues)
                    1 -> PaymentsList(payments)
                }
            }
        }
    }
}

@Composable
fun FeeSummaryHeader(summary: FeeSummary) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryBlue)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Fee Summary", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FeeSummaryStat(label = "Total", value = summary.displayTotal)
                FeeSummaryStat(label = "Paid", value = summary.totalPaid)
                FeeSummaryStat(label = "Remaining", value = summary.totalRemaining)
            }
        }
    }
}

@Composable
private fun FeeSummaryStat(label: String, value: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "₹${value.toLong()}",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

@Composable
fun DuesList(dues: List<FeeDue>) {
    if (dues.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No pending dues")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dues) { due ->
                DueCard(due)
            }
        }
    }
}

@Composable
fun PaymentsList(payments: List<FeePayment>) {
    if (payments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No payment history found")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(payments) { payment ->
                PaymentCard(payment)
            }
        }
    }
}

@Composable
fun DueCard(due: FeeDue) {
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
                Text(due.fee_type, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Due Date: ${due.displayDueDate}", fontSize = 12.sp, color = SecondaryText)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("₹${due.displayAmount.format(2)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ErrorColor)
                Surface(
                    color = WarningColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        due.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        color = WarningColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentCard(payment: FeePayment) {
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
                modifier = Modifier.size(40.dp).clip(CircleShape).background(SuccessColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = SuccessColor)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text("Payment #${payment.receipt_number ?: payment.id}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(payment.payment_date, fontSize = 12.sp, color = SecondaryText)
                Text(payment.payment_method, fontSize = 12.sp, color = SecondaryText)
            }
            
            Text(
                "$${payment.amount_paid.format(2)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SuccessColor
            )
        }
    }
}

// Simple extension for formatting in KMP
fun Double.format(digits: Int): String {
    val multiplier = 10.0.pow(digits)
    val rounded = (this * multiplier).toInt() / multiplier
    return rounded.toString()
}

fun Double.pow(n: Int): Double {
    var res = 1.0
    repeat(n) { res *= this }
    return res
}
