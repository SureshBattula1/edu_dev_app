package com.example.myeduapp.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: ImageVector? = null,
    onNavigationClick: () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(title, fontSize = 18.sp)
                if (subtitle != null) {
                    Text(subtitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        },
        navigationIcon = {
            if (navigationIcon != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(navigationIcon, contentDescription = null)
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBackTopBar(
    title: String,
    onBack: () -> Unit
) {
    AppTopBar(
        title = title,
        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
        onNavigationClick = onBack
    )
}
