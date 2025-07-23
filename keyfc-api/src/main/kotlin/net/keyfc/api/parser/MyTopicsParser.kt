package net.keyfc.api.parser

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import net.keyfc.api.RepoClient
import net.keyfc.api.model.mytopics.MyTopic
import net.keyfc.api.model.mytopics.MyTopicsPage
import net.keyfc.api.model.search.User
import net.keyfc.api.result.parse.BaseParseResult
import net.keyfc.api.result.parse.MyTopicsParseResult
import java.net.HttpCookie
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Parser for "My Topics" page.
 * This parser extracts the user's topics from the My Topics page.
 */
internal object MyTopicsParser : BaseParser() {

    private const val MY_TOPICS_URL = BASE_URL + "mytopics.aspx"
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    /**
     * Retrieves and parses the My Topics page.
     *
     * @param repoClient The repository client to use for HTTP requests
     * @param cookies The cookies to include in the request
     * @return [MyTopicsParseResult] containing the user's topics if successful
     */
    suspend fun parse(
        repoClient: RepoClient,
        cookies: List<HttpCookie> = emptyList(),
    ): MyTopicsParseResult {
        try {
            val document = repoClient.parseUrl(
                url = MY_TOPICS_URL,
                cookies = cookies,
            )

            // Parse the HTML response
            return parseMyTopicsPage(document)
        } catch (e: Exception) {
            return MyTopicsParseResult.Failure(
                "Failed to retrieve my topics: ${e.message}",
                e
            )
        }
    }

    /**
     * Parse the My Topics page and extract topics.
     *
     * @param document The HTML document to parse
     * @return [MyTopicsParseResult] containing the user's topics if successfully parsed
     */
    private fun parseMyTopicsPage(document: Document): MyTopicsParseResult {
        val baseResult = super.parseBase(document)

        return when (baseResult) {
            is BaseParseResult.Success -> {
                try {
                    // Check for permission denial message
                    val errorMsgDiv = document.selectFirst("div.msg_inner.error_msg")
                    if (errorMsgDiv != null) {
                        val permissionMessage = errorMsgDiv.selectFirst("p")?.text() ?: "Permission denied"
                        return MyTopicsParseResult.PermissionDenial(permissionMessage)
                    }

                    // Extract pagination information
                    // Using .html() instead of .text() to avoid character merging
                    val pagesText = document.selectFirst("div.pages")?.html() ?: ""
                    val pageRegex = "(\\d+)/(\\d+)页".toRegex()
                    val pageMatch = pageRegex.find(pagesText)
                    val currentPage = pageMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
                    val totalPages = pageMatch?.groupValues?.get(2)?.toIntOrNull() ?: 1

                    // Extract topics
                    val topics = mutableListOf<MyTopic>()
                    val topicRows = document.select("table.datatable > tbody > tr")

                    for (row in topicRows) {
                        val topic = parseTopic(row)
                        if (topic != null) {
                            topics.add(topic)
                        }
                    }

                    val myTopicsPage = MyTopicsPage(
                        pageInfo = baseResult.pageInfo,
                        topics = topics,
                        currentPage = currentPage,
                        totalPages = totalPages
                    )

                    return MyTopicsParseResult.Success(myTopicsPage)

                } catch (e: Exception) {
                    return MyTopicsParseResult.Failure(
                        "Failed to parse my topics page: ${e.message}",
                        e
                    )
                }
            }

            is BaseParseResult.Failure -> {
                MyTopicsParseResult.Failure(baseResult.message, baseResult.exception)
            }
        }
    }

    /**
     * Parse a single topic row element.
     *
     * @param row The topic row element
     * @return [MyTopic] or null if parsing fails
     */
    private fun parseTopic(row: Element): MyTopic? {
        try {
            // Extract topic status icon
            val statusImg = row.selectFirst("td:first-child img")
            val iconSrc = statusImg?.attr("src") ?: ""
            val isHot = iconSrc.contains("hot")

            // Extract topic title and URL
            val titleElement = row.selectFirst("td.datatitle > a")
            val title = titleElement?.text()?.trim() ?: ""
            val url = titleElement?.attr("href") ?: ""
            val id = extractIdFromUrl(url)

            // Extract forum information
            val forumElement = row.selectFirst("td:nth-child(4) > a")
            val forumName = forumElement?.text()?.trim() ?: ""
            val forumUrl = forumElement?.attr("href") ?: ""
            val forumId = extractIdFromUrl(forumUrl)

            // Extract last post information
            val lastPostCell = row.selectFirst("td:last-child")

            // Extract last post date
            val dateElement = lastPostCell?.selectFirst("span.time > a")
            val dateText = dateElement?.text()?.trim() ?: ""
            val date = parseDateTime(dateText)

            // Extract last post user
            val userElement = lastPostCell?.selectFirst("a[href^=userinfo-]")
            val userName = userElement?.text()?.trim() ?: ""
            val userUrl = userElement?.attr("href") ?: ""
            val userId = extractIdFromUrl(userUrl)
            val lastPostUser = User(id = userId, name = userName)

            return MyTopic(
                id = id,
                title = title,
                url = BASE_URL + url,
                forumName = forumName,
                forumId = forumId,
                forumUrl = BASE_URL + forumUrl,
                lastPostDate = date,
                lastPostUser = lastPostUser,
                isHot = isHot
            )
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Extracts the ID from a URL like "userinfo-12345.aspx" or "showtopic-12345.aspx" or "showforum-12345.aspx"
     */
    private fun extractIdFromUrl(url: String): String {
        val regex = "-(\\d+)".toRegex()
        val match = regex.find(url)
        return match?.groupValues?.get(1) ?: ""
    }

    /**
     * Parses date and time from the format "yyyy-MM-dd HH:mm"
     */
    private fun parseDateTime(dateTimeText: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateTimeText.trim(), dateFormatter)
        } catch (_: DateTimeParseException) {
            // If standard format fails, try alternative formats or return current time
            LocalDateTime.now()
        }
    }
}