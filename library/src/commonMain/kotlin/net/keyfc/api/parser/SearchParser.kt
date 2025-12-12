package net.keyfc.api.parser

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import io.ktor.client.statement.*
import io.ktor.http.Cookie
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import net.keyfc.api.RepoClient
import net.keyfc.api.RepoClient.Companion.BASE_URL
import net.keyfc.api.ext.pageInfo
import net.keyfc.api.ext.pagination
import net.keyfc.api.ext.parseDateTime
import net.keyfc.api.ext.parseId
import net.keyfc.api.ext.validate
import net.keyfc.api.model.PageInfo
import net.keyfc.api.model.User
import net.keyfc.api.model.index.Forum
import net.keyfc.api.model.search.LastPost
import net.keyfc.api.model.search.SearchItem
import net.keyfc.api.model.search.SearchPage

internal object SearchParser {

    private const val SEARCH_URL = BASE_URL + "search.aspx"

    private const val IS_ARCHIVER = false

    private fun getFullRedirectUrl(redirectLink: String) = BASE_URL + redirectLink

    @OptIn(FormatStringsInDatetimeFormats::class)
    private fun parseDateTime(dateTimeText: String) =
        dateTimeText.parseDateTime(LocalDateTime.Format { byUnicodePattern("yyyy.MM.dd HH:mm") })

    /**
     * Submits a search request and parse the response to extract the search redirection link.
     *
     * @param repoClient The repository client to use for HTTP requests
     * @param keyword The search keyword
     * @param cookies The cookies to include in the request
     *
     * @return [Result] containing the search redirection link if found, or an error if not found
     */
    suspend fun search(
        repoClient: RepoClient,
        keyword: String,
        cookies: List<Cookie> = emptyList()
    ): Result<SearchPage> {
        return runCatching {
            // Submit the search form
            val document = repoClient.postFormData(
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
                .let { repoClient.parseHtml(it.bodyAsText()) }
                .apply { this.validate().getOrThrow() }

            // Extract the link from the msg_inner div
            val msgInnerDiv = document.selectFirst("div.msg_inner")
            val redirectLink = msgInnerDiv?.selectFirst("a")?.attr("href")
                ?: throw RuntimeException("No redirect link found in the search result page")

            return parseResultPage(
                document = repoClient.parseUrl(getFullRedirectUrl(redirectLink), cookies),
                pageInfo = document.pageInfo()
            )
        }
    }

    /**
     * Parses the search result page to extract search items and pagination information.
     *
     * @param document The HTML document of the search result page
     * @param pageInfo The page information extracted from the document
     *
     * @return [Result] containing the parsed [SearchPage] if successful, or an error if parsing fails
     */
    private fun parseResultPage(document: Document, pageInfo: PageInfo): Result<SearchPage> {
        return runCatching {
            // Extract total results count
            val channelInfoText = document.selectFirst("p.channelinfo")?.text() ?: ""
            val totalResultsRegex = "共搜索到(\\d+)个符合条件的帖子".toRegex()
            val totalResultsMatch = totalResultsRegex.find(channelInfoText)
            val totalResults = totalResultsMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0

            // Extract search results
            val searchItems = document
                .select("div.threadlist.searchlist > table > tbody")
                .mapNotNull { parseSearchItem(it) }

            SearchPage(
                document = document,
                pageInfo = pageInfo,
                totalResults = totalResults,
                pagination = document.pagination(IS_ARCHIVER),
                items = searchItems
            )
        }
    }

    /**
     * Parses a single search item from the search result table body.
     *
     * @param tbody The table body element containing the search item
     *
     * @return [SearchItem] if parsing is successful, or null if parsing fails
     */
    private fun parseSearchItem(tbody: Element): SearchItem? {
        // Skip the header row
        if (tbody.hasClass("category")) return null

        val tr = tbody.selectFirst("tr") ?: return null

        // Extract topic information
        val titleElement = tr.selectFirst("th.subject > a")
        val topicUrl = titleElement?.attr("href") ?: return null
        val topicId = topicUrl.parseId()
        val title = titleElement.text()

        // Extract forum information
        val forumElement = tr.selectFirst("td > a[href^=showforum]")
        val forumUrl = forumElement?.attr("href") ?: return null
        val forumId = forumUrl.parseId()
        val forumName = forumElement.text()

        // Extract author information
        val authorElement = tr.selectFirst("td.author > cite > a")
        val authorUrl = authorElement?.attr("href") ?: return null
        val authorId = authorUrl.parseId()
        val authorName = authorElement.text()

        // Extract post date
        val postDateText = tr.selectFirst("td.author > em")?.text()?.trim() ?: return null
        val postDate = parseDateTime(postDateText)

        // Extract reply and view counts
        val numsText = tr.selectFirst("td.nums")?.text() ?: "0 / 0"
        val (replyCount, viewCount) = parseNums(numsText)

        // Extract last post information
        val lastPostDateElement = tr.selectFirst("td.lastpost > em > a")
        val lastPostUrl = lastPostDateElement?.attr("href") ?: return null

        val lastPostDateText = lastPostDateElement.text().trim()
        val lastPostDate = parseDateTime(lastPostDateText)

        val lastPostAuthorElement = tr.selectFirst("td.lastpost > cite > a")
        val lastPostAuthor = parseAuthor(lastPostAuthorElement) ?: return null


        // Create search item
        return SearchItem(
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
            postDateText = postDateText,
            replyCount = replyCount,
            viewCount = viewCount,
            lastPost = LastPost(
                date = lastPostDate,
                dateText = lastPostDateText,
                url = lastPostUrl,
                author = lastPostAuthor
            )
        )
    }

    /**
     * Parses the author element to extract user information.
     */
    private fun parseAuthor(author: Element?): User? {
        val lastPostAuthorUrl = author?.attr("href") ?: return null
        val lastPostAuthorId = lastPostAuthorUrl.parseId()
        val lastPostAuthorName = author.text()

        return User(id = lastPostAuthorId, name = lastPostAuthorName)
    }

    /**
     * Parses reply count and view count from text like "6 / 8566".
     */
    private fun parseNums(numsText: String): Pair<Int, Int> {
        val parts = numsText.split("/")
        val replyCount = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 0
        val viewCount = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0

        return Pair(replyCount, viewCount)
    }
}