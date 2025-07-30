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
import java.net.HttpCookie

internal class RepoClient : AutoCloseable {

    companion object {
        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"

        const val BASE_URL = "https://keyfc.net/bbs/" // `/` is needed

        const val ARCHIVER_URL = BASE_URL + "archiver/"
    }

    private val httpClient by lazy {
        HttpClient(CIO) {
            install(DefaultRequest) {
                header("User-Agent", USER_AGENT)
            }
        }
    }

    private val context = Dispatchers.IO

    override fun close() {
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

    suspend fun postFormData(
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

    /**
     * Fetches the response from the given URL, optionally using provided cookies.
     *
     * @param url The URL to fetch
     * @param cookies Optional list of cookies to include in the request
     *
     * @return The HTTP response
     *
     * @throws ResponseException if the response status is not 200 OK
     */
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

    /**
     * Adds cookies to HTTP request without encoding.
     */
    private fun HttpRequestBuilder.addRawCookies(cookies: List<HttpCookie>) {
        if (cookies.isNotEmpty())
            headers.append(
                HttpHeaders.Cookie,
                cookies.joinToString("; ") { "${it.name}=${it.value}" }) // to avoid encoding issues
    }
}