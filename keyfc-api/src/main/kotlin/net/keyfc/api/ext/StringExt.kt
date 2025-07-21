package net.keyfc.api.ext

import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Decodes a URL-encoded string.
 *
 * @param enc The encoding to use (default: UTF-8)
 *
 * @return the decoded string
 */
internal fun String.decodeUrl(enc: String = "UTF-8"): String = URLDecoder.decode(this, enc)

/**
 * Encodes a string for URL use.
 *
 * @param enc The encoding to use (default: UTF-8)
 *
 * @return The encoded string
 */
internal fun String.encodeUrl(enc: String = "UTF-8"): String = URLEncoder.encode(this, enc)