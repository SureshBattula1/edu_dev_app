package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

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

fun DashboardStats?.attendancePercent(): Int =
    this?.attendance?.students?.percentage?.toInt() ?: 0

fun DashboardStats?.pendingFeeAmount(): Double {
    val raw = this?.fees?.get("total_pending")
    return (raw as? JsonPrimitive)
        ?.contentOrNull
        ?.toDoubleOrNull()
        ?: 0.0
}

fun DashboardStats?.pendingFeeLabel(): String = "₹${pendingFeeAmount().toLong()}"
