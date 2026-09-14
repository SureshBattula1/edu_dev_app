package com.example.myeduapp.core.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object BigBridzRadii {
    val Button = 16.dp
    val Input = 16.dp
    val Card = 22.dp
    val HeroCard = 30.dp
    val Dialog = 24.dp
    val BottomSheet = 32.dp
    val HeaderBottom = 40.dp
    val Avatar = CircleShape
}

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(BigBridzRadii.Button),
    medium = RoundedCornerShape(BigBridzRadii.Card),
    large = RoundedCornerShape(BigBridzRadii.HeroCard),
    extraLarge = RoundedCornerShape(BigBridzRadii.BottomSheet)
)
