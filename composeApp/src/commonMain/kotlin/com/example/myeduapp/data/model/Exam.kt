package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Exam(
    val id: Int,
    val name: String,
    val date: String,
    val time: String? = null,
    val type: String? = null,
    val subject: String? = null
)

@Serializable
data class ExamResult(
    val id: Int,
    val subject: String,
    val marks: Float,
    val total_marks: Float = 100f,
    val grade: String,
    val remarks: String? = null,
    val date: String? = null
)

@Serializable
data class ExamResponse(
    val success: Boolean,
    val exams: List<Exam> = emptyList(),
    val results: List<ExamResult> = emptyList()
)
