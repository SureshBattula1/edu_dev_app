package com.example.myeduapp.core.platform

import androidx.compose.runtime.Composable

data class PickedDocument(
    val name: String,
    val mimeType: String,
    val bytes: ByteArray
)

@Composable
expect fun rememberFilePickerLauncher(onPicked: (List<PickedDocument>) -> Unit): () -> Unit
