package com.example.myeduapp.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun NetworkAvatar(
    url: String?,
    modifier: Modifier,
    contentDescription: String?,
    placeholder: @Composable () -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        placeholder()
    }
}
