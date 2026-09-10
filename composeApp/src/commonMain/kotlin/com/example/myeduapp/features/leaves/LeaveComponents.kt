package com.example.myeduapp.features.leaves

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myeduapp.core.ui.components.NetworkAvatar
import com.example.myeduapp.core.ui.theme.LeaveDimens
import com.example.myeduapp.core.ui.theme.leaveStatusColor
import com.example.myeduapp.core.util.DateUtils
import com.example.myeduapp.data.model.LeaveRecord
import com.example.myeduapp.data.model.LeaveSummary

@Composable
fun LeaveSummaryRow(summary: LeaveSummary, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LeaveSummaryChip("Total", summary.total_leaves.toString(), colorScheme.primary, Modifier.weight(1f))
        LeaveSummaryChip("Pending", summary.pending.toString(), leaveStatusColor("Pending"), Modifier.weight(1f))
        LeaveSummaryChip("Approved", summary.approved.toString(), leaveStatusColor("Approved"), Modifier.weight(1f))
        LeaveSummaryChip("Days", summary.total_days_taken.toString(), colorScheme.tertiary, Modifier.weight(1f))
    }
}

@Composable
private fun LeaveSummaryChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveStatusFilterRow(
    options: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LeaveDimens.ChipSpacing)
    ) {
        options.forEach { option ->
            val isSelected = selected == option || (option == "All" && selected == null)
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(if (option == "All") null else option) },
                label = { Text(option, fontSize = 12.sp) },
                modifier = Modifier.height(34.dp)
            )
        }
    }
}

@Composable
fun LeaveRecordCard(
    leave: LeaveRecord,
    showApplicant: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val statusColor = leaveStatusColor(leave.status)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(LeaveDimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showApplicant) {
                    NetworkAvatar(
                        url = leave.profile_picture,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        contentDescription = leave.applicantName,
                        placeholder = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )
                    Spacer(Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (showApplicant) leave.applicantName else leave.leave_type,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (showApplicant) {
                        leave.subtitle?.let {
                            Text(it, fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                        }
                        leave.classLabel?.let {
                            Text(it, fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(
                        "${formatLeaveDate(leave.from_date)} → ${formatLeaveDate(leave.to_date)}",
                        fontSize = 13.sp,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                LeaveStatusBadge(leave.status)
            }

            if (!showApplicant) {
                Spacer(Modifier.height(8.dp))
                Text(leave.leave_type, fontSize = 12.sp, color = colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.25f))
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Reason", fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
                    Text(
                        leave.reason,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                leave.total_days?.let { days ->
                    Surface(
                        color = statusColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "$days day${if (days == 1) "" else "s"}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            leave.remarks?.takeIf { it.isNotBlank() }?.let { remarks ->
                Spacer(Modifier.height(8.dp))
                Text("Remarks: $remarks", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
            }

            if (leave.status == "Approved" && !leave.approved_by_name.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Approved by ${leave.approved_by_name}",
                    fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }

            actions?.let { actionContent ->
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    content = actionContent
                )
            }
        }
    }
}

@Composable
fun LeaveStatusBadge(status: String) {
    val color = leaveStatusColor(status)
    Surface(color = color.copy(alpha = 0.14f), shape = RoundedCornerShape(50)) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun LeaveHubActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(LeaveDimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primaryContainer.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colorScheme.onSurface)
                Text(subtitle, fontSize = 13.sp, color = colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LeaveEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.EventBusy, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Spacer(Modifier.height(6.dp))
        Text(message, color = colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}

private fun formatLeaveDate(date: String): String =
    runCatching { DateUtils.formatLongDisplay(date.take(10)) }.getOrElse { date.take(10) }
