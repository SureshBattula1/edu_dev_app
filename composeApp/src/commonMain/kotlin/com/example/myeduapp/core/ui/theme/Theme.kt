package com.example.myeduapp.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = DarkBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3F2FD),
    onSecondaryContainer = Color(0xFF0D47A1),
    tertiary = InfoColor,
    onTertiary = Color.White,
    background = Background,
    onBackground = PrimaryText,
    surface = CardBackground,
    onSurface = PrimaryText,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = SecondaryText,
    outline = OutlineSoft,
    outlineVariant = Color(0xFFE8EDF4),
    error = ErrorColor,
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7EC8E8),
    onPrimary = Color(0xFF00344A),
    primaryContainer = Color(0xFF087FBA),
    onPrimaryContainer = Color(0xFFD6EFFA),
    secondary = Color(0xFF90CAF9),
    onSecondary = Color(0xFF0D47A1),
    tertiary = Color(0xFF81D4FA),
    onTertiary = Color(0xFF01579B),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE8EAED),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB0B3B8),
    outline = Color(0xFF5F6368),
    outlineVariant = Color(0xFF3C4043),
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF5F1212),
    errorContainer = Color(0xFF8E0000),
    onErrorContainer = Color(0xFFFFEBEE)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun MyEduAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val density = LocalDensity.current

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes
    ) {
        CompositionLocalProvider(
            LocalDensity provides Density(
                density = density.density,
                fontScale = density.fontScale * 0.92f
            ),
            content = content
        )
    }
}
