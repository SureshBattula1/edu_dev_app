package com.example.myeduapp.core.network

import com.example.myeduapp.core.network.mock.MockApiEngine
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiClient {

    private val jsonConfig = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    val client: HttpClient = if (ApiConfig.USE_MOCKS) {
        HttpClient(MockApiEngine.create()) { installPlugins() }
    } else {
        HttpClient { installPlugins() }
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
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 15000
        }

        defaultRequest {
            url(ApiConfig.BASE_URL)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
    }
}
