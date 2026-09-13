package com.example.myeduapp.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myeduapp.core.ui.icons.BigBridz3DIcon
import com.example.myeduapp.core.ui.icons.BigBridzIcon
import com.example.myeduapp.core.ui.theme.SecondaryText

/**
 * Surface container wrapper around 3D icons ensuring high contrast
 * and background softness in both Light and Dark mode.
 */
@Composable
fun BigBridz3DIconCard(
    icon: BigBridzIcon,
    modifier: Modifier = Modifier,
    iconSize: Dp = 52.dp,
    containerSize: Dp = 72.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
) {
    Surface(
        modifier = modifier.size(containerSize),
        shape = CircleShape,
        color = backgroundColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            BigBridz3DIcon(
                icon = icon,
                size = iconSize
            )
        }
    }
}

/**
 * Premium Module Card component for Dashboards.
 */
@Composable
fun BigBridzDashboardModuleCard(
    icon: BigBridzIcon,
    title: String,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    AppCard(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                BigBridz3DIconCard(
                    icon = icon,
                    iconSize = 48.dp,
                    containerSize = 64.dp
                )

                if (badgeText != null) {
                    Surface(
                        color = colorScheme.error,
                        shape = CircleShape,
                        modifier = Modifier.offset(x = 6.dp, y = (-4).dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = colorScheme.onError,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Feature Card component for major application navigation and feature entry points.
 */
@Composable
fun BigBridzFeatureCard(
    icon: BigBridzIcon,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    AppCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BigBridz3DIconCard(
                icon = icon,
                iconSize = 44.dp,
                containerSize = 58.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText,
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = SecondaryText.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Centralized 3D Empty State Component.
 */
@Composable
fun BigBridzEmptyState(
    icon: BigBridzIcon,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BigBridz3DIcon(
            icon = icon,
            size = 96.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            textAlign = TextAlign.Center
        )

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
            ) {
                Text(actionLabel, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * Centralized 3D Success State Component.
 */
@Composable
fun BigBridzSuccessState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String = "Continue",
    onAction: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BigBridz3DIcon(
            icon = BigBridzIcon.Success,
            size = 104.dp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAction,
            modifier = Modifier.fillMaxWidth(0.7f),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
        ) {
            Text(actionLabel, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

/**
 * Centralized 3D Error State Component.
 */
@Composable
fun BigBridzErrorState(
    title: String = "Something Went Wrong",
    message: String = "Unable to load data. Please check your connection and try again.",
    icon: BigBridzIcon = BigBridzIcon.Error,
    modifier: Modifier = Modifier,
    retryLabel: String = "Retry",
    onRetry: (() -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BigBridz3DIcon(
            icon = icon,
            size = 96.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colorScheme.error,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            textAlign = TextAlign.Center
        )

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(retryLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}
