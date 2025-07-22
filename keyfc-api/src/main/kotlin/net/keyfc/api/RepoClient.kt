package net.keyfc.api

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.keyfc.api.ext.addRawCookies
import java.net.HttpCookie

internal class RepoClient {

    companion object {
        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
    }

    private val httpClient by lazy {
        HttpClient(CIO) {
            install(DefaultRequest) {
                header("User-Agent", USER_AGENT)

            }
        }
    }

    private val context = Dispatchers.IO

    fun close() {
        httpClient.close()
    }

    suspend fun parseUrl(url: String, cookies: List<HttpCookie> = emptyList()): Document {
        val response = getResponse(url, cookies)

        // IO is heavy, so we need to run it in a separate thread
        return withContext(context) {
            Ksoup.parse(response.bodyAsText())
        }
    }

    suspend fun parseHtml(html: String): Document {
        return withContext(context) {
            Ksoup.parse(html)
        }
    }

    suspend inline fun postFormData(
        url: String,
        formDataMap: Map<String, String>,
        cookies: List<HttpCookie> = emptyList(),
    ): HttpResponse {
        val response = httpClient.post(url) {
            setBody(FormDataContent(Parameters.build {
                formDataMap.forEach { (key, value) ->
                    append(key, value)
                }
            }))

            addRawCookies(cookies)
        }

        return response
    }

    private suspend fun getResponse(url: String, cookies: List<HttpCookie> = emptyList()): HttpResponse {
        val response = httpClient.get(url) {
            addRawCookies(cookies)
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