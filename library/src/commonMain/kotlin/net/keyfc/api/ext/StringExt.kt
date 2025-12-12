package net.keyfc.api.ext

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.DateTimeFormat

/**
 * Parses the ID from a string in the format like `showtopic-{id}.aspx`
 *
 * @return The extracted ID as [String], or an empty string if no ID is found.
 */
internal fun String.parseId(): String {
    // Regex to match digits between "-" and ".aspx"
    val regex = Regex("-(\\d+)\\.aspx")
    val matchResult = regex.find(this)

    // Return the captured group (the digits)
    return matchResult?.groupValues?.getOrNull(1) ?: ""
}

/**
 * Parses date and time using the specified formatter.
 *
 * If parsing fails, it returns null.
 */
internal fun String.parseDateTime(formatter: DateTimeFormat<LocalDateTime>): LocalDateTime? {
    return runCatching {
        LocalDateTime.parse(this, formatter)
    }.getOrNull()
}