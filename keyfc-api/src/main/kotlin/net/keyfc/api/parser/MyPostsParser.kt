package net.keyfc.api.parser

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import net.keyfc.api.RepoClient
import net.keyfc.api.model.myposts.MyPost
import net.keyfc.api.model.myposts.MyPostsPage
import net.keyfc.api.model.search.User
import net.keyfc.api.result.parse.BaseParseResult
import net.keyfc.api.result.parse.MyPostsParseResult
import java.net.HttpCookie
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Parser for "My Posts" page.
 * This parser extracts the user's posts from the My Posts page.
 */
internal object MyPostsParser : BaseParser() {

    private const val MY_POSTS_URL = BASE_URL + "myposts.aspx"
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    /**
     * Retrieves and parses the My Posts page.
     *
     * @param repoClient The repository client to use for HTTP requests
     * @param cookies The cookies to include in the request
     * @return [MyPostsParseResult] containing the user's posts if successful
     */
    suspend fun parse(
        repoClient: RepoClient,
        cookies: List<HttpCookie> = emptyList(),
    ): MyPostsParseResult {
        try {
            val document = repoClient.parseUrl(
                url = MY_POSTS_URL,
                cookies = cookies,
            )

            // Parse the HTML response
            return parseMyPostsPage(document)
        } catch (e: Exception) {
            return MyPostsParseResult.Failure(
                "Failed to retrieve my posts: ${e.message}",
                e
            )
        }
    }

    /**
     * Parse the My Posts page and extract posts.
     *
     * @param document The HTML document to parse
     * @return [MyPostsParseResult] containing the user's posts if successfully parsed
     */
    private fun parseMyPostsPage(document: Document): MyPostsParseResult {
        val baseResult = super.parseBase(document)

        return when (baseResult) {
            is BaseParseResult.Success -> {
                try {
                    // Check for permission denial message
                    val errorMsgDiv = document.selectFirst("div.msg_inner.error_msg")
                    if (errorMsgDiv != null) {
                        val permissionMessage = errorMsgDiv.selectFirst("p")?.text() ?: "Permission denied"
                        return MyPostsParseResult.PermissionDenial(permissionMessage)
                    }

                    // Extract pagination information
                    // Using .html() instead of .text() to avoid character merging
                    val pagesText = document.selectFirst("div.pages")?.html() ?: ""
                    val pageRegex = "(\\d+)/(\\d+)页".toRegex()
                    val pageMatch = pageRegex.find(pagesText)
                    val currentPage = pageMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
                    val totalPages = pageMatch?.groupValues?.get(2)?.toIntOrNull() ?: 1

                    // Extract posts
                    val posts = mutableListOf<MyPost>()
                    val postRows = document.select("table.datatable > tbody > tr")

                    for (row in postRows) {
                        val post = parsePost(row)
                        if (post != null) {
                            posts.add(post)
                        }
                    }

                    val myPostsPage = MyPostsPage(
                        pageInfo = baseResult.pageInfo,
                        posts = posts,
                        currentPage = currentPage,
                        totalPages = totalPages
                    )

                    return MyPostsParseResult.Success(myPostsPage)

                } catch (e: Exception) {
                    return MyPostsParseResult.Failure(
                        "Failed to parse my posts page: ${e.message}",
                        e
                    )
                }
            }

            is BaseParseResult.Failure -> {
                MyPostsParseResult.Failure(baseResult.message, baseResult.exception)
            }
        }
    }

    /**
     * Parse a single post row element.
     *
     * @param row The post row element
     * @return [MyPost] or null if parsing fails
     */
    private fun parsePost(row: Element): MyPost? {
        try {
            // Extract post status icon
            val statusImg = row.selectFirst("td:first-child img")
            val iconSrc = statusImg?.attr("src") ?: ""
            val isHot = iconSrc.contains("hot")

            // Extract post title and URL
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

            return MyPost(
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