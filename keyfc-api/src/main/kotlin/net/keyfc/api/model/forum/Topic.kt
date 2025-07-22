package net.keyfc.api.model.forum

data class Topic(
    val title: String,
    val id: String,
    val replyCount: Int?
) {
    companion object {
        /**
         * Extracts the topic ID from a string in the format "showtopic-{id}.aspx"
         *
         * @param input The input string, e.g., "showtopic-19.aspx"
         *
         * @return The extracted ID as [String], or null if no match is found
         */
        fun extractId(input: String?): String? {
            if (input == null)
                return null

            // Regex to match digits between "showtopic-" and ".aspx"
            val regex = Regex("showtopic-(\\d+)\\.aspx")
            val matchResult = regex.find(input)

            // Return the captured group (the digits)
            return matchResult?.groupValues?.getOrNull(1)
        }
    }
}