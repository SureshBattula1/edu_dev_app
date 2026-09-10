package com.example.myeduapp.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AppLoader(
    modifier: Modifier = Modifier,
    size: Dp = 92.dp,
    message: String? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Orbital3DLoader(
            modifier = Modifier.size(size),
            primary = colorScheme.primary,
            accent = colorScheme.tertiary,
            highlight = colorScheme.secondary
        )
        if (!message.isNullOrBlank()) {
            Text(
                message,
                color = colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AppLoaderCompact(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    Orbital3DLoader(
        modifier = modifier.size(size),
        primary = MaterialTheme.colorScheme.primary,
        accent = MaterialTheme.colorScheme.tertiary,
        highlight = MaterialTheme.colorScheme.secondary
    )
}

@Composable
fun AppLoaderFullscreen(
    modifier: Modifier = Modifier,
    message: String? = "Loading"
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AppLoader(size = 108.dp, message = message)
    }
}

@Composable
fun Orbital3DLoader(
    modifier: Modifier = Modifier,
    primary: Color,
    accent: Color,
    highlight: Color
) {
    val transition = rememberInfiniteTransition(label = "orbital3d")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )
    val tilt by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tilt"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val radius = this.size.minDimension * 0.32f
        val focal = radius * 2.4f
        val tiltRad = (18f + 8f * sin(tilt * PI.toFloat() / 180f)) * PI.toFloat() / 180f

        fun project(angleDeg: Float): Triple<Offset, Float, Float> {
            val a = angleDeg * PI.toFloat() / 180f
            val x = radius * cos(a)
            val y = radius * sin(a) * cos(tiltRad)
            val z = radius * sin(a) * sin(tiltRad)
            val scale = focal / (focal + z)
            val depth = ((z + radius) / (radius * 2f)).coerceIn(0f, 1f)
            return Triple(Offset(cx + x * scale, cy + y * scale), scale, depth)
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(cx, cy + radius * 0.55f),
                radius = radius * 1.35f
            ),
            radius = radius * 1.15f,
            center = Offset(cx, cy + radius * 0.48f)
        )

        rotate(degrees = rotation * 0.35f, pivot = Offset(cx, cy)) {
            drawCircle(
                color = primary.copy(alpha = 0.22f),
                radius = radius * 1.05f,
                center = Offset(cx, cy),
                style = Stroke(width = 2.4f, cap = StrokeCap.Round)
            )
        }
        rotate(degrees = -rotation * 0.55f, pivot = Offset(cx, cy)) {
            drawCircle(
                color = accent.copy(alpha = 0.18f),
                radius = radius * 0.78f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.8f, cap = StrokeCap.Round)
            )
        }

        val orbs = listOf(
            Triple(0f, primary, 0.12f),
            Triple(120f, accent, 0.10f),
            Triple(240f, highlight, 0.09f)
        ).map { (offset, color, ratio) ->
            val (pos, scale, depth) = project(rotation + offset)
            OrbDraw(pos, scale, depth, color, this.size.minDimension * ratio)
        }.sortedBy { it.depth }

        orbs.forEach { orb ->
            val r = orb.baseRadius * orb.scale * pulse
            drawCircle(
                color = Color.Black.copy(alpha = 0.10f + orb.depth * 0.12f),
                radius = r * 1.15f,
                center = orb.pos + Offset(0f, r * 0.55f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        orb.color,
                        orb.color.copy(alpha = 0.75f)
                    ),
                    center = orb.pos + Offset(-r * 0.28f, -r * 0.32f),
                    radius = r * 1.35f
                ),
                radius = r,
                center = orb.pos
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.55f),
                radius = r * 0.22f,
                center = orb.pos + Offset(-r * 0.28f, -r * 0.28f)
            )
        }

        val coreR = radius * 0.22f * pulse
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, primary, primary.copy(alpha = 0.85f)),
                center = Offset(cx - coreR * 0.25f, cy - coreR * 0.3f),
                radius = coreR * 1.4f
            ),
            radius = coreR,
            center = Offset(cx, cy)
        )
    }
}

private data class OrbDraw(
    val pos: Offset,
    val scale: Float,
    val depth: Float,
    val color: Color,
    val baseRadius: Float
)
