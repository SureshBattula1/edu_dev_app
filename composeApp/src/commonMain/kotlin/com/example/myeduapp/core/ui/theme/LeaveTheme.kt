package com.example.myeduapp.core.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object LeaveDimens {
    val ScreenPadding = 16.dp
    val CardRadius = 16.dp
    val ChipSpacing = 8.dp
    val SectionSpacing = 14.dp
}

fun leaveStatusColor(status: String): Color = when (status) {
    "Approved" -> SuccessColor
    "Rejected" -> ErrorColor
    "Cancelled" -> SecondaryText
    else -> WarningColor
}

@Composable
fun LeaveHeroBackground(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(colorScheme.primary, colorScheme.secondary)
                )
            )
    )
}
