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
data class ClassAttendanceStatus(
    val grade: String? = null,
    val section: String? = null,
    val class_name: String? = null,
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val student_count: Int = 0,
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val present: Int = 0,
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val absent: Int = 0,
    @Serializable(with = JsonFlexibleBooleanSerializer::class)
    val created: Boolean = false,
    @Serializable(with = JsonFlexibleBooleanSerializer::class)
    val notify_sent: Boolean = false,
    val notify_group_key: String? = null,
    val notify_sent_at: String? = null
) {
    val displayName: String
        get() = class_name?.takeIf { it.isNotBlank() }
            ?: run {
                val g = grade.orEmpty()
                val s = section.orEmpty()
                when {
                    g.isNotEmpty() && s.isNotEmpty() -> "Grade $g - $s"
                    g.isNotEmpty() -> "Grade $g"
                    else -> s.ifBlank { "Class" }
                }
            }

    val selectionKey: String get() = "${grade.orEmpty()}|${section.orEmpty()}"
}

@Serializable
data class ClassAttendanceStatusResponse(
    val success: Boolean,
    val data: List<ClassAttendanceStatus> = emptyList(),
    val message: String? = null
)

@Serializable
data class AttendanceNotifyClass(
    val grade: String,
    val section: String
)

@Serializable
data class AttendanceNotifyRequest(
    val date: String,
    val classes: List<AttendanceNotifyClass>
)

@Serializable
data class AttendanceNotifyCampaign(
    val grade: String? = null,
    val section: String? = null,
    val group_key: String? = null,
    @Serializable(with = JsonFlexibleNullableIntSerializer::class)
    val student_count: Int? = null,
    val sent_at: String? = null
)

@Serializable
data class AttendanceNotifyResult(
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val student_count: Int = 0,
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val skipped_no_login: Int = 0,
    val campaigns: List<AttendanceNotifyCampaign> = emptyList()
)

@Serializable
data class AttendanceNotifyResponse(
    val success: Boolean,
    val data: AttendanceNotifyResult? = null,
    val message: String? = null
)

@Serializable
data class AttendanceNotifyStudent(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val user_id: String = "",
    val name: String = "",
    val roll_number: String? = null,
    val status: String? = null,
    @Serializable(with = JsonFlexibleBooleanSerializer::class)
    val sent: Boolean = false,
    val sent_at: String? = null
)

@Serializable
data class AttendanceNotifyReceipts(
    val grade: String? = null,
    val section: String? = null,
    val date: String? = null,
    val group_key: String? = null,
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val sent_count: Int = 0,
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val total: Int = 0,
    val students: List<AttendanceNotifyStudent> = emptyList()
)

@Serializable
data class AttendanceNotifyReceiptsResponse(
    val success: Boolean,
    val data: AttendanceNotifyReceipts? = null,
    val message: String? = null
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
