package net.keyfc.api.parser

import com.fleeksoft.ksoup.nodes.Document
import net.keyfc.api.RepoClient
import net.keyfc.api.model.Breadcrumb
import net.keyfc.api.model.forum.Topic
import net.keyfc.api.model.index.Forum
import net.keyfc.api.result.parse.ArchiverParseResult
import net.keyfc.api.result.parse.TopicParseResult
import net.keyfc.api.model.topic.Post
import net.keyfc.api.model.topic.TopicPage
import java.net.HttpCookie
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Call parse method to parse the topic page.
 *
 * @see <a href="https://keyfc.net/bbs/archiver/showtopic-70169.aspx">KeyFC Topic Sample</a>
 */
internal object TopicParser : ArchiverParser() {

    override val parsePagination = true

    /**
     * Parse topic page and return structured [TopicPage] object.
     *
     * @return parsed topic page data including posts, pagination, etc.
     */
    suspend fun parse(repoClient: RepoClient, id: String, cookies: List<HttpCookie> = emptyList()) =
        try {
            val archiverParseResult =
                super.parseArchiver(repoClient.parseUrl(ARCHIVER_URL + "showtopic-${id}.aspx", cookies))

            when (archiverParseResult) {
                is ArchiverParseResult.Failure -> TopicParseResult.Failure(
                    archiverParseResult.message,
                    archiverParseResult.exception
                )

                is ArchiverParseResult.Success -> {
                    // Denial validation must be called before parsing breadcrumbs,
                    // since denial page does not include breadcrumbs
                    validateDenial(archiverParseResult.document)?.let { return it }

                    // Parse breadcrumbs
                    val (thisTopic, thisForum, parentForum) = parseBreadcrumbs(archiverParseResult.breadcrumbs)

                    TopicParseResult.Success(
                        TopicPage(
                            pageInfo = archiverParseResult.pageInfo,
                            breadcrumbs = archiverParseResult.breadcrumbs,
                            thisTopic = thisTopic,
                            thisForum = thisForum,
                            parentForum = parentForum,
                            posts = parsePosts(archiverParseResult.document),
                            pagination = archiverParseResult.pagination
                        )
                    )
                }
            }
        } catch (e: Exception) {
            TopicParseResult.Failure("Soup client document parsing failed", e)
        }

    suspend fun parse(repoClient: RepoClient, topic: Topic, cookies: List<HttpCookie> = emptyList()) =
        parse(repoClient, topic.id, cookies)

    /**
     * Validate the accessibility under current state.
     *
     * @return [TopicParseResult] if denied, null otherwise
     */
    private fun validateDenial(doc: Document): TopicParseResult? {
        val msg = doc.selectFirst("div.msg")?.text()

        if (msg != null) {
            if (msg.contains("阅读权限不够")) {
                val requiredPermissionRegex = "本主题阅读权限为: (\\d+)".toRegex()
                val requiredPermissionMatch = requiredPermissionRegex.find(msg)
                val requiredPermissionLevel = requiredPermissionMatch?.groupValues?.get(1)?.toIntOrNull() ?: -1

                val currentIdentityRegex = "您当前的身份 \"(.+?)\" 阅读权限不够".toRegex()
                val currentIdentityMatch = currentIdentityRegex.find(msg)
                val currentIdentity = currentIdentityMatch?.groupValues?.get(1) ?: "未知用户"

                return TopicParseResult.PermissionDenial(
                    requiredPermissionLevel = requiredPermissionLevel,
                    currentIdentity = currentIdentity,
                )
            }

            return TopicParseResult.UnknownDenial(msg)
        }

        return null
    }

    /**
     * Parse along the relationship structure from the breadcrumbs.
     *
     * @return a triple of this topic, this forum, and parent forum
     * @throws [IllegalArgumentException] if breadcrumbs is empty
     */
    private fun parseBreadcrumbs(breadcrumbs: List<Breadcrumb>): Triple<Topic, Forum, Forum> {
        if (breadcrumbs.isEmpty())
            throw IllegalArgumentException("Breadcrumbs cannot be empty.")

        // Last link is this topic
        val lastIndex = breadcrumbs.size - 1
        val thisTopic =
            Topic(breadcrumbs[lastIndex].name, Topic.Companion.extractId(breadcrumbs[lastIndex].link) ?: "", null)

        // Second to last link is this forum
        val thisForum =
            Forum(breadcrumbs[lastIndex - 1].name, Forum.Companion.extractId(breadcrumbs[lastIndex - 1].link) ?: "")

        // Third to last link is parent forum
        val parentForum =
            Forum(breadcrumbs[lastIndex - 2].name, Forum.Companion.extractId(breadcrumbs[lastIndex - 2].link) ?: "")

        return Triple(thisTopic, thisForum, parentForum)
    }

    /**
     * Parse posts list.
     *
     * @return a list of posts
     */
    private fun parsePosts(doc: Document): List<Post> = doc.select("div.postitem").mapIndexed { index, element ->
        val titleElement = element.selectFirst("div.postitemtitle")
        val contentElement = element.selectFirst("div.postitemcontent")

        // Extract author and post time
        val titleText = titleElement?.text() ?: ""
        val authorAndTime = parseAuthorAndTime(titleText)
        val author = authorAndTime.first
        val postTime = authorAndTime.second

        // Extract post content
        val content = contentElement?.html() ?: ""

        Post(author, postTime, content, index + 1)
    }

    /**
     * Extract author and post time from title text.
     *
     * @return a pair of author name and post time
     */
    private fun parseAuthorAndTime(titleText: String): Pair<String, Date> {
        val dateFormat = SimpleDateFormat("yyyy/M/d H:mm:ss", Locale.US)

        // Pattern for "Author - YYYY/MM/DD HH:MM:SS"
        val pattern = Pattern.compile("(.+) - (\\d{4}/\\d{1,2}/\\d{1,2} \\d{1,2}:\\d{2}:\\d{2})")
        val matcher = pattern.matcher(titleText)

        return if (matcher.find()) {
            val author = matcher.group(1).trim()
            val timeString = matcher.group(2)
            val postTime = try {
                dateFormat.parse(timeString)
            } catch (_: Exception) {
                Date()  // Fallback to current time if parsing fails
            }
            Pair(author, postTime)
        } else {
            Pair(titleText, Date())  // Fallback if pattern doesn't match
        }
    }
}
