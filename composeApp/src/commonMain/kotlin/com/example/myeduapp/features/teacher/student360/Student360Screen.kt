package com.example.myeduapp.features.teacher.student360

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.ui.theme.*
import com.example.myeduapp.data.model.*
import com.example.myeduapp.data.repository.AttendanceRepository
import com.example.myeduapp.data.repository.ExamRepository
import com.example.myeduapp.data.repository.FeeRepository
import com.example.myeduapp.data.repository.StudentRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

@OptIn(ExperimentalMaterial3Api::class)
class Student360Screen(private val student: Student? = null) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val colorScheme = MaterialTheme.colorScheme
        val sessionUser = SessionManager.user
        val isSelfView = student == null

        var resolvedStudent by remember(student, sessionUser) { mutableStateOf(student) }
        var resolvingStudent by remember { mutableStateOf(student == null) }

        val studentRepo = remember { StudentRepository() }

        LaunchedEffect(student, sessionUser) {
            if (student != null) {
                resolvedStudent = student
                resolvingStudent = false
                return@LaunchedEffect
            }
            val user = sessionUser
            if (user == null) {
                resolvingStudent = false
                return@LaunchedEffect
            }
            resolvingStudent = true
            studentRepo.resolveStudentForUser(user)
                .onSuccess { resolvedStudent = it }
                .onFailure { resolvedStudent = user.toStudentSeed() }
            resolvingStudent = false
        }

        val currentStudent = resolvedStudent
        if (currentStudent == null) {
            if (resolvingStudent) {
                Student360Loading()
            } else {
                Student360EmptyTab(
                    title = "Profile unavailable",
                    message = "Could not load your student profile. Please sign in again."
                )
            }
            return
        }

        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("Profile", "Attendance", "Fees", "Exams", "Leaves")

        val attendanceRepo = remember { AttendanceRepository() }
        val feeRepo = remember { FeeRepository() }
        val examRepo = remember { ExamRepository() }

        var isLoading by remember { mutableStateOf(true) }
        var detail by remember { mutableStateOf<StudentDetail?>(null) }
        var loadError by remember { mutableStateOf<String?>(null) }
        var attendanceOverview by remember { mutableStateOf<AttendanceOverview?>(null) }
        var attendanceRecords by remember { mutableStateOf<List<Attendance>>(emptyList()) }
        var feeDues by remember { mutableStateOf<List<FeeDue>>(emptyList()) }
        var feePayments by remember { mutableStateOf<List<FeePayment>>(emptyList()) }
        var feeSummary by remember { mutableStateOf(FeeSummary()) }
        var examResults by remember { mutableStateOf<List<ExamResult>>(emptyList()) }

        LaunchedEffect(currentStudent.id) {
            isLoading = true
            loadError = null
            coroutineScope {
                val detailResult = async { studentRepo.getStudentDetail(currentStudent.id) }.await()
                detailResult
                    .onSuccess { detail = it }
                    .onFailure { loadError = it.message }

                val mergedStudent = currentStudent.mergeWithDetail(detailResult.getOrNull())
                val userIdInt = resolveStudentUserId(mergedStudent, detailResult.getOrNull())

                val attendanceJob = async {
                    userIdInt?.let { attendanceRepo.getStudentAttendance(it) } ?: Result.success(emptyList())
                }
                val overviewJob = async {
                    userIdInt?.let { attendanceRepo.getStudentOverview(it) } ?: Result.failure(Exception("no user"))
                }
                val feesJob = async {
                    userIdInt?.let { feeRepo.getStudentFees(it) } ?: Result.success(StudentFeesData())
                }
                val examsJob = async {
                    userIdInt?.let { examRepo.getStudentResults(it) } ?: Result.success(emptyList())
                }

                attendanceJob.await().onSuccess { attendanceRecords = it }
                overviewJob.await().onSuccess { attendanceOverview = it }
                feesJob.await().onSuccess { fees ->
                    feeDues = fees.dues
                    feePayments = fees.payments
                    feeSummary = fees.summary
                }
                examsJob.await().onSuccess { examResults = it }
            }
            isLoading = false
        }

        val displayStudent = currentStudent.mergeWithDetail(detail)
        val studentUserId = resolveStudentUserId(displayStudent, detail)
        val screenTitle = if (isSelfView) "My Profile" else "Student 360"

        Scaffold(
            containerColor = colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text(screenTitle, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorScheme.primary,
                        titleContentColor = colorScheme.onPrimary,
                        navigationIconContentColor = colorScheme.onPrimary
                    )
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                Student360Hero(student = displayStudent, detail = detail)

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = colorScheme.surface,
                    contentColor = colorScheme.primary,
                    edgePadding = Student360Dimens.ScreenPadding,
                    divider = { HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.4f)) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorScheme.background)
                ) {
                    when {
                        isLoading -> Student360Loading()
                        selectedTab == 0 -> Student360ProfileTab(
                            detail = detail,
                            student = displayStudent,
                            loadError = loadError
                        )
                        selectedTab == 1 -> Student360AttendanceTab(
                            overview = attendanceOverview,
                            records = attendanceRecords
                        )
                        selectedTab == 2 -> Student360FeesTab(
                            dues = feeDues,
                            payments = feePayments,
                            summary = feeSummary
                        )
                        selectedTab == 3 -> Student360ExamsTab(results = examResults)
                        selectedTab == 4 -> Student360LeavesTab(
                            userId = studentUserId,
                            isSelfView = isSelfView,
                            branchId = sessionUser?.branch_id ?: detail?.branch_id?.toIntOrNull()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Student360ProfileTab(
    detail: StudentDetail?,
    student: Student,
    loadError: String?
) {
    val colorScheme = MaterialTheme.colorScheme
    val profile = detail ?: student.toDetailFallback()

    LazyColumn(
        contentPadding = PaddingValues(Student360Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Student360Dimens.SectionSpacing)
    ) {
        if (detail == null && loadError != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.errorContainer.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Full profile could not be loaded. Showing available data. $loadError",
                            fontSize = 12.sp,
                            color = colorScheme.onSurface
                        )
                    }
                }
            }
        }
        items(profile.profileSections()) { section ->
            Student360SectionCard(section = section)
        }
    }
}

