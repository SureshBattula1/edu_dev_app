package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Student(
    val id: Int,
    val user_id: Int,
    val first_name: String,
    val last_name: String,
    val admission_number: String,
    val roll_number: String? = null,
    val grade: String? = null,
    val section: String? = null,
    val avatar: String? = null
) {
    val full_name: String get() = "$first_name $last_name"
}

@Serializable
data class StudentResponse(
    val success: Boolean,
    val data: List<Student> = emptyList()
)
