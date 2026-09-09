package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DashboardResponse(
    val success: Boolean,
    val data: DashboardStats
)

@Serializable
data class DashboardStats(
    val overview: Map<String, JsonElement> = emptyMap(),
    val attendance: AttendanceStats? = null,
    val fees: Map<String, JsonElement> = emptyMap(),
    val quick_stats: Map<String, JsonElement> = emptyMap(),
    val financial: Map<String, JsonElement> = emptyMap(),
    val upcoming_events: List<DashboardEvent> = emptyList()
)

@Serializable
data class AttendanceStats(
    val students: AttendanceOverview? = null,
    val teachers: AttendanceOverview? = null
)

@Serializable
data class DashboardEvent(
    val title: String,
    val date: String,
    val type: String
)

@Serializable
data class FeatureItemModel(
    val title: String,
    val route: String,
    val iconName: String
)
