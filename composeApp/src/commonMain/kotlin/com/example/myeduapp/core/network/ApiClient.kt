package com.example.myeduapp.core.network

import com.example.myeduapp.core.datastore.SessionManager
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiClient {

    val jsonConfig = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    val client: HttpClient = HttpClient {
        installPlugins()
    }

    private fun HttpClientConfig<*>.installPlugins() {
        install(ContentNegotiation) {
            json(jsonConfig)
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30000 // Increased for real backend
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 30000
        }

        defaultRequest {
            url(ApiConfig.BASE_URL)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            SessionManager.academicYearId?.let { yearId ->
                header("X-Academic-Year-Id", yearId)
            }
        }
    }
}
