package net.keyfc.api

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpCookie

object SoupClient {
    private val context = Dispatchers.IO

    suspend fun parse(url: String, cookies: List<HttpCookie>): Document {
        val response = getResponse(url, cookies)

        // IO is heavy, so we need to run it in a separate thread
        return withContext(context) {
            Ksoup.parse(response.bodyAsText())
        }
    }

    suspend fun parse(html: String): Document {
        return withContext(context) {
            Ksoup.parse(html)
        }
    }

    private suspend fun getResponse(url: String, cookies: List<HttpCookie>): HttpResponse {
        val response = ApiApplication.httpClient.get(url) {
            cookies.forEach {
                cookie(it.name, it.value)
            }
        }

        if (response.status.value != 200) {
            throw ResponseException(
                response = response,
                cachedResponseText = "Failed to fetch page: ${response.status.description}"
            )
        }

        return response
    }
}