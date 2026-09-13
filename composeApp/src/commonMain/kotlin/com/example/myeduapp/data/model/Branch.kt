package com.example.myeduapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Serializable
data class BranchOption(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String,
    val name: String,
    val code: String? = null,
    val is_active: Boolean? = true
)

@Serializable
data class AccessibleBranchesResponse(
    val success: Boolean,
    val data: List<BranchOption> = emptyList(),
    val user_branch_id: Int? = null,
    val can_select_branch: Boolean? = null
)

@Serializable
data class Branch(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String,
    val name: String,
    val code: String? = null,
    val address: String? = null,
    val city: String? = null,
    val phone: String? = null,
    val email: String? = null,
    
    @SerialName("principal_name")
    val principal_name_field: String? = null,
    @SerialName("principal")
    val principal_field: String? = null,
    
    // Explicitly named fields to avoid JSON key conflicts
    @SerialName("students_count")
    val students_count_val: JsonElement? = null,
    @SerialName("student_count")
    val student_count_val: JsonElement? = null,
    @SerialName("total_students")
    val total_students_val: JsonElement? = null,
    @SerialName("total_students_count")
    val total_students_count_val: JsonElement? = null,
    @SerialName("students")
    val students_raw_val: JsonElement? = null,
    
    @SerialName("teachers_count")
    val teachers_count_val: JsonElement? = null,
    @SerialName("teacher_count")
    val teacher_count_val: JsonElement? = null,
    @SerialName("total_teachers")
    val total_teachers_val: JsonElement? = null,
    @SerialName("total_teachers_count")
    val total_teachers_count_val: JsonElement? = null,
    @SerialName("teachers")
    val teachers_raw_val: JsonElement? = null,
    
    @SerialName("classes_count")
    val classes_count_val: JsonElement? = null,
    @SerialName("class_count")
    val class_count_val: JsonElement? = null,
    @SerialName("total_classes")
    val total_classes_val: JsonElement? = null,
    @SerialName("classes")
    val classes_raw_val: JsonElement? = null,
    
    @Serializable(with = JsonFlexibleBooleanSerializer::class)
    val is_active: Boolean = true,
    val created_at: String? = null
) {
    val studentsCount: Int get() = 
        resolveInt(students_count_val) ?: 
        resolveInt(student_count_val) ?: 
        resolveInt(total_students_val) ?: 
        resolveInt(total_students_count_val) ?: 
        resolveInt(students_raw_val) ?: 0

    val teachersCount: Int get() = 
        resolveInt(teachers_count_val) ?: 
        resolveInt(teacher_count_val) ?: 
        resolveInt(total_teachers_val) ?: 
        resolveInt(total_teachers_count_val) ?: 
        resolveInt(teachers_raw_val) ?: 0

    val classesCount: Int get() = 
        resolveInt(classes_count_val) ?: 
        resolveInt(class_count_val) ?: 
        resolveInt(total_classes_val) ?: 
        resolveInt(classes_raw_val) ?: 0

    val principalName: String? get() = principal_name_field ?: principal_field

    private fun resolveInt(element: JsonElement?): Int? {
        if (element == null) return null
        return (element as? JsonPrimitive)?.let { prim ->
            prim.contentOrNull?.toDoubleOrNull()?.toInt()
                ?: prim.contentOrNull?.toIntOrNull()
                ?: prim.contentOrNull?.replace(",", "")?.toIntOrNull()
        }
    }
}

@Serializable
data class BranchListResponse(
    val success: Boolean,
    val data: List<Branch> = emptyList(),
    val message: String? = null
)

@Serializable
data class BranchDetailResponse(
    val success: Boolean,
    val data: Branch? = null,
    val message: String? = null
)
