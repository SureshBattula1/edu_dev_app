package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Attendance(
    val id: Int? = null,
    val student_id: Int? = null,
    val student_name: String? = null,
    val roll_number: String? = null,
    val date: String,
    val status: String, // Present, Absent, Late, Sick Leave
    val remarks: String? = null
)

@Serializable
data class ClassAttendanceResponse(
    val success: Boolean,
    val date: String,
    val class_name: String,
    val section: String,
    val attendance: List<Attendance> = emptyList()
)

@Serializable
data class AttendanceOverview(
    val total_days: Int,
    val present_days: Int,
    val absent_days: Int,
    val late_days: Int,
    val leave_days: Int,
    val percentage: Float
)

@Serializable
data class AttendanceResponse(
    val success: Boolean,
    val data: List<Attendance> = emptyList(),
    val overview: AttendanceOverview? = null
)
