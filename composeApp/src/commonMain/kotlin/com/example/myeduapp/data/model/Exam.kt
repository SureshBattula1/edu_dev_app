package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Exam(
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val id: Int = 0,
    val name: String = "",
    val date: String = "",
    val time: String? = null,
    val type: String? = null,
    val subject: String? = null
)

@Serializable
data class ExamResult(
    @Serializable(with = JsonFlexibleIntSerializer::class)
    val id: Int = 0,
    val subject: String = "",
    val marks: Float = 0f,
    val percentage: Float? = null,
    val total_marks: Float = 100f,
    val grade: String = "",
    val remarks: String? = null,
    val date: String? = null
) {
    val displayMarks: Float get() = when {
        marks > 0f -> marks
        percentage != null -> percentage
        else -> 0f
    }

    val displayDate: String?
        get() = date?.trim()?.take(10)?.takeIf { it.isNotBlank() }
}

@Serializable
data class ExamResponse(
    val success: Boolean,
    val exams: List<Exam> = emptyList(),
    val results: List<ExamResult> = emptyList()
)
