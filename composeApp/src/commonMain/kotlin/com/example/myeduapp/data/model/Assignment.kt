package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Assignment(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String,
    val title: String,
    val description: String? = null,
    val class_name: String = "",
    val grade: String? = null,
    val section: String = "",
    val subject: String = "",
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val subject_id: String = "",
    val due_date: String = "",
    val submission_count: Int = 0,
    val recipient_count: Int = 0,
    val status: String = "Published",
    val audience_mode: String = "all",
    val max_marks: Double? = null,
    val assignment_type: String = "Homework",
    @Serializable(with = JsonFlexibleBooleanSerializer::class)
    val is_published: Boolean = true,
    val attachments: List<AssignmentAttachment> = emptyList(),
    @Serializable(with = JsonFlexibleBooleanSerializer::class)
    val can_edit: Boolean = false,
    val instructions: String? = null
)

@Serializable
data class AssignmentAttachment(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String = "",
    val file_name: String = "",
    val original_name: String? = null,
    val file_path: String = "",
    val file_url: String? = null,
    val file_type: String? = null,
    @Serializable(with = JsonFlexibleNullableIntSerializer::class)
    val file_size: Int? = null,
    val attachment_type: String = "document"
) {
    val displayName: String get() = original_name?.ifBlank { null } ?: file_name.ifBlank { "Attachment" }
}

@Serializable
data class AssignmentResponse(
    val success: Boolean,
    val data: List<Assignment> = emptyList(),
    val message: String? = null
)

@Serializable
data class AssignmentDetailResponse(
    val success: Boolean,
    val data: Assignment? = null,
    val message: String? = null
)

@Serializable
data class CreateAssignmentBody(
    val branch_id: Int? = null,
    val grade: String,
    val section: String,
    val subject_id: String,
    val title: String,
    val description: String? = null,
    val instructions: String? = null,
    val due_date: String,
    val max_marks: Double? = null,
    val assignment_type: String = "Homework",
    val audience_mode: String,
    val student_ids: List<String> = emptyList(),
    val is_published: Boolean = true,
    val attachments: List<AssignmentAttachment> = emptyList()
)

@Serializable
data class UpdateAssignmentBody(
    val title: String,
    val description: String? = null,
    val instructions: String? = null,
    val due_date: String,
    val max_marks: Double? = null,
    val assignment_type: String,
    val attachments: List<AssignmentAttachment>,
    val is_published: Boolean = true,
    val notify: Boolean = true
)

@Serializable
data class EligibleStudent(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String,
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val user_id: String = "",
    val name: String,
    val admission_number: String? = null
)

@Serializable
data class EligibleStudentsResponse(
    val success: Boolean,
    val data: List<EligibleStudent> = emptyList(),
    val message: String? = null
)

@Serializable
data class SubjectOption(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String,
    val name: String,
    val code: String? = null
)

@Serializable
data class SubjectListResponse(
    val success: Boolean,
    val data: List<SubjectOption> = emptyList(),
    val message: String? = null
)

@Serializable
data class UploadedFile(
    val file_path: String,
    val file_url: String? = null,
    val file_name: String = "",
    @Serializable(with = JsonFlexibleNullableIntSerializer::class)
    val file_size: Int? = null,
    val file_type: String? = null
)

@Serializable
data class UploadFileResponse(
    val success: Boolean,
    val data: UploadedFile? = null,
    val message: String? = null
)
