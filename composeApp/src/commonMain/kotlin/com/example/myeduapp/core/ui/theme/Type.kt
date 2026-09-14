package com.example.myeduapp.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ============================================================
// BigBridz Edu Typography System
// Clean • Modern • Smooth • iOS-inspired
// ============================================================
//
// Designed to visually match the typography style shown in
// the reference image:
//
// - Strong but clean headings
// - Medium-weight navigation labels
// - Compact body text
// - Tight heading letter spacing
// - Comfortable body line height
// - No decorative fonts
// ============================================================

private val BigBridzFont = FontFamily.SansSerif

val Typography = Typography(

    // ========================================================
    // DISPLAY
    // Large dashboard / hero numbers / major titles
    // ========================================================

    displayLarge = TextStyle(
        fontFamily = BigBridzFont,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp
    ),

    displayMedium = TextStyle(
        fontFamily = BigBridzFont,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.4).sp
    ),

    displaySmall = TextStyle(
        fontFamily = BigBridzFont,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.3).sp
    ),

    // ========================================================
    // HEADLINES
    // Screen titles / major section titles
    // ========================================================

    headlineLarge = TextStyle(
        fontFamily = BigBridzFont,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.4).sp
    ),

    headlineMedium = TextStyle(
        fontFamily = BigBridzFont,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp
    ),

    headlineSmall = TextStyle(
        fontFamily = BigBridzFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.15).sp
    ),

    // ========================================================
    // TITLES
    // Card titles / page subsections / feature names
    // ========================================================

    titleLarge = TextStyle(
        fontFamily = BigBridzFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.1).sp
    ),

    titleMedium = TextStyle(
        fontFamily = BigBridzFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),

    titleSmall = TextStyle(
        fontFamily = BigBridzFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),

    // ========================================================
    // BODY
    // Normal application content
    // ========================================================

    bodyLarge = TextStyle(
        fontFamily = BigBridzFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = BigBridzFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),

    bodySmall = TextStyle(
        fontFamily = BigBridzFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.05.sp
    ),

    // ========================================================
    // LABELS
    // Buttons / tabs / navigation / small UI elements
    // ========================================================

    labelLarge = TextStyle(
        fontFamily = BigBridzFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),

    labelMedium = TextStyle(
        fontFamily = BigBridzFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp
    ),

    labelSmall = TextStyle(
        fontFamily = BigBridzFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.2.sp
    )
)