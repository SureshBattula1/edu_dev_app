package com.example.myeduapp.features.notifications

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
actual fun PlatformFileViewer(url: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Previewing: $url", color = Color.White)
    }
}

@Composable
actual fun PlatformShare(url: String) {
    IconButton(onClick = {
        // iOS sharing logic would go here using UIActivityViewController
    }) {
        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
    }
}
