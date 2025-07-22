package net.keyfc.api.parser

import com.fleeksoft.ksoup.nodes.Document
import io.ktor.client.statement.*
import net.keyfc.api.RepoClient
import net.keyfc.api.model.PageInfo
import net.keyfc.api.model.index.Forum
import net.keyfc.api.model.search.LastPost
import net.keyfc.api.model.search.SearchItem
import net.keyfc.api.model.search.SearchPage
import net.keyfc.api.model.search.User
import net.keyfc.api.result.parse.BaseParseResult
import net.keyfc.api.result.parse.SearchParseResult
import java.net.HttpCookie
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Parser for search result pages.
 * This parser extracts the search redirection link from the search page.
 */
internal object SearchParser : BaseParser() {

    private const val SEARCH_URL = BASE_URL + "search.aspx"
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")

    /**
     * Submit a search request and parse the response to extract the search redirection link.
     *
     * @param repoClient The repository client to use for HTTP requests
     * @param keyword The search keyword
     * @param cookies The cookies to include in the request
     * @return [SearchParseResult] containing the search redirection link if found
     */
    suspend fun search(
        repoClient: RepoClient,
        keyword: String,
        cookies: List<HttpCookie> = emptyList()
    ): SearchParseResult {
        try {
            // Submit the search form
            val response = repoClient.postFormData(
                url = SEARCH_URL,
                formDataMap = mapOf(
                    "keyword" to keyword,
                    "poster" to "",
                    "type" to "post",
                    "keywordtype" to "0",
                    "posttableid" to "1",
                    "searchtime" to "0",
                    "searchtimetype" to "0",
                    "resultorder" to "0",
                    "resultordertype" to "0",
                    "searchforumid" to "",
                    "submit" to ""
                ),
                cookies = cookies,
            )

            // Parse the HTML response
            return parseRedirectPage(repoClient, cookies, repoClient.parseHtml(response.bodyAsText()))
        } catch (e: Exception) {
            return SearchParseResult.Failure(
                "Failed to submit search request: ${e.message}",
                e
            )
        }
    }

    /**
     * Parse the redirect page and extract the search redirection link.
     *
     * @param document The HTML document to parse
     * @return SearchParseResult containing the search redirection link if found
     */
    private suspend fun parseRedirectPage(
        repoClient: RepoClient,
        cookies: List<HttpCookie>,
        document: Document
    ): SearchParseResult {
        val baseResult = super.parseBase(document)

        return when (baseResult) {
            is BaseParseResult.Success -> {
                try {
                    // Check for permission denial message
                    val errorMsgDiv = document.selectFirst("div.msg_inner.error_msg")
                    if (errorMsgDiv != null) {
                        val permissionMessage = errorMsgDiv.selectFirst("p")?.text() ?: "Permission denied"
                        return SearchParseResult.PermissionDenial(permissionMessage)
                    }

                    // Extract the link from the msg_inner div
                    val msgInnerDiv = document.selectFirst("div.msg_inner")
                    val redirectLink = msgInnerDiv?.selectFirst("a")?.attr("href")

                    if (redirectLink == null)
                        return SearchParseResult.Failure(
                            "Failed to extract search redirection link: No link found",
                            RuntimeException("Failed to extract search redirection link: No link found")
                        )

                    parseResultPage(
                        document = repoClient.parseUrl(BASE_URL + redirectLink, cookies),
                        pageInfo = baseResult.pageInfo
                    )
                } catch (e: Exception) {
                    SearchParseResult.Failure(
                        "Failed to extract search redirection link: ${e.message}",
                        e
                    )
                }
            }

            is BaseParseResult.Failure -> {
                SearchParseResult.Failure(baseResult.message, baseResult.exception)
            }
        }
    }

