package com.example.myeduapp.data.model

import com.example.myeduapp.core.network.ApiConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class TeacherDetailExtraField(
    val label: String,
    val value: String
)

@Serializable
data class TeacherAttachment(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String? = null,
    val title: String? = null,
    val name: String? = null,
    val file_name: String? = null,
    val original_name: String? = null,
    val file_path: String? = null,
    val url: String? = null,
    val file_type: String? = null,
    val document_type: String? = null,
    val file_size: String? = null
) {
    val displayName: String
        get() = title?.takeIf { it.isNotBlank() }
            ?: name?.takeIf { it.isNotBlank() }
            ?: original_name?.takeIf { it.isNotBlank() }
            ?: file_name?.takeIf { it.isNotBlank() }
            ?: document_type?.takeIf { it.isNotBlank() }
            ?: "Attachment"

    val fullUrl: String?
        get() {
            val path = url?.takeIf { it.isNotBlank() }
                ?: file_path?.takeIf { it.isNotBlank() }
                ?: return null
            return if (path.startsWith("http://") || path.startsWith("https://")) {
                path
            } else {
                "${ApiConfig.SERVER_ORIGIN}/${path.removePrefix("/")}"
            }
        }
}

@Serializable
data class TeacherUserBrief(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String? = null,
    val first_name: String? = null,
    val last_name: String? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val alternate_phone: String? = null,
    val emergency_contact: String? = null,
    val avatar: String? = null,
    val address: String? = null,
    val current_address: String? = null,
    val permanent_address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    val dob: String? = null,
    val date_of_birth: String? = null,
    val gender: String? = null,
    val blood_group: String? = null,
    val religion: String? = null,
    val marital_status: String? = null,
    val father_name: String? = null,
    val husband_name: String? = null,
    val mother_name: String? = null,
    val qualification: String? = null,
    val experience: String? = null,
    val joining_date: String? = null,
    val department: String? = null,
    @Serializable(with = JsonFlexibleBooleanSerializer::class)
    val is_active: Boolean? = null
)

@Serializable
data class TeacherBranchBrief(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String? = null,
    val name: String? = null,
    val code: String? = null
)

@Serializable
data class Teacher(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String = "",
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val user_id: String? = null,
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val branch_id: String? = null,
    val employee_id: String? = null,
    val designation: String? = null,
    val department: String? = null,
    val category_type: String? = null,
    val teacher_status: String? = null,
    val contract_type: String? = null,
    val work_shift: String? = null,
    val gender: String? = null,
    val blood_group: String? = null,
    val religion: String? = null,
    val marital_status: String? = null,
    val father_name: String? = null,
    val husband_name: String? = null,
    val mother_name: String? = null,
    val national_id: String? = null,
    val aadhaar_number: String? = null,
    val pan_number: String? = null,
    val class_teacher_of_grade: String? = null,
    val class_teacher_of_section: String? = null,
    val qualification: String? = null,
    val experience: String? = null,
    val specialization: String? = null,
    val subjects: String? = null,
    val joining_date: String? = null,
    val basic_salary: String? = null,
    val bank_name: String? = null,
    val account_title: String? = null,
    val bank_account_number: String? = null,
    val ifsc_code: String? = null,
    val resume: String? = null,
    val id_proof: String? = null,
    val documents: List<TeacherAttachment> = emptyList(),
    val attachments: List<TeacherAttachment> = emptyList(),
    @Serializable(with = JsonFlexibleBooleanSerializer::class)
    val is_active: Boolean? = null,
    val user: TeacherUserBrief? = null,
    val branch: TeacherBranchBrief? = null,
    @Transient val additionalFields: List<TeacherDetailExtraField> = emptyList()
) {
    val fullName: String
        get() {
            val userFullName = user?.name?.takeIf { it.isNotBlank() }
            if (userFullName != null) return userFullName
            val first = user?.first_name.orEmpty()
            val last = user?.last_name.orEmpty()
            return "$first $last".trim().ifBlank { "Teacher #$id" }
        }

    val branchName: String?
        get() = branch?.name?.takeIf { it.isNotBlank() }

    val allAttachments: List<TeacherAttachment>
        get() {
            val combined = mutableListOf<TeacherAttachment>()
            combined.addAll(documents)
            combined.addAll(attachments)
            
            resume?.takeIf { it.isNotBlank() }?.let { path ->
                if (combined.none { it.file_path == path || it.url == path }) {
                    combined.add(
                        TeacherAttachment(
                            title = "Resume / CV",
                            file_path = path,
                            document_type = "resume"
                        )
                    )
                }
            }
            
            id_proof?.takeIf { it.isNotBlank() }?.let { path ->
                if (combined.none { it.file_path == path || it.url == path }) {
                    combined.add(
                        TeacherAttachment(
                            title = "ID Proof",
                            file_path = path,
                            document_type = "id_proof"
                        )
                    )
                }
            }

            return combined
        }
}

@Serializable
data class TeacherListMeta(
    @Serializable(with = JsonFlexibleNullableIntSerializer::class)
    val current_page: Int? = 1,
    @Serializable(with = JsonFlexibleNullableIntSerializer::class)
    val per_page: Int? = 25,
    @Serializable(with = JsonFlexibleNullableIntSerializer::class)
    val total: Int? = 0,
    @Serializable(with = JsonFlexibleNullableIntSerializer::class)
    val last_page: Int? = 1,
    @Serializable(with = JsonFlexibleBooleanSerializer::class)
    val has_more_pages: Boolean = false
)

@Serializable
data class TeacherListResponse(
    @Serializable(with = JsonFlexibleBooleanSerializer::class)
    val success: Boolean = true,
    val data: List<Teacher> = emptyList(),
    val meta: TeacherListMeta? = null,
    val message: String? = null
)

@Serializable
data class TeacherDetailResponse(
    @Serializable(with = JsonFlexibleBooleanSerializer::class)
    val success: Boolean = true,
    val data: Teacher? = null,
    val message: String? = null
)
