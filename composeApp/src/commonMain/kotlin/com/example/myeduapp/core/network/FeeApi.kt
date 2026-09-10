package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.FeeDue
import com.example.myeduapp.data.model.FeePayment
import com.example.myeduapp.data.model.FeeSummary
import com.example.myeduapp.data.model.StudentFeesBundle
import com.example.myeduapp.data.model.StudentFeesData
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.*

class FeeApi {
    private val client = ApiClient.client
    private val json = ApiClient.jsonConfig

    /**
     * @param studentUserId users.id (backend treats route param as user_id)
     */
    suspend fun getStudentFeesData(token: String, studentUserId: Int): StudentFeesData {
        val duesBody = client.get("${ApiConfig.FEE_DUES}/$studentUserId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.bodyAsText()
        val duesResult = parseDuesResponse(duesBody)

        val bundleBody = client.get("${ApiConfig.FEE_PAYMENTS}/$studentUserId/fees") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.bodyAsText()
        val bundle = parseFeesBundle(bundleBody)

        val mergedDues = mergeDues(duesResult.dues, bundle.pendingFees)
        val summary = buildSummary(mergedDues, bundle, duesResult.summary)

        return StudentFeesData(
            dues = mergedDues,
            payments = bundle.payments,
            summary = summary
        )
    }

    suspend fun getStudentDues(token: String, studentUserId: Int): List<FeeDue> =
        getStudentFeesData(token, studentUserId).dues

    suspend fun getStudentFeesBundle(token: String, studentUserId: Int): StudentFeesBundle {
        val body = client.get("${ApiConfig.FEE_PAYMENTS}/$studentUserId/fees") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.bodyAsText()
        return parseFeesBundle(body)
    }

    private data class DuesParseResult(
        val dues: List<FeeDue> = emptyList(),
        val summary: FeeSummary = FeeSummary()
    )

    private fun parseDuesResponse(body: String): DuesParseResult {
        return try {
            val root = json.parseToJsonElement(body).jsonObject
            if (root["success"]?.jsonPrimitive?.booleanOrNull == false) return DuesParseResult()
            val data = root["data"] ?: return DuesParseResult()
            when (data) {
                is JsonArray -> DuesParseResult(decodeDueList(data))
                is JsonObject -> {
                    val dues = data["dues"]?.let { decodeDueList(it) }
                        ?: data["pending_fees"]?.let { decodeDueList(it) }
                        ?: emptyList()
                    val summaryObj = data["summary"]?.jsonObject
                    val totalRemaining = summaryObj?.double("total_balance")
                        ?: data.double("total_balance")
                        ?: dues.sumOf { it.displayAmount }
                    val totalOriginal = dues.sumOf { due ->
                        due.original_amount ?: due.amount.takeIf { it > 0.0 } ?: 0.0
                    }
                    val totalPaid = dues.sumOf { it.paid_amount ?: 0.0 }
                    DuesParseResult(
                        dues = dues,
                        summary = FeeSummary(
                            totalOriginal = totalOriginal,
                            totalPaid = totalPaid,
                            totalRemaining = totalRemaining
                        )
                    )
                }
                else -> DuesParseResult()
            }
        } catch (_: Exception) {
            DuesParseResult()
        }
    }

    private fun mergeDues(primary: List<FeeDue>, pendingFromFees: List<FeeDue>): List<FeeDue> {
        if (primary.isEmpty()) return pendingFromFees
        if (pendingFromFees.isEmpty()) return primary
        val seen = primary.map { "${it.fee_type}|${it.displayDueDate}" }.toMutableSet()
        val extra = pendingFromFees.filter { due ->
            "${due.fee_type}|${due.displayDueDate}" !in seen
        }
        return primary + extra
    }

    private fun buildSummary(
        dues: List<FeeDue>,
        bundle: StudentFeesBundle,
        duesSummary: FeeSummary
    ): FeeSummary {
        val totalRemaining = dues.sumOf { it.displayAmount }
            .takeIf { it > 0.0 }
            ?: duesSummary.totalRemaining
        val totalOriginal = dues.sumOf { due ->
            due.original_amount ?: due.amount.takeIf { it > 0.0 } ?: 0.0
        }.takeIf { it > 0.0 } ?: duesSummary.totalOriginal

        val paidFromDues = dues.sumOf { it.paid_amount ?: 0.0 }
        val paidFromPayments = bundle.payments.sumOf { it.amount_paid }
        val totalPaid = when {
            bundle.totalPaid > 0.0 -> bundle.totalPaid
            paidFromDues > 0.0 -> paidFromDues
            paidFromPayments > 0.0 -> paidFromPayments
            else -> duesSummary.totalPaid
        }.coerceAtLeast(0.0)

        return FeeSummary(
            totalOriginal = totalOriginal.coerceAtLeast(totalPaid + totalRemaining),
            totalPaid = totalPaid,
            totalRemaining = totalRemaining
        )
    }

    private fun parseFeesBundle(body: String): StudentFeesBundle {
        return try {
            val root = json.parseToJsonElement(body).jsonObject
            if (root["success"]?.jsonPrimitive?.booleanOrNull == false) {
                return StudentFeesBundle()
            }
            val data = root["data"]
            when (data) {
                is JsonArray -> StudentFeesBundle(payments = decodePaymentList(data))
                is JsonObject -> StudentFeesBundle(
                    payments = data["payments"]?.let { decodePaymentList(it) } ?: emptyList(),
                    pendingFees = data["pending_fees"]?.let { decodeDueList(it) } ?: emptyList(),
                    totalPaid = data["total_paid"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
                )
                else -> StudentFeesBundle()
            }
        } catch (_: Exception) {
            StudentFeesBundle()
        }
    }

    private fun decodeDueList(element: JsonElement): List<FeeDue> {
        val items = when (element) {
            is JsonArray -> element
            is JsonObject -> JsonArray(listOf(element))
            else -> return emptyList()
        }
        return items.mapNotNull { item ->
            runCatching { json.decodeFromJsonElement(FeeDue.serializer(), item) }.getOrNull()
                ?: parseDueLoose(item.jsonObject)
        }
    }

    private fun decodePaymentList(element: JsonElement): List<FeePayment> {
        val items = when (element) {
            is JsonArray -> element
            is JsonObject -> JsonArray(listOf(element))
            else -> return emptyList()
        }
        return items.mapNotNull { item ->
            runCatching { json.decodeFromJsonElement(FeePayment.serializer(), item) }.getOrNull()
                ?: parsePaymentLoose(item.jsonObject)
        }
    }

    private fun parseDueLoose(obj: JsonObject): FeeDue? {
        val feeType = obj.string("fee_type") ?: return null
        return FeeDue(
            id = obj.string("id") ?: "",
            fee_type = feeType,
            amount = obj.double("amount") ?: 0.0,
            balance_amount = obj.double("balance_amount"),
            original_amount = obj.double("original_amount"),
            paid_amount = obj.double("paid_amount"),
            remaining_amount = obj.double("remaining_amount"),
            due_date = obj.string("due_date") ?: "",
            status = obj.string("status") ?: "Pending"
        )
    }

    private fun parsePaymentLoose(obj: JsonObject): FeePayment? {
        val amount = obj.double("amount_paid") ?: return null
        return FeePayment(
            id = obj.string("id") ?: "",
            amount_paid = amount,
            payment_date = obj.string("payment_date") ?: "",
            payment_method = obj.string("payment_method") ?: "Payment",
            receipt_number = obj.string("receipt_number")
        )
    }

    private fun JsonObject.string(key: String): String? =
        this[key]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }

    private fun JsonObject.double(key: String): Double? =
        this[key]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()
}
