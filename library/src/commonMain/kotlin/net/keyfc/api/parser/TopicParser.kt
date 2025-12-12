package net.keyfc.api.parser

import com.fleeksoft.ksoup.nodes.Document
import io.ktor.http.Cookie
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import net.keyfc.api.RepoClient
import net.keyfc.api.RepoClient.Companion.ARCHIVER_URL
import net.keyfc.api.ext.archiverBreadcrumbs
import net.keyfc.api.ext.pageInfo
import net.keyfc.api.ext.pagination
import net.keyfc.api.ext.parseDateTime
import net.keyfc.api.ext.parseId
import net.keyfc.api.ext.validate
import net.keyfc.api.model.Breadcrumb
import net.keyfc.api.model.forum.Topic
import net.keyfc.api.model.index.Forum
import net.keyfc.api.model.topic.Post
import net.keyfc.api.model.topic.TopicPage

internal object TopicParser {

    private const val IS_ARCHIVER = true

    private fun getTopicUrl(id: String) = ARCHIVER_URL + "showtopic-${id}.aspx"

    @OptIn(FormatStringsInDatetimeFormats::class)
    private fun parseDateTime(dateTimeText: String) =
        dateTimeText.parseDateTime(LocalDateTime.Format { byUnicodePattern("yyyy/M/d H:mm:ss") })

    /**
     * Parses topic page and return structured [TopicPage] object.
     *
     * @return parsed topic page data including posts, pagination, etc., or an error if parsing fails
     */
    suspend fun parse(
        repoClient: RepoClient,
        id: String,
        cookies: List<Cookie> = emptyList()
    ): Result<TopicPage> {
        return runCatching {
            val document = repoClient.parseUrl(getTopicUrl(id), cookies).apply {
                this.validate().getOrThrow()
            }

            val breadcrumbs = document.archiverBreadcrumbs()

            // Parse breadcrumbs
            val (thisTopic, thisForum, parentForum) = parseBreadcrumbs(breadcrumbs)

            TopicPage(
                document = document,
                pageInfo = document.pageInfo(),
                breadcrumbs = breadcrumbs,
                thisTopic = thisTopic,
                thisForum = thisForum,
                parentForum = parentForum,
                posts = parsePosts(document),
                pagination = document.pagination(IS_ARCHIVER)
            )
        }
    }

    suspend fun parse(repoClient: RepoClient, topic: Topic, cookies: List<Cookie> = emptyList()) =
        parse(repoClient, topic.id, cookies)

    /**
     * Parses along the relationship structure from the breadcrumbs.
     *
     * @return a triple of this topic, this forum, and parent forum
     */
    private fun parseBreadcrumbs(breadcrumbs: List<Breadcrumb>): Triple<Topic?, Forum?, Forum?> {
        if (breadcrumbs.isEmpty())
            return Triple(null, null, null)

        // Last link is this topic
        val lastIndex = breadcrumbs.size - 1
        val thisTopic =
            Topic(
                breadcrumbs[lastIndex].name,
                breadcrumbs[lastIndex].link.parseId(),
                null
            )

        // Second to last link is this forum
        val thisForum =
            Forum(
                breadcrumbs[lastIndex - 1].name,
                breadcrumbs[lastIndex - 1].link.parseId()
            )

        // Third to last link is parent forum
        val parentForum =
            Forum(
                breadcrumbs[lastIndex - 2].name,
                breadcrumbs[lastIndex - 2].link.parseId()
            )

        return Triple(thisTopic, thisForum, parentForum)
    }

    /**
     * Parses posts list.
     *
     * @return a list of posts
     */
    private fun parsePosts(document: Document): List<Post> =
        document.select("div.postitem").mapIndexedNotNull { index, element ->
            val titleElement = element.selectFirst("div.postitemtitle")
            val contentElement = element.selectFirst("div.postitemcontent")

            // Extract author and post time
            val titleText = titleElement?.text() ?: return@mapIndexedNotNull null
            val (author, postTime, postTimeText) = parseAuthorAndTime(titleText)
            if (author == null || postTime == null || postTimeText == null) return@mapIndexedNotNull null

            // Extract post content
            val content = contentElement?.html() ?: return@mapIndexedNotNull null

            Post(author, postTime, postTimeText, content, index + 1)
        }

    /**
     * Parses author and post time from title text.
     *
     * @return a triple of author name, parsed post time, and post time text
     */
    private fun parseAuthorAndTime(titleText: String): Triple<String?, LocalDateTime?, String?> {
        // Pattern for "Author - yyyy/M/d H:mm:ss"
        val regex = Regex("(.+) - (\\d{4}/\\d{1,2}/\\d{1,2} \\d{1,2}:\\d{2}:\\d{2})")
        val matchResult = regex.find(titleText)

        return if (matchResult != null) {
            val (rawAuthor, timeString) = matchResult.destructured

            val author = rawAuthor.trim()
            val postTime = parseDateTime(timeString)

            Triple(author, postTime, timeString)
        } else {
            Triple(null, null, null)
        }
    }
}