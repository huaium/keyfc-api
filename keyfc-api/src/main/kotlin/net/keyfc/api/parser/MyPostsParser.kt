package net.keyfc.api.parser

import com.fleeksoft.ksoup.nodes.Element
import net.keyfc.api.RepoClient
import net.keyfc.api.RepoClient.Companion.BASE_URL
import net.keyfc.api.ext.*
import net.keyfc.api.model.User
import net.keyfc.api.model.myposts.MyPost
import net.keyfc.api.model.myposts.MyPostsPage
import java.net.HttpCookie
import java.time.format.DateTimeFormatter.ofPattern

internal object MyPostsParser {

    private const val MY_POSTS_URL = BASE_URL + "myposts.aspx"

    private const val IS_ARCHIVER = false


    private fun parseDateTime(dateTimeText: String) = dateTimeText.parseDateTime(ofPattern("yyyy-MM-dd HH:mm"))

    /**
     * Retrieves and parses the My Posts page.
     *
     * @param repoClient The repository client to use for HTTP requests
     * @param cookies The cookies to include in the request
     *
     * @return [Result] containing [MyPostsPage] if successful, or an error if parsing fails
     */
    suspend fun parse(
        repoClient: RepoClient,
        cookies: List<HttpCookie> = emptyList()
    ) = parse(repoClient, MY_POSTS_URL, cookies)

    internal suspend fun parse(
        repoClient: RepoClient,
        url: String,
        cookies: List<HttpCookie> = emptyList(),
    ): Result<MyPostsPage> {
        return runCatching {
            val document = repoClient.parseUrl(
                url = url,
                cookies = cookies,
            ).apply { this.validate().getOrThrow() }

            val posts = document
                .select("table.datatable > tbody > tr")
                .mapNotNull { parsePost(it) }

            MyPostsPage(
                document = document,
                pageInfo = document.pageInfo(),
                posts = posts,
                pagination = document.pagination(IS_ARCHIVER)
            )
        }
    }

    /**
     * Parses a single post row element.
     *
     * @param row The post row element
     *
     * @return [MyPost] or null if parsing fails
     */
    private fun parsePost(row: Element): MyPost? {
        try {
            // Extract post status icon
            val statusImg = row.selectFirst("td:first-child img")
            val iconSrc = statusImg?.attr("src")
            val isHot = iconSrc?.contains("hot") ?: return null

            // Extract post title and URL
            val titleElement = row.selectFirst("td.datatitle > a")
            val title = titleElement?.text()?.trim() ?: return null
            val url = titleElement.attr("href")
            val id = url.parseId()

            // Extract forum information
            val forumElement = row.selectFirst("td:nth-child(4) > a")
            val forumName = forumElement?.text()?.trim() ?: return null
            val forumUrl = forumElement.attr("href")
            val forumId = forumUrl.parseId()

            // Extract last post information
            val lastPostCell = row.selectFirst("td:last-child")

            // Extract last post date
            val dateElement = lastPostCell?.selectFirst("span.time > a")
            val dateText = dateElement?.text()?.trim() ?: return null
            val date = parseDateTime(dateText)

            // Extract last post user
            val userElement = lastPostCell.selectFirst("a[href^=userinfo-]")
            val lastPostUser = parseLastPostUser(userElement) ?: return null

            return MyPost(
                id = id,
                title = title,
                url = url,
                forumName = forumName,
                forumId = forumId,
                forumUrl = forumUrl,
                lastPostDate = date,
                lastPostDateText = dateText,
                lastPostUser = lastPostUser,
                isHot = isHot
            )
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Parses the last post user from the user element.
     */
    private fun parseLastPostUser(user: Element?): User? {
        val userName = user?.text()?.trim()
        val userUrl = user?.attr("href")
        val userId = userUrl?.parseId()

        if (userId == null || userName == null)
            return null

        return User(id = userId, name = userName)
    }
}