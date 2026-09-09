package com.example.myeduapp.features.attendance

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.myeduapp.core.ui.theme.AttendanceDimens
import com.example.myeduapp.core.ui.theme.CardBackground
import com.example.myeduapp.core.ui.theme.ErrorColor
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.ui.theme.SuccessColor
import com.example.myeduapp.core.ui.theme.WarningColor
import com.example.myeduapp.core.ui.theme.attendanceStatusColor
import com.example.myeduapp.core.ui.theme.attendanceStatusShortLabel
import com.example.myeduapp.data.model.Attendance
import com.example.myeduapp.data.model.AttendanceOverview

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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, fontSize = 13.sp, color = SecondaryText)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SecondaryText)
        }
    }
}

@Composable
fun AttendanceRecordCard(record: Attendance, modifier: Modifier = Modifier) {
    val color = attendanceStatusColor(record.status)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    record.student_name ?: "Record",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                record.remarks?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, fontSize = 13.sp, color = SecondaryText, maxLines = 2)
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
fun AttendanceStatusChip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false
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
    val shape = RoundedCornerShape(10.dp)
    val chipModifier = if (expanded) {
        modifier
            .fillMaxWidth()
            .height(36.dp)
    } else {
        modifier.height(36.dp)
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
                modifier = Modifier.padding(horizontal = 8.dp),
                color = fg,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
