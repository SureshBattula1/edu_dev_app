package com.example.myeduapp.features.auth.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.delay
import myeduapp.composeapp.generated.resources.Res
import myeduapp.composeapp.generated.resources.splash_brand
import org.jetbrains.compose.resources.painterResource

/** Splash only ≈ 2 seconds, then zoom-out. */
private const val SPLASH_MS = 1600L
private const val ZOOM_MS = 400

class SplashScreen(
    private val onFinished: (() -> Unit)? = null
) : Screen {
    @Composable
    override fun Content() {
        SplashContent(onFinished = onFinished ?: {})
    }
}

@Composable
fun SplashContent(onFinished: () -> Unit) {
    var exiting by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (exiting) 1.4f else 1f,
        animationSpec = tween(durationMillis = ZOOM_MS, easing = FastOutSlowInEasing),
        label = "splashZoom"
    )
    val alpha by animateFloatAsState(
        targetValue = if (exiting) 0f else 1f,
        animationSpec = tween(durationMillis = ZOOM_MS, easing = FastOutSlowInEasing),
        label = "splashFade"
    )

    LaunchedEffect(Unit) {
        delay(SPLASH_MS)
        exiting = true
        delay(ZOOM_MS.toLong())
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.splash_brand),
            contentDescription = "BigBridz",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .alpha(alpha),
            contentScale = ContentScale.Fit
        )
    }
}
