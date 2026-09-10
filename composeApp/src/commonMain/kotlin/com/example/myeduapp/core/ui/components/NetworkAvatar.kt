package com.example.myeduapp.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun NetworkAvatar(
    url: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    placeholder: @Composable () -> Unit = {}
)
