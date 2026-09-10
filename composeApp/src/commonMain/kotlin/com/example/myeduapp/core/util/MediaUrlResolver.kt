package com.example.myeduapp.core.util

import com.example.myeduapp.core.network.ApiConfig

object MediaUrlResolver {
    fun resolve(path: String?): String? {
        val raw = path
            ?.trim()
            ?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
            ?: return null

        if (raw.startsWith("http://", ignoreCase = true) || raw.startsWith("https://", ignoreCase = true)) {
            return rewriteHostForDevice(raw)
        }

        val normalized = when {
            raw.startsWith("/storage/") -> raw.removePrefix("/")
            raw.startsWith("storage/") -> raw
            raw.startsWith("/uploads/") -> "storage$raw"
            raw.startsWith("uploads/") -> "storage/$raw"
            else -> "storage/$raw"
        }

        return "${ApiConfig.SERVER_ORIGIN}/$normalized"
    }

    private fun rewriteHostForDevice(url: String): String =
        url
            .replace("http://localhost:8000", ApiConfig.SERVER_ORIGIN, ignoreCase = true)
            .replace("http://127.0.0.1:8000", ApiConfig.SERVER_ORIGIN, ignoreCase = true)
}
