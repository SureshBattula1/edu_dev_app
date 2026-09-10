package com.example.myeduapp.core.network

import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.platform.PickedDocument
import com.example.myeduapp.data.model.UploadFileResponse
import com.example.myeduapp.data.model.UploadedFile
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Clock

class UploadApi {
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(ApiClient.jsonConfig)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 120_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 120_000
        }
    }

    suspend fun uploadDocument(token: String, file: PickedDocument): UploadedFile {
        val safeName = file.name.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "file" }
        val uploadPath = "assignments/${Clock.System.now().toEpochMilliseconds()}_$safeName"
        val response: UploadFileResponse = client.post(ApiConfig.BASE_URL + ApiConfig.UPLOADS) {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, ContentType.Application.Json.toString())
            SessionManager.academicYearId?.let { header("X-Academic-Year-Id", it) }
            timeout {
                requestTimeoutMillis = 120_000
            }
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("upload_path", uploadPath)
                        append(
                            "file",
                            file.bytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, file.mimeType)
                                append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                            }
                        )
                    }
                )
            )
        }.body()
        val data = response.data
        if (!response.success || data == null) {
            throw IllegalStateException(response.message ?: "Upload failed")
        }
        return data
    }
}
