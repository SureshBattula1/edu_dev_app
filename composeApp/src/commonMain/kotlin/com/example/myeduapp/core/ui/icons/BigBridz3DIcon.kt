package com.example.myeduapp.core.ui.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

/**
 * Reusable Central 3D Icon Component for BigBridz.
 *
 * IMPORTANT DESIGN RULE:
 * Preserves original 3D artwork, perspective, lighting, and gradients by default.
 * Do NOT pass a tint by default as it destroys 3D depth and shadow rendering.
 */
@Composable
fun BigBridz3DIcon(
    icon: BigBridzIcon,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    contentDescription: String? = icon.defaultDescription,
    alpha: Float = 1.0f,
    tint: Color? = null
) {
    Image(
        painter = painterResource(icon.resource),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit,
        alpha = alpha,
        colorFilter = tint?.let { ColorFilter.tint(it) }
    )
}
