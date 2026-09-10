package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FeeDue(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String = "",
    val fee_type: String = "",
    val amount: Double = 0.0,
    val balance_amount: Double? = null,
    val original_amount: Double? = null,
    val paid_amount: Double? = null,
    val remaining_amount: Double? = null,
    val due_date: String = "",
    val status: String = "Pending"
) {
    val displayAmount: Double
        get() = balance_amount
            ?: remaining_amount
            ?: amount.takeIf { it > 0.0 }
            ?: original_amount
            ?: 0.0

    val displayDueDate: String
        get() = due_date.trim().take(10).ifBlank { "N/A" }
}

@Serializable
data class FeePayment(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String = "",
    val amount_paid: Double = 0.0,
    val payment_date: String = "",
    val payment_method: String = "",
    val receipt_number: String? = null
) {
    val displayPaymentDate: String
        get() = payment_date.trim().take(10).ifBlank { payment_date }
}

@Serializable
data class StudentFeesBundle(
    val payments: List<FeePayment> = emptyList(),
    val pendingFees: List<FeeDue> = emptyList(),
    val totalPaid: Double = 0.0
)

data class FeeSummary(
    val totalOriginal: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalRemaining: Double = 0.0
) {
    val displayTotal: Double
        get() = totalOriginal.takeIf { it > 0.0 } ?: (totalPaid + totalRemaining)
}

data class StudentFeesData(
    val dues: List<FeeDue> = emptyList(),
    val payments: List<FeePayment> = emptyList(),
    val summary: FeeSummary = FeeSummary()
)

@Serializable
data class FeeResponse(
    val success: Boolean,
    val dues: List<FeeDue> = emptyList(),
    val payments: List<FeePayment> = emptyList(),
    val total_pending: Double = 0.0
)
