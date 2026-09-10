package com.example.myeduapp.features.teacher.student360

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myeduapp.core.ui.components.AppLoaderFullscreen
import com.example.myeduapp.core.ui.components.NetworkAvatar
import com.example.myeduapp.core.ui.theme.*
import com.example.myeduapp.data.model.Attendance
import com.example.myeduapp.data.model.AttendanceOverview
import com.example.myeduapp.features.attendance.AttendanceCalendarView
import com.example.myeduapp.data.model.STUDENT360_NA
import com.example.myeduapp.data.model.Student
import com.example.myeduapp.data.model.Student360Field
import com.example.myeduapp.data.model.Student360Section
import com.example.myeduapp.data.model.StudentDetail

@Composable
fun Student360Hero(
    student: Student,
    detail: StudentDetail?,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val displayName = detail?.fullName?.takeUnless { it == STUDENT360_NA } ?: student.full_name
    val classText = detail?.classLabel ?: buildClassLabel(student)
    val admission = detail?.admission_number?.displayOrNa() ?: student.admission_number.displayOrNa()
    val roll = detail?.roll_number?.displayOrNa() ?: student.roll_number.displayOrNa()
    val status = detail?.student_status.displayOrNa()
    val avatarPath = detail?.profile_picture?.takeIf { it.isNotBlank() }
        ?: student.avatar?.takeIf { it.isNotBlank() }

    Box(modifier = modifier.fillMaxWidth()) {
        Student360HeroBackground()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Student360Dimens.ScreenPadding, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NetworkAvatar(
                    url = avatarPath,
                    modifier = Modifier
                        .size(Student360Dimens.AvatarSize)
                        .clip(CircleShape)
                        .background(colorScheme.onPrimary.copy(alpha = 0.2f)),
                    contentDescription = displayName,
                    placeholder = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        displayName,
                        color = colorScheme.onPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(classText, color = colorScheme.onPrimary.copy(alpha = 0.9f), fontSize = 14.sp)
                    Text(
                        "Roll $roll • Adm. $admission",
                        color = colorScheme.onPrimary.copy(alpha = 0.78f),
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Student360Badge(status, student360StatusColor(detail?.student_status))
                detail?.branch?.name?.let {
                    Student360Badge(it, colorScheme.secondary)
                }
                detail?.academic_year?.let {
                    Student360Badge(it, colorScheme.tertiary)
                }
            }
        }
    }
}

@Composable
private fun Student360Badge(text: String, color: Color) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        color = color.copy(alpha = 0.22f),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = colorScheme.onPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun Student360QuickStatsRow(
    attendance: String,
    avgMarks: String,
    feeDue: String,
    status: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Student360StatCard("Attendance", attendance, SuccessColor, Modifier.weight(1f))
        Student360StatCard("Avg Marks", avgMarks, InfoColor, Modifier.weight(1f))
        Student360StatCard("Fee Due", feeDue, WarningColor, Modifier.weight(1f))
        Student360StatCard("Status", status, student360StatusColor(status), Modifier.weight(1f))
    }
}

@Composable
fun Student360StatCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Student360Dimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (value == STUDENT360_NA) colorScheme.onSurfaceVariant else colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                label,
                fontSize = 11.sp,
                color = colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun Student360SectionCard(
    section: Student360Section,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Student360Dimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    sectionIcon(section.icon),
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    section.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colorScheme.primary
                )
            }
            Spacer(Modifier.height(12.dp))
            section.fields.forEachIndexed { index, field ->
                Student360InfoRow(field)
                if (index < section.fields.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = colorScheme.outline.copy(alpha = 0.35f)
                    )
                }
            }
        }
    }
}

@Composable
fun Student360InfoRow(field: Student360Field) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            field.label,
            fontSize = 11.sp,
            color = colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(2.dp))
        Text(
            field.value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (field.value == STUDENT360_NA) colorScheme.onSurfaceVariant else colorScheme.onSurface,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun Student360EmptyTab(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = colorScheme.onSurface)
        Spacer(Modifier.height(6.dp))
        Text(message, color = colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}

@Composable
fun Student360Loading(modifier: Modifier = Modifier) {
    AppLoaderFullscreen(modifier = modifier, message = "Loading profile")
}

@Composable
fun Student360AttendanceCalendar(
    records: List<Attendance>,
    overview: AttendanceOverview?,
    modifier: Modifier = Modifier
) {
    AttendanceCalendarView(
        records = records,
        overview = overview,
        modifier = modifier,
        contentPadding = PaddingValues(Student360Dimens.ScreenPadding)
    )
}

private fun sectionIcon(key: String): ImageVector = when (key) {
    "school" -> Icons.Default.School
    "person" -> Icons.Default.Person
    "contact" -> Icons.Default.ContactMail
    "family" -> Icons.Default.FamilyRestroom
    "guardian" -> Icons.Default.SupervisorAccount
    "emergency" -> Icons.Default.Emergency
    "history" -> Icons.Default.History
    "medical" -> Icons.Default.MedicalServices
    "star" -> Icons.Default.Star
    else -> Icons.Default.Info
}

private fun buildClassLabel(student: Student): String {
    val gradeLabel = student.displayGradeLabel?.takeIf { it.isNotBlank() }
    val grade = student.displayGrade?.takeIf { it.isNotBlank() }
    val section = student.displaySection?.takeIf { it.isNotBlank() }
    return when {
        gradeLabel != null && section != null -> "$gradeLabel • Section $section"
        gradeLabel != null -> gradeLabel
        grade != null && section != null -> "Grade $grade • Section $section"
        grade != null -> "Grade $grade"
        section != null -> "Section $section"
        else -> STUDENT360_NA
    }
}

private fun String?.displayOrNa(): String =
    this?.trim()?.takeIf { it.isNotBlank() } ?: STUDENT360_NA
