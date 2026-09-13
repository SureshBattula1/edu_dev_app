package com.example.myeduapp.features.teacher.teachers

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.ui.components.AppCard
import com.example.myeduapp.core.ui.components.AppLoaderFullscreen
import com.example.myeduapp.core.ui.components.NetworkAvatar
import com.example.myeduapp.core.ui.theme.ErrorColor
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.ui.theme.SuccessColor
import com.example.myeduapp.core.ui.theme.WarningColor
import com.example.myeduapp.data.model.Attendance
import com.example.myeduapp.data.model.AttendanceOverview
import com.example.myeduapp.data.model.LeaveCategory
import com.example.myeduapp.data.model.LeaveRecord
import com.example.myeduapp.data.model.LeaveSummary
import com.example.myeduapp.data.model.Teacher
import com.example.myeduapp.data.model.TeacherAttachment
import com.example.myeduapp.data.repository.AttendanceRepository
import com.example.myeduapp.data.repository.LeaveRepository
import com.example.myeduapp.data.repository.TeacherRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class TeacherProfileScreen(val teacher: Teacher) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TeacherProfileScreenContent(
            teacher = teacher,
            onBack = { navigator.pop() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherProfileScreenContent(
    teacher: Teacher,
    onBack: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val repository = remember { TeacherRepository() }
    val attendanceRepo = remember { AttendanceRepository() }
    val leaveRepo = remember { LeaveRepository() }

    var teacherDetail by remember(teacher.id) { mutableStateOf(teacher) }
    var attendanceOverview by remember { mutableStateOf<AttendanceOverview?>(null) }
    var attendanceRecords by remember { mutableStateOf<List<Attendance>>(emptyList()) }
    var leaveList by remember { mutableStateOf<List<LeaveRecord>>(emptyList()) }
    var leaveSummary by remember { mutableStateOf<LeaveSummary?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Attendance", "Leaves", "Documents")

    LaunchedEffect(teacher.id) {
        isLoading = true
        coroutineScope {
            val detailJob = async { repository.getTeacherDetail(teacher.id) }
            detailJob.await().onSuccess { teacherDetail = it }

            val tId = teacherDetail.id.toIntOrNull()
                ?: teacher.id.toIntOrNull()
                ?: teacherDetail.user_id?.toIntOrNull()
                ?: 0
            val uId = teacherDetail.user_id?.toIntOrNull()
                ?: teacherDetail.user?.id?.toIntOrNull()
                ?: teacher.user_id?.toIntOrNull()
                ?: teacher.user?.id?.toIntOrNull()
                ?: tId

            if (tId > 0) {
                async { attendanceRepo.getTeacherOverview(tId) }.await()
                    .onSuccess { attendanceOverview = it }
                async { attendanceRepo.getTeacherAttendance(tId) }.await()
                    .onSuccess { attendanceRecords = it }
            }

            if (uId > 0) {
                async { leaveRepo.getMyLeaves(LeaveCategory.TEACHER, uId) }.await()
                    .onSuccess {
                        leaveList = it.data
                        leaveSummary = it.summary
                    }
            }
        }
        isLoading = false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "TEACHER PROFILE",
                        style = MaterialTheme.typography.headlineLarge,
                        color = colorScheme.onPrimary,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(colorScheme.onPrimary.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary),
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { padding ->
        if (isLoading) {
            AppLoaderFullscreen(message = "Loading teacher profile...")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    TeacherHeroHeader(teacher = teacherDetail)
                }

                item {
                    TeacherProfileTabs(
                        tabs = tabs,
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }

                item {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "TeacherTabContent"
                    ) { targetTab ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            when (targetTab) {
                                0 -> TeacherOverviewSection(teacherDetail)
                                1 -> TeacherAttendanceSection(overview = attendanceOverview, records = attendanceRecords)
                                2 -> TeacherLeavesSection(summary = leaveSummary, leaves = leaveList)
                                3 -> TeacherDocumentsSection(attachments = teacherDetail.allAttachments)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun TeacherHeroHeader(teacher: Teacher) {
    val colorScheme = MaterialTheme.colorScheme
    val avatarUrl = teacher.user?.avatar?.takeIf { it.isNotBlank() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(colorScheme.primary, colorScheme.secondary)
                )
            )
            .padding(vertical = 24.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(colorScheme.onPrimary.copy(alpha = 0.2f), CircleShape)
                    .padding(4.dp)
                    .background(colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                NetworkAvatar(
                    url = avatarUrl,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentDescription = teacher.fullName,
                    placeholder = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(52.dp),
                                tint = colorScheme.primary
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = teacher.fullName.uppercase(),
                style = MaterialTheme.typography.headlineLarge,
                color = colorScheme.onPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = teacher.designation ?: teacher.category_type ?: "Teacher",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onPrimary.copy(alpha = 0.85f),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Active Badge & Class Teacher
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isActive = teacher.is_active != false && teacher.user?.is_active != false
                Surface(
                    color = colorScheme.onPrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (isActive) "ACTIVE" else "INACTIVE",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.onPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                teacher.employee_id?.takeIf { it.isNotBlank() }?.let { empId ->
                    Surface(
                        color = colorScheme.onPrimary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "EMP ID: $empId",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = colorScheme.onPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            teacher.branchName?.let { bName ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Branch: $bName",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onPrimary.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }

            val classTeacher = listOfNotNull(
                teacher.class_teacher_of_grade,
                teacher.class_teacher_of_section
            ).joinToString(" - ")

            if (classTeacher.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Class Teacher Of: $classTeacher",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onPrimary.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun TeacherProfileTabs(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, start = 20.dp, end = 20.dp),
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.3f))
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = colorScheme.primary,
            divider = {},
            indicator = {},
            modifier = Modifier.padding(4.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Tab(
                    selected = isSelected,
                    onClick = { onTabSelected(index) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) colorScheme.primary else Color.Transparent)
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// SECTION 1: OVERVIEW
@Composable
private fun TeacherOverviewSection(teacher: Teacher) {
    val colorScheme = MaterialTheme.colorScheme

    // Contact Information
    val email = teacher.user?.email
    val phone = teacher.user?.phone ?: teacher.user?.alternate_phone
    val address = teacher.user?.address
        ?: teacher.user?.current_address
        ?: teacher.user?.permanent_address

    DetailSectionHeader("CONTACT INFORMATION")
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailItemRow(
                icon = Icons.Default.Email,
                label = "Email",
                value = email ?: "N/A"
            )
            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
            DetailItemRow(
                icon = Icons.Default.Phone,
                label = "Phone",
                value = phone ?: "N/A"
            )
            teacher.user?.emergency_contact?.takeIf { it.isNotBlank() }?.let { emerg ->
                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                DetailItemRow(
                    icon = Icons.Default.ContactPhone,
                    label = "Emergency Contact",
                    value = emerg
                )
            }
            address?.takeIf { it.isNotBlank() }?.let { addr ->
                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                DetailItemRow(
                    icon = Icons.Default.LocationOn,
                    label = "Address",
                    value = listOfNotNull(
                        addr,
                        teacher.user?.city,
                        teacher.user?.state,
                        teacher.user?.pincode
                    ).filter { it.isNotBlank() }.joinToString(", ")
                )
            }
        }
    }

    // Employment & Academics
    DetailSectionHeader("EMPLOYMENT & ACADEMICS")
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailItemRow(
                icon = Icons.Default.Work,
                label = "Designation",
                value = teacher.designation ?: "N/A"
            )
            (teacher.department ?: teacher.user?.department)?.takeIf { it.isNotBlank() }?.let { dept ->
                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                DetailItemRow(
                    icon = Icons.Default.Category,
                    label = "Department",
                    value = dept
                )
            }
            teacher.category_type?.takeIf { it.isNotBlank() }?.let { cat ->
                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                DetailItemRow(
                    icon = Icons.Default.Folder,
                    label = "Category",
                    value = cat
                )
            }
            teacher.teacher_status?.takeIf { it.isNotBlank() }?.let { status ->
                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                DetailItemRow(
                    icon = Icons.Default.AssignmentInd,
                    label = "Teacher Status",
                    value = status
                )
            }
            teacher.contract_type?.takeIf { it.isNotBlank() }?.let { contract ->
                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                DetailItemRow(
                    icon = Icons.Default.Description,
                    label = "Contract Type",
                    value = contract
                )
            }
            (teacher.qualification ?: teacher.user?.qualification)?.takeIf { it.isNotBlank() }?.let { qual ->
                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                DetailItemRow(
                    icon = Icons.Default.School,
                    label = "Qualification",
                    value = qual
                )
            }
            (teacher.experience ?: teacher.user?.experience)?.takeIf { it.isNotBlank() }?.let { exp ->
                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                DetailItemRow(
                    icon = Icons.Default.Timeline,
                    label = "Experience",
                    value = exp
                )
            }
            (teacher.specialization ?: teacher.subjects)?.takeIf { it.isNotBlank() }?.let { subjs ->
                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                DetailItemRow(
                    icon = Icons.Default.Book,
                    label = "Subjects / Specialization",
                    value = subjs
                )
            }
            (teacher.joining_date ?: teacher.user?.joining_date)?.takeIf { it.isNotBlank() }?.let { jDate ->
                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                DetailItemRow(
                    icon = Icons.Default.Event,
                    label = "Joining Date",
                    value = jDate
                )
            }
        }
    }

    // Personal Details
    val dob = teacher.user?.dob ?: teacher.user?.date_of_birth
    val gender = teacher.gender ?: teacher.user?.gender
    val bloodGroup = teacher.blood_group ?: teacher.user?.blood_group
    val religion = teacher.religion ?: teacher.user?.religion
    val marital = teacher.marital_status ?: teacher.user?.marital_status
    val fatherOrHusband = teacher.father_name
        ?: teacher.husband_name
        ?: teacher.user?.father_name
        ?: teacher.user?.husband_name

    if (dob != null || gender != null || bloodGroup != null || religion != null || marital != null || fatherOrHusband != null) {
        DetailSectionHeader("PERSONAL DETAILS")
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                dob?.takeIf { it.isNotBlank() }?.let { d ->
                    DetailItemRow(
                        icon = Icons.Default.Cake,
                        label = "Date of Birth",
                        value = d
                    )
                }
                gender?.takeIf { it.isNotBlank() }?.let { g ->
                    HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                    DetailItemRow(
                        icon = Icons.Default.Wc,
                        label = "Gender",
                        value = g.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    )
                }
                bloodGroup?.takeIf { it.isNotBlank() }?.let { bg ->
                    HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                    DetailItemRow(
                        icon = Icons.Default.Bloodtype,
                        label = "Blood Group",
                        value = bg
                    )
                }
                religion?.takeIf { it.isNotBlank() }?.let { rel ->
                    HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                    DetailItemRow(
                        icon = Icons.Default.Mosque,
                        label = "Religion",
                        value = rel
                    )
                }
                marital?.takeIf { it.isNotBlank() }?.let { m ->
                    HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                    DetailItemRow(
                        icon = Icons.Default.Favorite,
                        label = "Marital Status",
                        value = m
                    )
                }
                fatherOrHusband?.takeIf { it.isNotBlank() }?.let { fh ->
                    HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                    DetailItemRow(
                        icon = Icons.Default.FamilyRestroom,
                        label = "Father / Spouse Name",
                        value = fh
                    )
                }
            }
        }
    }

    // Financial & Bank Details
    val bankName = teacher.bank_name
    val accountTitle = teacher.account_title
    val accountNumber = teacher.bank_account_number
    val ifsc = teacher.ifsc_code
    val salary = teacher.basic_salary

    if (bankName != null || accountNumber != null || salary != null) {
        DetailSectionHeader("BANK & PAYROLL INFO")
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                bankName?.takeIf { it.isNotBlank() }?.let { b ->
                    DetailItemRow(
                        icon = Icons.Default.AccountBalance,
                        label = "Bank Name",
                        value = b
                    )
                }
                accountTitle?.takeIf { it.isNotBlank() }?.let { title ->
                    HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                    DetailItemRow(
                        icon = Icons.Default.AccountCircle,
                        label = "Account Title",
                        value = title
                    )
                }
                accountNumber?.takeIf { it.isNotBlank() }?.let { acc ->
                    HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                    DetailItemRow(
                        icon = Icons.Default.CreditCard,
                        label = "Account Number",
                        value = acc
                    )
                }
                ifsc?.takeIf { it.isNotBlank() }?.let { code ->
                    HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                    DetailItemRow(
                        icon = Icons.Default.Pin,
                        label = "IFSC Code",
                        value = code
                    )
                }
                salary?.takeIf { it.isNotBlank() }?.let { s ->
                    HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                    DetailItemRow(
                        icon = Icons.Default.AttachMoney,
                        label = "Basic Salary",
                        value = s
                    )
                }
            }
        }
    }

    // Additional Info (Dynamic Extra Fields)
    if (teacher.additionalFields.isNotEmpty()) {
        DetailSectionHeader("ADDITIONAL INFORMATION")
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                teacher.additionalFields.forEachIndexed { index, field ->
                    if (index > 0) {
                        HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                    DetailItemRow(
                        icon = Icons.Default.Info,
                        label = field.label,
                        value = field.value
                    )
                }
            }
        }
    }
}

// SECTION 2: ATTENDANCE
@Composable
private fun TeacherAttendanceSection(
    overview: AttendanceOverview?,
    records: List<Attendance>
) {
    DetailSectionHeader("ATTENDANCE OVERVIEW")

    if (overview != null) {
        val pct = overview.percentage
        val pctColor = if (pct >= 75f) SuccessColor else ErrorColor

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileStatCard(
                label = "Attendance",
                value = "${pct.toInt()}%",
                color = pctColor,
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                label = "Present",
                value = "${overview.present_days} Days",
                color = SuccessColor,
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                label = "Absent",
                value = "${overview.absent_days} Days",
                color = ErrorColor,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileStatCard(
                label = "Leaves",
                value = "${overview.leave_days} Days",
                color = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                label = "Late",
                value = "${overview.late_days} Days",
                color = WarningColor,
                modifier = Modifier.weight(1f)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    DetailSectionHeader("ATTENDANCE LOG (${records.size})")

    if (records.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No attendance records found", color = SecondaryText, fontSize = 13.sp)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            records.take(20).forEach { record ->
                AttendanceRecordRow(record)
            }
        }
    }
}

@Composable
private fun AttendanceRecordRow(record: Attendance) {
    val colorScheme = MaterialTheme.colorScheme
    val statusColor = when (record.status.lowercase()) {
        "present", "p" -> SuccessColor
        "absent", "a" -> ErrorColor
        "leave", "l" -> PrimaryBlue
        "late" -> WarningColor
        else -> SecondaryText
    }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (record.status.lowercase()) {
                        "present", "p" -> Icons.Default.CheckCircle
                        "absent", "a" -> Icons.Default.Cancel
                        "leave", "l" -> Icons.Default.EventNote
                        else -> Icons.Default.Schedule
                    },
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.date,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
                record.remarks?.takeIf { it.isNotBlank() }?.let { rem ->
                    Text(rem, fontSize = 12.sp, color = SecondaryText)
                }
            }

            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = record.status.capitalize(),
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// SECTION 3: LEAVES
@Composable
private fun TeacherLeavesSection(
    summary: LeaveSummary?,
    leaves: List<LeaveRecord>
) {
    DetailSectionHeader("LEAVE SUMMARY")

    if (summary != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileStatCard(
                label = "Total Leaves",
                value = summary.total_leaves.toString(),
                color = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                label = "Approved",
                value = summary.approved.toString(),
                color = SuccessColor,
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                label = "Pending",
                value = summary.pending.toString(),
                color = WarningColor,
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                label = "Rejected",
                value = summary.rejected.toString(),
                color = ErrorColor,
                modifier = Modifier.weight(1f)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    DetailSectionHeader("LEAVE APPLICATIONS (${leaves.size})")

    if (leaves.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No leave requests found", color = SecondaryText, fontSize = 13.sp)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            leaves.forEach { leave ->
                TeacherLeaveCard(leave)
            }
        }
    }
}

@Composable
private fun TeacherLeaveCard(leave: LeaveRecord) {
    val colorScheme = MaterialTheme.colorScheme
    val statusColor = when (leave.status.lowercase()) {
        "approved" -> SuccessColor
        "pending" -> WarningColor
        "rejected" -> ErrorColor
        else -> SecondaryText
    }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = PrimaryBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = leave.leave_type.ifBlank { "Leave" }.uppercase(),
                        color = PrimaryBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = leave.status.capitalize(),
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = "${leave.from_date}  to  ${leave.to_date}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface
            )

            if (leave.reason.isNotBlank()) {
                Text(
                    text = "Reason: ${leave.reason}",
                    fontSize = 12.sp,
                    color = SecondaryText
                )
            }

            leave.approved_by_name?.takeIf { it.isNotBlank() }?.let { appBy ->
                Text(
                    text = "Approved by: $appBy",
                    fontSize = 11.sp,
                    color = SecondaryText.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// SECTION 4: DOCUMENTS
@Composable
private fun TeacherDocumentsSection(attachments: List<TeacherAttachment>) {
    val uriHandler = LocalUriHandler.current

    DetailSectionHeader("ATTACHMENTS & DOCUMENTS (${attachments.size})")

    if (attachments.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No attachments or documents uploaded for this teacher", color = SecondaryText, fontSize = 13.sp)
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            attachments.forEach { doc ->
                TeacherAttachmentCard(
                    attachment = doc,
                    onOpen = {
                        doc.fullUrl?.let { url ->
                            try {
                                uriHandler.openUri(url)
                            } catch (_: Exception) {}
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TeacherAttachmentCard(
    attachment: TeacherAttachment,
    onOpen: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isUrlAvailable = attachment.fullUrl != null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUrlAvailable, onClick = onOpen),
        shape = RoundedCornerShape(14.dp),
        color = colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.InsertDriveFile,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attachment.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
                attachment.document_type?.takeIf { it.isNotBlank() }?.let { docType ->
                    Text(
                        text = docType.uppercase(),
                        fontSize = 11.sp,
                        color = SecondaryText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (isUrlAvailable) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryBlue.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.OpenInNew,
                        contentDescription = "View",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "View",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = SecondaryText,
        letterSpacing = 0.8.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun ProfileStatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 10.sp, color = SecondaryText)
        }
    }
}

@Composable
private fun DetailItemRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = SecondaryText)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
