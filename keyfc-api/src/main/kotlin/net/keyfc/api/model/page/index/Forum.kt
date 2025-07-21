package net.keyfc.api.model.page.index

import net.keyfc.api.parser.ForumParser
import java.net.HttpCookie

data class Forum(
    val name: String,
    val id: String,
    val subForums: List<Forum> = emptyList()
) {
    companion object {
        /**
         * Extracts the forum ID from a string in the format "showforum-{id}.aspx"
         *
         * @param input The input string, e.g., "showforum-19.aspx"
         *
         * @return The extracted ID as [String], or null if no match is found
         */
        fun extractId(input: String?): String? {
            if (input == null)
                return null

            // Regex to match digits between "showforum-" and ".aspx"
            val regex = Regex("showforum-(\\d+)\\.aspx")
            val matchResult = regex.find(input)

            // Return the captured group (the digits)
            return matchResult?.groupValues?.getOrNull(1)
        }
    }

    suspend fun parse(cookies: List<HttpCookie> = emptyList()) {
        ForumParser.parse(this, cookies)
    }
}