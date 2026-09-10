package com.example.myeduapp.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.myeduapp.core.util.MediaUrlResolver

@Composable
actual fun NetworkAvatar(
    url: String?,
    modifier: Modifier,
    contentDescription: String?,
    placeholder: @Composable () -> Unit
) {
    val resolvedUrl = MediaUrlResolver.resolve(url)
    if (resolvedUrl == null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            placeholder()
        }
        return
    }

    val context = LocalContext.current
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(resolvedUrl)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        loading = { placeholder() },
        error = { placeholder() }
    )
}
