package com.example.myeduapp.features.attendance

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.myeduapp.core.ui.components.BigBridz3DIconCard
import com.example.myeduapp.core.ui.icons.BigBridzIcon
import com.example.myeduapp.core.ui.theme.AttendanceDimens
import com.example.myeduapp.core.ui.theme.CardBackground
import com.example.myeduapp.core.ui.theme.ErrorColor
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.PrimaryText
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.ui.theme.SuccessColor
import com.example.myeduapp.core.ui.theme.WarningColor
import com.example.myeduapp.core.ui.theme.attendanceStatusColor
import com.example.myeduapp.core.ui.theme.attendanceStatusRowLabel
import com.example.myeduapp.core.ui.theme.attendanceStatusShortLabel
import com.example.myeduapp.core.util.DateUtils
import com.example.myeduapp.data.model.Attendance
import com.example.myeduapp.data.model.AttendanceOverview
import kotlinx.datetime.LocalDate

@Composable
fun AttendanceOverviewGrid(overview: AttendanceOverview, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AttendanceDimens.ChipSpacing)
    ) {
        OverviewChip("Present", overview.present_days.toString(), SuccessColor, Modifier.weight(1f))
        OverviewChip("Absent", overview.absent_days.toString(), ErrorColor, Modifier.weight(1f))
        OverviewChip("Late", overview.late_days.toString(), WarningColor, Modifier.weight(1f))
        OverviewChip("%", "${overview.percentage.toInt()}", PrimaryBlue, Modifier.weight(1f))
    }
}

@Composable
fun OverviewChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 20.sp)
            Text(label, fontSize = 12.sp, color = SecondaryText, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AttendanceActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BigBridz3DIconCard(
                icon = BigBridzIcon.Attendance,
                iconSize = 32.dp,
                containerSize = 52.dp
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryText)
                Text(subtitle, fontSize = 13.sp, color = SecondaryText)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SecondaryText)
        }
    }
}

