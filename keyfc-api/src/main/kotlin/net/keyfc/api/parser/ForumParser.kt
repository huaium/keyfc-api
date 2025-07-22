package net.keyfc.api.parser

import com.fleeksoft.ksoup.nodes.Document
import net.keyfc.api.RepoClient
import net.keyfc.api.model.page.Breadcrumb
import net.keyfc.api.model.page.forum.ForumPage
import net.keyfc.api.model.page.forum.Topic
import net.keyfc.api.model.page.index.Forum
import net.keyfc.api.model.result.parse.ArchiverParseResult
import net.keyfc.api.model.result.parse.ForumParseResult
import java.net.HttpCookie
import java.util.regex.Pattern

/**
 * Call parse method to parse the forum page.
 *
 * @see <a href="https://keyfc.net/bbs/archiver/showforum-52.aspx">KeyFC Forum Sample</a>
 */
internal object ForumParser : ArchiverParser() {

    override val parsePagination = true

    /**
     * Fetches and parses the forum page.
     *
     * If basic page info is successfully parsed by super class, this method will reuse the HTML document provided,
     * and validate the accessibility under current permission state. If not, it will return [ForumParseResult.PermissionDenial].
     *
     * If it is accessible, structured data represented by [ForumPage] will be returned, wrapped in [ForumParseResult.Success].
     *
     * Or, if parsing fails, it will return [ForumParseResult.Failure] with the error message and exception.
     */
    suspend fun parse(repoClient: RepoClient, id: String, cookies: List<HttpCookie> = emptyList()) =
        try {
            val archiverParseResult =
                super.parseArchiver(repoClient.parseUrl(ARCHIVER_URL + "showforum-${id}.aspx", cookies))

            when (archiverParseResult) {
                is ArchiverParseResult.Failure -> ForumParseResult.Failure(
                    archiverParseResult.message,
                    archiverParseResult.exception
                )

                is ArchiverParseResult.Success -> {
                    // Denial validation must be called before parsing breadcrumbs,
                    // since denial page does not include breadcrumbs
                    validateDenial(archiverParseResult.document)?.let { return it }

                    val (thisForum, parentForum) = parseBreadcrumbs(archiverParseResult.breadcrumbs)

                    ForumParseResult.Success(
                        ForumPage(
                            pageInfo = archiverParseResult.pageInfo,
                            breadcrumbs = archiverParseResult.breadcrumbs,
                            parentForum = parentForum,
                            thisForum = thisForum,
                            topics = parseTopics(archiverParseResult.document),
                            pagination = archiverParseResult.pagination
                        )
                    )
                }
            }
        } catch (e: Exception) {
            ForumParseResult.Failure("Soup client document parsing failed", e)
        }

    suspend fun parse(repoClient: RepoClient, forum: Forum, cookies: List<HttpCookie> = emptyList()) =
        parse(repoClient, forum.id, cookies)

    /**
     * Validate the accessibility under current state.
     *
     * @return [ForumParseResult] if denied, null otherwise
     */
    private fun validateDenial(doc: Document): ForumParseResult? {
        val msg = doc.selectFirst("div.msg")?.text()

        if (msg != null) {
            if (msg.contains("没有浏览该版块的权限"))
                return ForumParseResult.PermissionDenial(msg)

            return ForumParseResult.UnknownDenial(msg)
        }

        return null
    }


    /**
     * Parse along the relationship structure from the breadcrumbs.
     *
     * @return a pair of this forum and its parent forum
     * @throws [IllegalArgumentException] if breadcrumbs is empty
     */
    private fun parseBreadcrumbs(breadcrumbs: List<Breadcrumb>): Pair<Forum, Forum> {
        if (breadcrumbs.isEmpty())
            throw IllegalArgumentException("Breadcrumbs cannot be empty.")

        // Last link is this forum
        val lastIndex = breadcrumbs.size - 1
        val thisForum = Forum(breadcrumbs[lastIndex].name, Forum.Companion.extractId(breadcrumbs[lastIndex].link) ?: "")

        // Second to last link is parent forum
        val parentForum =
            Forum(breadcrumbs[lastIndex - 1].name, Forum.Companion.extractId(breadcrumbs[lastIndex - 1].link) ?: "")

        return Pair(thisForum, parentForum)
    }

    /**
     * Parse topic list.
     *
     * @return a list of topics
     */
    private fun parseTopics(doc: Document): List<Topic> {
        return doc.select("#wrap ol li").map { element ->
            val anchor = element.selectFirst("a")
            val title = anchor?.text() ?: ""
            val id = Topic.Companion.extractId(anchor?.attr("href")) ?: ""

            // Parse reply count, format is usually `(X replies)`
            val replyCountText = element.ownText().trim()
            val replyCount = extractReplyCount(replyCountText)

            Topic(title, id, replyCount)
        }
    }

    /**
     * Extract reply count from text.
     *
     * @return reply count
     */
    private fun extractReplyCount(text: String): Int {
        // Use regex to extract number
        val matcher = Pattern.compile("\\((\\d+) 篇回复\\)").matcher(text)
        return if (matcher.find()) {
            matcher.group(1).toInt()
        } else {
            0
        }
    }
}
