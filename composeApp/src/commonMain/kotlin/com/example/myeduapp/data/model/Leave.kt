package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LeaveRecord(
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val id: Int = 0,
    @Serializable(with = JsonFlexibleNullableIntSerializer::class)
    val student_id: Int? = null,
    @Serializable(with = JsonFlexibleNullableIntSerializer::class)
    val teacher_id: Int? = null,
    val from_date: String = "",
    val to_date: String = "",
    @Serializable(with = JsonFlexibleNullableIntSerializer::class)
    val total_days: Int? = null,
    val leave_type: String = "",
    val status: String = "Pending",
    val reason: String = "",
    val remarks: String? = null,
    val first_name: String? = null,
    val last_name: String? = null,
    val email: String? = null,
    val admission_number: String? = null,
    val employee_id: String? = null,
    val designation: String? = null,
    val grade: String? = null,
    val grade_label: String? = null,
    val section: String? = null,
    val branch_name: String? = null,
    val academic_year_name: String? = null,
    val approved_by_name: String? = null,
    val approved_at: String? = null,
    val created_at: String? = null,
    val profile_picture: String? = null,
    val leave_for: String? = null
) {
    val applicantName: String
        get() = listOfNotNull(first_name, last_name)
            .joinToString(" ")
            .trim()
            .ifBlank { "Unknown" }

    val classLabel: String?
        get() = when {
            grade_label != null && section != null -> "$grade_label • Sec $section"
            grade_label != null -> grade_label
            grade != null && section != null -> "Grade $grade • Sec $section"
            grade != null -> "Grade $grade"
            else -> null
        }

    val subtitle: String?
        get() = when {
            admission_number != null -> "Adm. $admission_number"
            employee_id != null -> "ID $employee_id"
            designation != null -> designation
            else -> email
        }
}

@Serializable
data class LeaveSummary(
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val total_leaves: Int = 0,
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val total_days_taken: Int = 0,
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val approved: Int = 0,
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val pending: Int = 0,
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val rejected: Int = 0
)

@Serializable
data class LeaveListMeta(
    val current_page: Int = 1,
    val per_page: Int = 25,
    val total: Int = 0,
    val last_page: Int = 1,
    val has_more_pages: Boolean = false
)

@Serializable
data class LeaveListResponse(
    val success: Boolean = false,
    val data: List<LeaveRecord> = emptyList(),
    val summary: LeaveSummary? = null,
    val meta: LeaveListMeta? = null,
    val message: String? = null
)

@Serializable
data class LeaveDetailResponse(
    val success: Boolean = false,
    val data: LeaveRecord? = null,
    val message: String? = null
)

@Serializable
data class LeaveActionResponse(
    val success: Boolean = false,
    val message: String? = null
)

@Serializable
data class CreateStudentLeaveBody(
    val student_id: Int,
    val from_date: String,
    val to_date: String,
    val leave_type: String,
    val reason: String,
    val remarks: String? = null,
    val branch_id: Int? = null
)

@Serializable
data class CreateTeacherLeaveBody(
    val teacher_id: Int,
    val from_date: String,
    val to_date: String,
    val leave_type: String,
    val reason: String,
    val remarks: String? = null,
    val branch_id: Int? = null,
    val substitute_teacher_id: Int? = null
)

@Serializable
data class UpdateLeaveBody(
    val status: String? = null,
    val remarks: String? = null
)

object LeaveTypes {
    val STUDENT = listOf(
        "Sick Leave",
        "Casual Leave",
        "Medical Leave",
        "Family Emergency",
        "Other"
    )
    val TEACHER = listOf(
        "Sick Leave",
        "Casual Leave",
        "Medical Leave",
        "Maternity Leave",
        "Paternity Leave",
        "Compensatory Leave",
        "Unpaid Leave",
        "Other"
    )
}

enum class LeaveCategory(val apiType: String) {
    STUDENT("student"),
    TEACHER("teacher")
}