@Composable
fun AttendanceRecordCard(record: Attendance, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val color = attendanceStatusColor(record.status)
    val displayDate = runCatching { DateUtils.formatLongDisplay(record.date.take(10)) }
        .getOrElse { record.date.take(10) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    displayDate,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                record.remarks?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        it,
                        fontSize = 13.sp,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                color = color.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    record.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun AttendanceCalendarView(
    records: List<Attendance>,
    overview: AttendanceOverview?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(AttendanceDimens.ScreenHorizontal)
) {
    val colorScheme = MaterialTheme.colorScheme
    val statusByDate = remember(records) {
        records.associateBy { it.date.take(10) }
    }

    var monthStart by remember {
        val today = DateUtils.currentLocalDate()
        mutableStateOf(LocalDate(today.year, today.month, 1).toString())
    }
    var selectedDate by remember { mutableStateOf<String?>(null) }

    val monthDate = remember(monthStart) { DateUtils.parse(monthStart) }
    val cells = remember(monthDate) { DateUtils.calendarCells(monthDate.year, monthDate.month) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        overview?.let { summary ->
            AttendanceInfoBanner(overview = summary)
            AttendanceSummaryRow(overview = summary)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { monthStart = DateUtils.shiftMonth(monthStart, -1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month", tint = colorScheme.primary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            DateUtils.formatMonthYear(monthDate.year, monthDate.month),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { monthStart = DateUtils.shiftMonth(monthStart, 1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next month", tint = colorScheme.primary)
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    DateUtils.dayHeaders().forEach { day ->
                        Text(
                            day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                cells.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { date ->
                            AttendanceCalendarDayCell(
                                date = date,
                                status = date?.toString()?.let { statusByDate[it]?.status },
                                isSelected = date != null && date.toString() == selectedDate,
                                isToday = date != null && DateUtils.isToday(date),
                                onClick = { if (date != null) selectedDate = date.toString() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        AttendanceStatusLegend()

        val selectedRecord = selectedDate?.let { statusByDate[it] }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Day Details",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                when {
                    selectedDate == null -> {
                        Text(
                            "Tap a date on the calendar to view attendance status.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    selectedRecord == null -> {
                        Text(
                            DateUtils.formatLongDisplay(selectedDate!!),
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "No attendance marked for this day.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        Text(
                            DateUtils.formatLongDisplay(selectedRecord.date),
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        val statusColor = attendanceStatusColor(selectedRecord.status)
                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                selectedRecord.status,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        selectedRecord.remarks?.takeIf { it.isNotBlank() }?.let { remarks ->
                            Spacer(Modifier.height(8.dp))
                            Text(
                                remarks,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceInfoBanner(overview: AttendanceOverview, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.EventAvailable,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Attendance Overview",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colorScheme.onSurface
                )
                Text(
                    buildString {
                        append("${overview.total_days} days tracked")
                        append(" • ")
                        append("${overview.percentage.toInt()}% present")
                        if (overview.leave_days > 0) {
                            append(" • ${overview.leave_days} leave")
                        }
                    },
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AttendanceSummaryRow(overview: AttendanceOverview, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AttendanceDimens.ChipSpacing)
    ) {
        AttendanceSummaryChip(
            label = "Present",
            value = overview.present_days.toString(),
            color = attendanceStatusColor("Present"),
            modifier = Modifier.weight(1f)
        )
        AttendanceSummaryChip(
            label = "Absent",
            value = overview.absent_days.toString(),
            color = attendanceStatusColor("Absent"),
            modifier = Modifier.weight(1f)
        )
        AttendanceSummaryChip(
            label = "Late",
            value = overview.late_days.toString(),
            color = attendanceStatusColor("Late"),
            modifier = Modifier.weight(1f)
        )
        AttendanceSummaryChip(
            label = "%",
            value = "${overview.percentage.toInt()}",
            color = colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AttendanceSummaryChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
            Text(label, fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AttendanceCalendarDayCell(
    date: LocalDate?,
    status: String?,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val statusColor = status?.let { attendanceStatusColor(it) }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    isSelected -> colorScheme.primary.copy(alpha = 0.18f)
                    statusColor != null -> statusColor.copy(alpha = 0.12f)
                    else -> Color.Transparent
                }
            )
            .then(
                if (isToday) Modifier.border(1.dp, colorScheme.primary, RoundedCornerShape(10.dp))
                else Modifier
            )
            .clickable(enabled = date != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    date.dayOfMonth.toString(),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) colorScheme.primary else colorScheme.onSurface
                )
                if (statusColor != null) {
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceStatusLegend() {
    val colorScheme = MaterialTheme.colorScheme
    val items = listOf("Present", "Absent", "Late", "Half-Day", "Sick Leave")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Legend",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { status ->
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(attendanceStatusColor(status))
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            status.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceStatusChip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    compact: Boolean = false
) {
    val bg by animateColorAsState(
        targetValue = if (selected) color else color.copy(alpha = 0.08f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "statusBg"
    )
    val fg by animateColorAsState(
        targetValue = if (selected) Color.White else color,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "statusFg"
    )
    val shape = RoundedCornerShape(if (compact) 8.dp else 10.dp)
    val chipHeight = if (compact) AttendanceDimens.MarkChipHeight else 36.dp
    val chipModifier = if (expanded) {
        modifier.fillMaxWidth().height(chipHeight)
    } else {
        modifier.height(chipHeight)
    }

    Surface(
        modifier = chipModifier
            .then(
                if (!selected) Modifier.border(1.dp, color.copy(alpha = 0.25f), shape) else Modifier
            )
            .clickable(onClick = onClick),
        shape = shape,
        color = bg
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                label,
                modifier = Modifier.padding(horizontal = if (compact) 2.dp else 8.dp),
                color = fg,
                fontWeight = FontWeight.SemiBold,
                fontSize = if (compact) 9.sp else 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AttendanceStatusRow(
    statuses: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AttendanceDimens.MarkChipSpacing)
    ) {
        statuses.forEach { status ->
            AttendanceStatusChip(
                label = attendanceStatusRowLabel(status),
                selected = selected == status,
                color = attendanceStatusColor(status),
                onClick = { onSelected(status) },
                expanded = true,
                compact = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AttendanceStatusGrid(
    statuses: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstRow = statuses.take(3)
    val secondRow = statuses.drop(3)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AttendanceDimens.ChipSpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AttendanceDimens.ChipSpacing)
        ) {
            firstRow.forEach { status ->
                AttendanceStatusChip(
                    label = attendanceStatusShortLabel(status),
                    selected = selected == status,
                    color = attendanceStatusColor(status),
                    onClick = { onSelected(status) },
                    expanded = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (secondRow.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AttendanceDimens.ChipSpacing)
            ) {
                secondRow.forEach { status ->
                    AttendanceStatusChip(
                        label = attendanceStatusShortLabel(status),
                        selected = selected == status,
                        color = attendanceStatusColor(status),
                        onClick = { onSelected(status) },
                        expanded = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AttendanceDimens.ScreenHorizontal, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = PrimaryBlue)
            subtitle?.let {
                Text(it, fontSize = 13.sp, color = SecondaryText, modifier = Modifier.padding(top = 2.dp))
            }
        }
        action()
    }
}

@Composable
fun EmptyAttendanceState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(36.dp), tint = PrimaryBlue)
        }
        Spacer(Modifier.height(16.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Text(message, color = SecondaryText, fontSize = 14.sp, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            Button(onClick = onAction, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                Text(actionLabel)
            }
        }
    }
}
