package net.keyfc.api.parser

import com.fleeksoft.ksoup.nodes.Document
import net.keyfc.api.RepoClient
import net.keyfc.api.RepoClient.Companion.ARCHIVER_URL
import net.keyfc.api.ext.*
import net.keyfc.api.model.Breadcrumb
import net.keyfc.api.model.forum.ForumPage
import net.keyfc.api.model.forum.Topic
import net.keyfc.api.model.index.Forum
import java.net.HttpCookie
import java.util.regex.Pattern

internal object ForumParser {

    private const val IS_ARCHIVER = true

    private fun getForumUrl(id: String) = ARCHIVER_URL + "showforum-${id}.aspx"

    /**
     * Fetches and parses the forum page.
     *
     * If it is accessible, structured data represented by [ForumPage] will be returned, wrapped in [Result].
     *
     * Or, if parsing fails, it will return [Result] with the error message and exception.
     */
    suspend fun parse(repoClient: RepoClient, id: String, cookies: List<HttpCookie> = emptyList()): Result<ForumPage> {
        return runCatching {
            val document = repoClient.parseUrl(getForumUrl(id), cookies)
                .apply { this.validate().getOrThrow() }

            val breadcrumbs = document.archiverBreadcrumbs()

            val (thisForum, parentForum) = parseBreadcrumbs(breadcrumbs)

            ForumPage(
                document = document,
                pageInfo = document.pageInfo(),
                breadcrumbs = breadcrumbs,
                parentForum = parentForum,
                thisForum = thisForum,
                topics = parseTopics(document),
                pagination = document.pagination(IS_ARCHIVER)
            )
        }
    }

    suspend fun parse(repoClient: RepoClient, forum: Forum, cookies: List<HttpCookie> = emptyList()) =
        parse(repoClient, forum.id, cookies)

    /**
     * Parses along the relationship structure from the breadcrumbs.
     *
     * Notice: will return a pair of null if the breadcrumbs is empty.
     *
     * @return a pair of this forum and its parent forum
     */
    private fun parseBreadcrumbs(breadcrumbs: List<Breadcrumb>): Pair<Forum?, Forum?> {
        if (breadcrumbs.isEmpty())
            return Pair(null, null)

        // Last link is this forum
        val lastIndex = breadcrumbs.size - 1
        val thisForum = Forum(breadcrumbs[lastIndex].name, breadcrumbs[lastIndex].link.parseId())

        // Second to last link is parent forum
        val parentForum =
            Forum(breadcrumbs[lastIndex - 1].name, breadcrumbs[lastIndex - 1].link.parseId())

        return Pair(thisForum, parentForum)
    }

    /**
     * Parses topic list.
     *
     * @return a list of topics
     */
    private fun parseTopics(document: Document): List<Topic> {
        return document.select("#wrap ol li").mapNotNull { element ->
            val anchor = element.selectFirst("a")
            val title = anchor?.text() ?: return@mapNotNull null
            val id = anchor.attr("href").parseId()

            // Parse reply count, format is usually `(X replies)`
            val replyCountText = element.ownText().trim()
            val replyCount = parseReplyCount(replyCountText)

            Topic(title, id, replyCount)
        }
    }

    /**
     * Parses reply count from text.
     *
     * @return reply count
     */
    private fun parseReplyCount(text: String): Int? {
        // Use regex to extract number
        val matcher = Pattern.compile("\\((\\d+) 篇回复\\)").matcher(text)
        return if (matcher.find()) {
            matcher.group(1).toInt()
        } else {
            null
        }
    }
}
