package com.example.myeduapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myeduapp.data.model.UserRole
import com.example.myeduapp.core.security.AuthorizationManager
import com.example.myeduapp.core.ui.theme.SecondaryText

@Composable
fun AuthorizationWrapper(
    requiredPermission: String? = null,
    requiredRoles: List<UserRole> = emptyList(),
    content: @Composable () -> Unit
) {
    val isAuthorized = when {
        requiredPermission != null -> AuthorizationManager.hasPermission(requiredPermission)
        requiredRoles.isNotEmpty() -> requiredRoles.any { AuthorizationManager.hasRole(it) }
        else -> true
    }

    if (isAuthorized) {
        content()
    } else {
        UnauthorizedScreen()
    }
}

@Composable
fun UnauthorizedScreen() {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Access Denied",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "You do not have the required permissions to view this screen. Please contact your administrator if you believe this is an error.",
                fontSize = 16.sp,
                color = SecondaryText,
                textAlign = TextAlign.Center
            )
        }
    }
}