    fun parseResultPage(document: Document, pageInfo: PageInfo): SearchParseResult {
        try {
            // Extract total results count
            val channelInfoText = document.selectFirst("p.channelinfo")?.text() ?: ""
            val totalResultsRegex = "共搜索到(\\d+)个符合条件的帖子".toRegex()
            val totalResultsMatch = totalResultsRegex.find(channelInfoText)
            val totalResults = totalResultsMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0

            // Extract pagination information
            val pagesText = document.selectFirst("div.pages")?.text() ?: ""
            val pageRegex = "(\\d+)/(\\d+)页".toRegex()
            val pageMatch = pageRegex.find(pagesText)
            val currentPage = pageMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
            val totalPages = pageMatch?.groupValues?.get(2)?.toIntOrNull() ?: 1

            // Extract search results
            val searchItems = mutableListOf<SearchItem>()
            val resultTbodies = document.select("div.threadlist.searchlist > table > tbody")

            for (tbody in resultTbodies) {
                // Skip the header row
                if (tbody.hasClass("category")) continue

                val tr = tbody.selectFirst("tr") ?: continue

                // Extract topic information
                val titleElement = tr.selectFirst("th.subject > a")
                val topicUrl = titleElement?.attr("href") ?: continue
                val topicId = extractIdFromUrl(topicUrl)
                val title = titleElement.text()

                // Extract forum information
                val forumElement = tr.selectFirst("td > a[href^=showforum]")
                val forumUrl = forumElement?.attr("href") ?: continue
                val forumId = extractIdFromUrl(forumUrl)
                val forumName = forumElement.text()

                // Extract author information
                val authorElement = tr.selectFirst("td.author > cite > a")
                val authorUrl = authorElement?.attr("href") ?: continue
                val authorId = extractIdFromUrl(authorUrl)
                val authorName = authorElement.text()

                // Extract post date
                val postDateText = tr.selectFirst("td.author > em")?.text()?.trim() ?: continue
                val postDate = parseDateTime(postDateText)

                // Extract reply and view counts
                val numsText = tr.selectFirst("td.nums")?.text() ?: "0 / 0"
                val (replyCount, viewCount) = parseNums(numsText)

                // Extract last post information
                val lastPostDateElement = tr.selectFirst("td.lastpost > em > a")
                val lastPostUrl = lastPostDateElement?.attr("href") ?: continue
                val lastPostDateText = lastPostDateElement.text().trim()
                val lastPostDate = parseDateTime(lastPostDateText)

                val lastPostAuthorElement = tr.selectFirst("td.lastpost > cite > a")
                val lastPostAuthorUrl = lastPostAuthorElement?.attr("href") ?: continue
                val lastPostAuthorId = extractIdFromUrl(lastPostAuthorUrl)
                val lastPostAuthorName = lastPostAuthorElement.text()

                // Create search item
                val searchItem = SearchItem(
                    id = topicId,
                    title = title,
                    url = topicUrl,
                    forum = Forum(
                        name = forumName,
                        id = forumId,
                    ),
                    author = User(
                        id = authorId,
                        name = authorName,
                    ),
                    postDate = postDate,
                    replyCount = replyCount,
                    viewCount = viewCount,
                    lastPost = LastPost(
                        date = lastPostDate,
                        url = lastPostUrl,
                        author = User(
                            id = lastPostAuthorId,
                            name = lastPostAuthorName,
                        )
                    )
                )

                searchItems.add(searchItem)
            }

            val searchPage = SearchPage(
                pageInfo = pageInfo,
                totalResults = totalResults,
                currentPage = currentPage,
                totalPages = totalPages,
                items = searchItems
            )

            return SearchParseResult.Success(searchPage)

        } catch (e: Exception) {
            return SearchParseResult.Failure(
                "Failed to parse search results page: ${e.message}",
                e
            )
        }
    }

    /**
     * Extracts the ID from a URL like "showtopic-12345.aspx" or "userinfo-12345.aspx"
     */
    private fun extractIdFromUrl(url: String): String {
        val regex = "-(\\d+)".toRegex()
        val match = regex.find(url)
        return match?.groupValues?.get(1) ?: ""
    }

    /**
     * Parses date and time from the format "yyyy.MM.dd HH:mm"
     */
    private fun parseDateTime(dateTimeText: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateTimeText.trim(), dateFormatter)
        } catch (_: Exception) {
            LocalDateTime.now() // Default to current time if parsing fails
        }
    }

    /**
     * Parses reply count and view count from text like "6 / 8566"
     */
    private fun parseNums(numsText: String): Pair<Int, Int> {
        val parts = numsText.split("/")
        val replyCount = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 0
        val viewCount = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
        return Pair(replyCount, viewCount)
    }
}