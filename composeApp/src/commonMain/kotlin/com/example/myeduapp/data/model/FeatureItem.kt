package com.example.myeduapp.data.model

import androidx.compose.ui.graphics.vector.ImageVector

data class FeatureItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val iconColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified
)
