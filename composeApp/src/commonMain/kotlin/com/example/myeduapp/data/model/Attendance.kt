package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Attendance(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String? = null,
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val student_id: String? = null,
    val student_name: String? = null,
    val roll_number: String? = null,
    val date: String,
    val status: String,
    val remarks: String? = null
) {
    val listKey: String get() = "${id ?: ""}_${student_id ?: ""}_$date"
}

@Serializable
data class AttendanceOverview(
    val total_days: Int = 0,
    val present_days: Int = 0,
    val absent_days: Int = 0,
    val late_days: Int = 0,
    val leave_days: Int = 0,
    val percentage: Float = 0f
)

@Serializable
data class AttendanceSummary(
    val total_days: Int = 0,
    val present: Int = 0,
    val absent: Int = 0,
    val late: Int = 0,
    val leaves: Int = 0,
    val half_day: Int = 0,
    val percentage: Float = 0f
) {
    fun toOverview(): AttendanceOverview = AttendanceOverview(
        total_days = total_days,
        present_days = present,
        absent_days = absent,
        late_days = late,
        leave_days = leaves,
        percentage = percentage
    )
}

@Serializable
data class LaravelAttendanceRecord(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String? = null,
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val student_id: String? = null,
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val teacher_id: String? = null,
    val date: String? = null,
    val status: String? = null,
    val remarks: String? = null,
    val first_name: String? = null,
    val last_name: String? = null,
    val roll_number: String? = null,
    val admission_number: String? = null,
    val grade_level: String? = null,
    val section: String? = null
) {
    fun toAttendance(): Attendance {
        val rawDate = date ?: ""
        val normalizedDate = if (rawDate.length >= 10) rawDate.take(10) else rawDate
        return Attendance(
            id = id,
            student_id = student_id?.takeIf { it.isNotBlank() && it != "0" }
                ?: teacher_id?.takeIf { it.isNotBlank() && it != "0" },
            student_name = listOfNotNull(first_name, last_name).joinToString(" ").ifBlank { null },
            roll_number = roll_number,
            date = normalizedDate,
            status = status ?: "Unknown",
            remarks = remarks
        )
    }
}

@Serializable
data class ClassAttendanceMeta(
    val grade: String? = null,
    val section: String? = null,
    val date: String? = null,
    val total: Int = 0,
    val present: Int = 0,
    val absent: Int = 0
)

@Serializable
data class LaravelClassAttendanceResponse(
    val success: Boolean,
    val data: List<LaravelAttendanceRecord> = emptyList(),
    val meta: ClassAttendanceMeta? = null,
    val message: String? = null
)

@Serializable
data class PersonAttendanceResponse(
    val success: Boolean,
    val data: List<LaravelAttendanceRecord> = emptyList(),
    val summary: AttendanceSummary? = null,
    val message: String? = null
)

@Serializable
data class ClassAttendanceResult(
    val date: String,
    val grade: String,
    val section: String,
    val attendance: List<Attendance>,
    val meta: ClassAttendanceMeta? = null
)

@Serializable
data class BulkAttendanceItem(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String,
    val status: String,
    val grade_level: String? = null,
    val section: String? = null,
    val remarks: String? = null
)

@Serializable
data class BulkAttendanceRequest(
    val type: String,
    val date: String,
    val branch_id: Int,
    val attendance: List<BulkAttendanceItem>
)

@Serializable
data class BulkAttendanceResponse(
    val success: Boolean,
    val message: String? = null,
    val data: BulkAttendanceResultData? = null
)

@Serializable
data class BulkAttendanceResultData(
    val marked: Int = 0
)
