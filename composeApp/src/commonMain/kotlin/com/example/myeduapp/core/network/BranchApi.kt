package com.example.myeduapp.core.network

import com.example.myeduapp.data.model.AccessibleBranchesResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

class BranchApi {
    private val client = ApiClient.client

    suspend fun getAccessibleBranches(token: String): AccessibleBranchesResponse {
        return client.get(ApiConfig.BRANCHES_ACCESSIBLE) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }
}
