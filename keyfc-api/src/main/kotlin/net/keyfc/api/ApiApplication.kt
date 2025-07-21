package net.keyfc.api

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*

object ApiApplication {
    const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"

    val httpClient = HttpClient(CIO) {
        install(DefaultRequest) {
            header("User-Agent", USER_AGENT)
        }
    }
}