package net.keyfc.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

actual fun createHttpClient(userAgent: String): HttpClient {
    return HttpClient {
        install(DefaultRequest) {
            header("User-Agent", userAgent)
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
//            filter { request ->
//                request.url.host.contains("keyfc.net")
//            }
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
    }
}