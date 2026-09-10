package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.AssignmentDetailResponse
import com.example.myeduapp.data.model.AssignmentResponse
import com.example.myeduapp.data.model.CreateAssignmentBody
import com.example.myeduapp.data.model.EligibleStudentsResponse
import com.example.myeduapp.data.model.SubjectListResponse
import com.example.myeduapp.data.model.UpdateAssignmentBody
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders

class AssignmentApi {
    private val client = ApiClient.client

    suspend fun getAssignments(token: String): AssignmentResponse {
        return client.get(ApiConfig.ASSIGNMENTS) {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("per_page", 50)
        }.body()
    }

    suspend fun createAssignment(token: String, body: CreateAssignmentBody): AssignmentDetailResponse {
        return client.post(ApiConfig.ASSIGNMENTS) {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(body)
        }.body()
    }

    suspend fun getAssignment(token: String, id: String): AssignmentDetailResponse {
        return client.get("${ApiConfig.ASSIGNMENTS}/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun updateAssignment(token: String, id: String, body: UpdateAssignmentBody): AssignmentDetailResponse {
        return client.put("${ApiConfig.ASSIGNMENTS}/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(body)
        }.body()
    }

    suspend fun getEligibleStudents(
        token: String,
        grade: String,
        section: String,
        branchId: Int? = null
    ): EligibleStudentsResponse {
        return client.get("${ApiConfig.ASSIGNMENTS}/eligible-students") {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("grade", grade)
            parameter("section", section)
            branchId?.let { parameter("branch_id", it) }
        }.body()
    }

    suspend fun getSubjects(token: String, grade: String, branchId: Int? = null): SubjectListResponse {
        return client.get(ApiConfig.SUBJECTS) {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("grade_level", grade)
            parameter("per_page", 100)
            branchId?.let { parameter("branch_id", it) }
        }.body()
    }
}
