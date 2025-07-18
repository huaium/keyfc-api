package net.keyfc.api.ext

import java.net.URLEncoder

/**
 * Extension function to convert a Map to form URL-encoded string.
 */
fun Map<String, String>.toFormData(): String = this.entries.joinToString("&") { (key, value) ->
    "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
}