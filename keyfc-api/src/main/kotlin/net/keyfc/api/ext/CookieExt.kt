package net.keyfc.api.ext

import io.ktor.client.request.*
import io.ktor.http.*
import java.net.HttpCookie

/**
 * Get cookies as formatted string ready to be used in HTTP requests.
 */
fun List<HttpCookie>.toHeaderString(): String {
    if (this.isEmpty())
        throw IllegalArgumentException("Empty cookies")

    return this.joinToString("; ") { "${it.name}=${it.value}" }
}

/**
 * Add cookies to HTTP request without encoding.
 */
fun HttpRequestBuilder.addRawCookies(cookies: List<HttpCookie>) {
    if (cookies.isNotEmpty())
        headers.append(HttpHeaders.Cookie, cookies.toHeaderString()) // to avoid encoding issues
}