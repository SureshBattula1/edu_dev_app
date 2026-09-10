package com.example.myeduapp.core.platform

import androidx.compose.runtime.Composable

@Composable
actual fun rememberFilePickerLauncher(onPicked: (List<PickedDocument>) -> Unit): () -> Unit {
    return {}
}
