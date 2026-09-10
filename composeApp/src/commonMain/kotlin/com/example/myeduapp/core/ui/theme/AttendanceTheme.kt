package com.example.myeduapp.core.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AttendanceDimens {
    val ScreenHorizontal = 16.dp
    val ScreenVertical = 8.dp
    val CardPadding = 16.dp
    val MarkRowPadding = 8.dp
    val ItemSpacing = 12.dp
    val ListSpacing = 6.dp
    val ChipSpacing = 8.dp
    val MarkChipSpacing = 3.dp
    val AvatarSize = 40.dp
    val MarkAvatarSize = 28.dp
    val ButtonHeight = 52.dp
    val MarkChipHeight = 28.dp
    val MarkRemarksHeight = 32.dp
}

fun attendanceStatusColor(status: String): Color = when (status) {
    "Present" -> SuccessColor
    "Absent" -> ErrorColor
    "Late" -> WarningColor
    "Half-Day" -> WarningColor
    "Sick Leave", "Leave" -> InfoColor
    else -> PrimaryBlue
}

fun attendanceStatusShortLabel(status: String): String = when (status) {
    "Half-Day" -> "Half-Day"
    "Sick Leave" -> "Sick Leave"
    else -> status
}

/** Compact labels for single-row status picker on mark attendance screen. */
fun attendanceStatusRowLabel(status: String): String = when (status) {
    "Present" -> "Present"
    "Absent" -> "Absent"
    "Late" -> "Late"
    "Half-Day" -> "Half"
    "Sick Leave" -> "Sick"
    "Leave" -> "Leave"
    else -> status
}

@Composable
fun AttendanceHeroBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryBlue, PrimaryLight)
                )
            )
    )
}
