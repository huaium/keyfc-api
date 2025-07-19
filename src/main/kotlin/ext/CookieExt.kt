package net.keyfc.api.ext

import java.net.HttpCookie

/**
 * Get cookies as formatted string ready to be used in HTTP requests.
 */
fun List<HttpCookie>.toHeaderString(): String {
    if (this.isEmpty())
        throw IllegalArgumentException("Empty cookies")

    return this.joinToString("; ") { "${it.name}=${it.value}" }
}