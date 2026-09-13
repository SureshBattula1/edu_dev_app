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
import com.example.myeduapp.core.datastore.AuthState
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.ui.components.AppLoaderFullscreen
import com.example.myeduapp.core.ui.components.BigBridzEmptyState
import com.example.myeduapp.core.ui.components.ClearFiltersButton
import com.example.myeduapp.core.ui.components.FilterOptionDropdown
import com.example.myeduapp.core.ui.icons.BigBridzIcon
import com.example.myeduapp.core.ui.theme.ErrorColor
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.ui.theme.SuccessColor
import com.example.myeduapp.core.ui.theme.WarningColor
import com.example.myeduapp.data.model.FeeDue
import com.example.myeduapp.data.model.FeePayment
import com.example.myeduapp.data.model.FeeSummary
import com.example.myeduapp.data.model.FilterOption
import com.example.myeduapp.data.model.GradeOption
import com.example.myeduapp.data.model.UserRole
import com.example.myeduapp.data.repository.BranchRepository
import com.example.myeduapp.data.repository.ClassRepository
import com.example.myeduapp.data.repository.FeeRepository
import com.example.myeduapp.data.repository.StudentRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

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
    val role = user.userRole

    val isStudent = role == UserRole.STUDENT
    val isSuperAdmin = role == UserRole.SUPER_ADMIN
    val isBranchAdminOrTeacher = role in listOf(UserRole.SUPER_ADMIN, UserRole.BRANCH_ADMIN, UserRole.TEACHER, UserRole.ACCOUNTANT, UserRole.STAFF)

    val feeRepository = remember { FeeRepository() }
    val studentRepository = remember { StudentRepository() }
    val branchRepository = remember { BranchRepository() }
    val classRepository = remember { ClassRepository() }

    var dues by remember { mutableStateOf<List<FeeDue>>(emptyList()) }
    var payments by remember { mutableStateOf<List<FeePayment>>(emptyList()) }
    var feeSummary by remember { mutableStateOf(FeeSummary()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }

    // Filter States
    var branchOptions by remember { mutableStateOf<List<FilterOption>>(emptyList()) }
    var grades by remember { mutableStateOf<List<GradeOption>>(emptyList()) }
    var sections by remember { mutableStateOf<List<FilterOption>>(emptyList()) }
    var selectedBranchId by remember { mutableStateOf<String?>(null) }
    var selectedGrade by remember { mutableStateOf<String?>(null) }
    var selectedSection by remember { mutableStateOf<String?>(null) }

    val gradeOptions = remember(grades) {
        grades.map { FilterOption(value = it.value, label = it.label) }
    }

    val effectiveBranchId = if (isSuperAdmin) selectedBranchId?.toIntOrNull() else user.branch_id
    val hasActiveFilters = selectedBranchId != null || selectedGrade != null || selectedSection != null

    val tabs = listOf("Dues", "History")

    // Load Branch Options for SuperAdmin
    LaunchedEffect(isSuperAdmin) {
        if (isSuperAdmin) {
            branchRepository.getBranchFilterOptions().onSuccess { branchOptions = it }
        }
    }

    // Load Grades based on effective branch
    LaunchedEffect(effectiveBranchId) {
        classRepository.getGrades(branchId = effectiveBranchId)
            .onSuccess { grades = it }
        selectedGrade = null
        selectedSection = null
    }

    // Load Sections based on selected grade and effective branch
    LaunchedEffect(selectedGrade, effectiveBranchId) {
        classRepository.getSections(grade = selectedGrade, branchId = effectiveBranchId)
            .onSuccess { sections = it }
        if (selectedGrade == null) selectedSection = null
    }

    // Fetch Fee Data dynamically based on user role and filters
    LaunchedEffect(isStudent, user.id, effectiveBranchId, selectedGrade, selectedSection) {
        isLoading = true
        if (isStudent) {
            feeRepository.getStudentFees(user.id).onSuccess { fees ->
                dues = fees.dues
                payments = fees.payments
                feeSummary = fees.summary
            }
        } else {
            // Admin / Teacher / Staff View: Fetch students matching selected branch, class, and section
            studentRepository.getStudents(
                grade = selectedGrade,
                section = selectedSection,
                branchId = effectiveBranchId
            ).onSuccess { students ->
                if (students.isEmpty()) {
                    dues = emptyList()
                    payments = emptyList()
                    feeSummary = FeeSummary()
                } else {
                    // Fetch fees for each student in parallel
                    val allStudentFees = coroutineScope {
                        students.map { student ->
                            async {
                                val sUserId = student.attendanceUserId.toIntOrNull()
                                    ?: student.user_id.toIntOrNull()
                                    ?: student.id.toIntOrNull()
                                    ?: 0
                                if (sUserId > 0) {
                                    val result = feeRepository.getStudentFees(sUserId).getOrNull()
                                    val studentClassLabel = listOfNotNull(
                                        student.displayGradeLabel ?: student.displayGrade,
                                        student.displaySection
                                    ).filter { it.isNotBlank() }.joinToString(" - ")

                                    val enrichedDues = result?.dues?.map { due ->
                                        due.copy(
                                            student_name = student.full_name,
                                            student_roll_no = student.roll_number ?: student.admission_number,
                                            student_class = studentClassLabel
                                        )
                                    }.orEmpty()

                                    val enrichedPayments = result?.payments?.map { pm ->
                                        pm.copy(
                                            student_name = student.full_name,
                                            student_roll_no = student.roll_number ?: student.admission_number,
                                            student_class = studentClassLabel
                                        )
                                    }.orEmpty()

                                    Triple(enrichedDues, enrichedPayments, result?.summary ?: FeeSummary())
                                } else {
                                    Triple(emptyList<FeeDue>(), emptyList<FeePayment>(), FeeSummary())
                                }
                            }
                        }.awaitAll()
                    }

                    val mergedDues = allStudentFees.flatMap { it.first }
                    val mergedPayments = allStudentFees.flatMap { it.second }

                    var totalOrig = 0.0
                    var totalP = 0.0
                    var totalRem = 0.0

                    allStudentFees.forEach { (_, _, summary) ->
                        totalOrig += summary.totalOriginal
                        totalP += summary.totalPaid
                        totalRem += summary.totalRemaining
                    }

                    dues = mergedDues.sortedByDescending { it.displayAmount }
                    payments = mergedPayments.sortedByDescending { it.payment_date }
                    feeSummary = FeeSummary(
                        totalOriginal = totalOrig,
                        totalPaid = totalP,
                        totalRemaining = totalRem
                    )
                }
            }.onFailure {
                dues = emptyList()
                payments = emptyList()
                feeSummary = FeeSummary()
            }
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
            // Dynamic Fee Summary Banner with PrimaryBlue theme
            FeeSummaryHeader(feeSummary)

            // Dynamic Filters for SuperAdmin, BranchAdmin, and Teachers
            if (isBranchAdminOrTeacher) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isSuperAdmin) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                            modifier = Modifier.fillMaxWidth(),
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
                    }
                }
            }

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
                AppLoaderFullscreen(message = "Loading fee data...")
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
        BigBridzEmptyState(
            icon = BigBridzIcon.Fees,
            title = "No Pending Fees",
            message = "All student fees are fully settled or no fee dues match your active filters."
        )
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
        BigBridzEmptyState(
            icon = BigBridzIcon.Payments,
            title = "No Payment History",
            message = "No fee payment records match your active filters."
        )
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
                due.student_name?.let { sName ->
                    Text(sName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = listOfNotNull(due.student_class, due.student_roll_no?.let { "Roll: $it" })
                            .joinToString(" • "),
                        fontSize = 12.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(due.fee_type, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("Due Date: ${due.displayDueDate}", fontSize = 12.sp, color = SecondaryText)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("₹${due.displayAmount.toLong()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ErrorColor)
                Spacer(modifier = Modifier.height(4.dp))
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
                payment.student_name?.let { sName ->
                    Text(sName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = listOfNotNull(payment.student_class, payment.student_roll_no?.let { "Roll: $it" })
                            .joinToString(" • "),
                        fontSize = 12.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text("Payment #${payment.receipt_number ?: payment.id}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text("${payment.displayPaymentDate} • ${payment.payment_method}", fontSize = 12.sp, color = SecondaryText)
            }

            Text(
                "₹${payment.amount_paid.toLong()}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SuccessColor
            )
        }
    }
}
