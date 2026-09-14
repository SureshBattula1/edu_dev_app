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
    primaryContainer = SecondaryBlue,
    onPrimaryContainer = PrimaryBlue,
    secondary = PrimaryLight,
    onSecondary = Color.White,
    secondaryContainer = SecondaryBlueVariant,
    onSecondaryContainer = PrimaryBlue,
    tertiary = SuccessColor,
    onTertiary = Color.White,
    background = Background,
    onBackground = PrimaryText,
    surface = CardBackground,
    onSurface = PrimaryText,
    surfaceVariant = SecondaryBlue,
    onSurfaceVariant = SecondaryText,
    outline = OutlineSoft,
    error = ErrorColor,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF00344A),
    onPrimaryContainer = Color(0xFFE8F2FF),
    secondary = Color(0xFF7EC8E8),
    onSecondary = Color(0xFF00344A),
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFE8F2FF),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF064E3B),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8F9FF),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8F9FF),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
    error = Color(0xFFF87171),
    onError = Color(0xFF7F1D1D)
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
                fontScale = density.fontScale * 0.95f // Slightly reduced for Sunrise Academic style
            ),
            content = content
        )
    }
}