@Composable
private fun Student360AttendanceTab(
    overview: AttendanceOverview?,
    records: List<Attendance>
) {
    Student360AttendanceCalendar(
        records = records,
        overview = overview
    )
}

@Composable
private fun Student360FeesTab(
    dues: List<FeeDue>,
    payments: List<FeePayment>,
    summary: FeeSummary
) {
    val colorScheme = MaterialTheme.colorScheme
    when {
        dues.isEmpty() && payments.isEmpty() -> Student360EmptyTab(
            title = "No fee records",
            message = "Fee and payment information is not available for this student."
        )
        else -> LazyColumn(
            contentPadding = PaddingValues(Student360Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Student360Dimens.SectionSpacing)
        ) {
            item {
                Student360FeeSummaryCard(summary = summary)
            }
            if (dues.isNotEmpty()) {
                item {
                    Text(
                        "Pending Dues",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colorScheme.primary
                    )
                }
                items(dues) { due ->
                    Student360FeeDueCard(due)
                }
            }
            if (payments.isNotEmpty()) {
                item {
                    Text(
                        "Payment History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colorScheme.primary
                    )
                }
                items(payments) { payment ->
                    Student360FeePaymentCard(payment)
                }
            }
        }
    }
}

@Composable
private fun Student360ExamsTab(results: List<ExamResult>) {
    when {
        results.isEmpty() -> Student360EmptyTab(
            title = "No exam results",
            message = "Exam marks are not available for this student yet."
        )
        else -> LazyColumn(
            contentPadding = PaddingValues(Student360Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(results) { result ->
                Student360ExamResultCard(result)
            }
        }
    }
}

@Composable
private fun Student360ExamResultCard(result: ExamResult) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Student360Dimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(result.subject, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                result.displayDate?.let {
                    Text(it, fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                }
                result.remarks?.takeIf { it.isNotBlank() }?.let {
                    Text(it, fontSize = 12.sp, color = colorScheme.onSurfaceVariant, maxLines = 2)
                }
            }
            Surface(
                color = colorScheme.primaryContainer.copy(alpha = 0.6f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${result.displayMarks.toInt()}%",
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                    Text(
                        result.grade,
                        fontSize = 11.sp,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun Student360FeeSummaryCard(summary: FeeSummary) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Student360Dimens.CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Fee Summary",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Student360FeeStatChip(
                    label = "Total",
                    value = formatFeeAmount(summary.displayTotal),
                    color = colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Student360FeeStatChip(
                    label = "Paid",
                    value = formatFeeAmount(summary.totalPaid),
                    color = SuccessColor,
                    modifier = Modifier.weight(1f)
                )
                Student360FeeStatChip(
                    label = "Remaining",
                    value = formatFeeAmount(summary.totalRemaining),
                    color = if (summary.totalRemaining > 0.0) WarningColor else SuccessColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun Student360FeeStatChip(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = color,
                maxLines = 1
            )
            Text(
                label,
                fontSize = 11.sp,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatFeeAmount(amount: Double): String = "₹${amount.toLong()}"

@Composable
private fun Student360FeeDueCard(due: FeeDue) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Student360Dimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(due.fee_type, fontWeight = FontWeight.SemiBold)
                Text("Due: ${due.displayDueDate}", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                Text(due.status, fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
            }
            Text("₹${due.displayAmount.toInt()}", fontWeight = FontWeight.Bold, color = WarningColor)
        }
    }
}

@Composable
private fun Student360FeePaymentCard(payment: FeePayment) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Student360Dimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(payment.payment_method, fontWeight = FontWeight.SemiBold)
                Text(payment.displayPaymentDate, fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                payment.receipt_number?.let {
                    Text("Receipt: $it", fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
                }
            }
            Text("₹${payment.amount_paid.toInt()}", fontWeight = FontWeight.Bold, color = SuccessColor)
        }
    }
}
