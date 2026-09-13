package com.example.myeduapp.features.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.core.ui.components.NetworkAvatar
import com.example.myeduapp.core.ui.theme.PrimaryBlue

import androidx.compose.ui.platform.LocalUriHandler
import com.example.myeduapp.core.ui.components.AppLoaderCompact

class FilePreviewScreen(
    val title: String,
    val url: String,
    val fileName: String,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val uriHandler = LocalUriHandler.current
        val isImage = listOf("jpg", "jpeg", "png", "webp", "gif").any { url.lowercase().endsWith(it) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(fileName, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { 
                                // Open in browser for download
                                runCatching { uriHandler.openUri(url) }
                            },
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
                        }
                        PlatformShare(url)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
                )
            },
            containerColor = Color.Black
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                if (isImage) {
                    NetworkAvatar(
                        url = url,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = fileName,
                        placeholder = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color.White)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Loading image...", color = Color.White, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    )
                } else {
                    PlatformFileViewer(url)
                }
            }
        }
    }
}

@Composable
expect fun PlatformFileViewer(url: String)

@Composable
expect fun PlatformShare(url: String)


