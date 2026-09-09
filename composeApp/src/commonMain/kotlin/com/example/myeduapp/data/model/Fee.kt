package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FeeDue(
    val id: Int,
    val fee_type: String,
    val amount: Double,
    val due_date: String,
    val status: String // Pending, Paid, Partially Paid
)

@Serializable
data class FeePayment(
    val id: Int,
    val amount_paid: Double,
    val payment_date: String,
    val payment_method: String,
    val receipt_number: String? = null
)

@Serializable
data class FeeResponse(
    val success: Boolean,
    val dues: List<FeeDue> = emptyList(),
    val payments: List<FeePayment> = emptyList(),
    val total_pending: Double = 0.0
)
