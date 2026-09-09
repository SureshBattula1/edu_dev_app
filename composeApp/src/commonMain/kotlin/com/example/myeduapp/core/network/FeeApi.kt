package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.FeeDue
import com.example.myeduapp.data.model.FeePayment
import com.example.myeduapp.data.model.ApiResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class FeeApi {
    private val client = ApiClient.client

    suspend fun getStudentDues(token: String, studentId: Int): List<FeeDue> {
        val response: ApiResponse<List<FeeDue>> = client.get("${ApiConfig.FEE_DUES}/$studentId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
        return response.data ?: emptyList()
    }

    suspend fun getStudentPayments(token: String, studentId: Int): List<FeePayment> {
        val response: ApiResponse<List<FeePayment>> = client.get("${ApiConfig.FEE_PAYMENTS}/$studentId/fees") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
        return response.data ?: emptyList()
    }
}
