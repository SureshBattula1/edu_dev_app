package com.example.myeduapp.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myeduapp.core.ui.icons.BigBridz3DIcon
import com.example.myeduapp.core.ui.icons.BigBridzIcon
import com.example.myeduapp.core.ui.theme.*

/**
 * TODAY OVERVIEW CARD (Height: 160dp, Gradient BG, 3D Indicators)
 */
@Composable
fun TodayOverviewCard(
    classesCount: Int = 4,
    attendancePercent: String = "92%",
    examsCount: Int = 1,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(BigBridzDimensions.HeightTodayOverviewCard)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(BigBridzDimensions.RadiusCard),
        color = CardBackground,
        border = BorderStroke(1.dp, OutlineSoft),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftBlue)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SuccessColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TODAY'S OVERVIEW",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TodayItem(value = "$classesCount", label = "Classes", icon = BigBridzIcon.Classes)
                Box(modifier = Modifier.width(1.dp).height(36.dp).background(OutlineSoft))
                TodayItem(value = attendancePercent, label = "Attendance", icon = BigBridzIcon.Attendance)
                Box(modifier = Modifier.width(1.dp).height(36.dp).background(OutlineSoft))
                TodayItem(value = "$examsCount", label = "Exams Today", icon = BigBridzIcon.Exams)
            }
        }
    }
}

@Composable
private fun TodayItem(value: String, label: String, icon: BigBridzIcon) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BigBridz3DIcon(icon = icon, size = 20.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = SecondaryText,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * PROMOTION / BANNER CARD (Height: 180dp, Soft Gradient, 30dp Radius)
 */
@Composable
fun PromotionBannerCard(
    title: String = "Academic Performance Report",
    subtitle: String = "Comprehensive analytics and term progress reports published.",
    buttonText: String = "View Reports",
    modifier: Modifier = Modifier,
    onButtonClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(BigBridzDimensions.HeightBannerCard),
        shape = RoundedCornerShape(BigBridzDimensions.RadiusHeroCard),
        color = CardBackground,
        border = BorderStroke(1.dp, OutlineSoft),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = PrimaryBlue
                )
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "ACADEMICS",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onButtonClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(BigBridzRadii.Button),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            BigBridz3DIcon(
                icon = BigBridzIcon.Reports,
                size = 72.dp
            )
        }
    }
}

/**
 * ANNOUNCEMENT CARD
 */
@Composable
fun AnnouncementCard(
    title: String,
    content: String,
    date: String,
    author: String? = null,
    priority: String = "Normal",
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BigBridz3DIcon(icon = BigBridzIcon.Notices, size = 28.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    color = if (priority.equals("High", ignoreCase = true)) ErrorColor.copy(alpha = 0.12f) else SoftBlue,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = priority.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (priority.equals("High", ignoreCase = true)) ErrorColor else PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = content,
                fontSize = 13.sp,
                color = SecondaryText,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (author != null) {
                    Text(text = "By $author", fontSize = 11.sp, color = MutedText)
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MutedText, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = date, fontSize = 11.sp, color = MutedText)
                }
            }
        }
    }
}
