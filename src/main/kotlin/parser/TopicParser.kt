package net.keyfc.api.parser

import net.keyfc.api.ext.parsePagination
import net.keyfc.api.model.page.Breadcrumb
import net.keyfc.api.model.page.forum.Topic
import net.keyfc.api.model.page.index.Forum
import net.keyfc.api.model.page.topic.Post
import net.keyfc.api.model.page.topic.TopicPage
import net.keyfc.api.model.result.BaseParseResult
import net.keyfc.api.model.result.TopicParseResult
import org.jsoup.nodes.Document
import java.net.HttpCookie
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Call parse method to parse the topic page.
 *
 * @see <a href="https://keyfc.net/bbs/archiver/showtopic-70169.aspx">KeyFC Topic Sample</a>
 */
object TopicParser : BaseParser<TopicParseResult>() {
    override fun validateUrl(relativeUrl: String) {
        // Only allow `showtopic-x.aspx` or `showtopic-x-y.aspx`
        if (!relativeUrl.matches(Regex("""showtopic-\d+(-\d+)?\.aspx"""))) {
            throw IllegalArgumentException("The path format is invalid. It must be in the form `showtopic-x.aspx` or `showtopic-x-y.aspx`.")
        }
    }

    public override fun parse(relativeUrl: String, cookies: List<HttpCookie>) = super.parse(relativeUrl, cookies)

    fun parse(relativeUrl: String) = super.parse(relativeUrl, emptyList())

    fun parse(topic: Topic, cookies: List<HttpCookie> = emptyList()) = super.parse(topic.link, cookies)

    /**
     * Parse topic page and return structured [TopicPage] object.
     * @return parsed topic page data including posts, pagination, etc.
     */
    override fun parse(baseParseResult: BaseParseResult): TopicParseResult {
        return when (baseParseResult) {
            is BaseParseResult.Failure -> TopicParseResult.Failure(baseParseResult.message, baseParseResult.exception)

            is BaseParseResult.Success -> {
                validateDenial(baseParseResult.doc)?.let { return it }

                // Parse structure
                val (thisTopic, thisForum, parentForum) = parseStructure(baseParseResult.breadcrumbs)

                // Parse posts
                val posts = parsePosts(baseParseResult.doc)

                // Parse pagination
                val pagination = baseParseResult.doc.parsePagination()

                TopicParseResult.Success(
                    TopicPage(
                        pageInfo = baseParseResult.pageInfo,
                        breadcrumbs = baseParseResult.breadcrumbs,
                        thisTopic = thisTopic,
                        thisForum = thisForum,
                        parentForum = parentForum,
                        posts = posts,
                        pagination = pagination
                    )
                )
            }
        }
    }

    /**
     * Validate the accessibility under current state.
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
     * @return a triple of this topic, this forum, and parent forum
     */
    private fun parseStructure(breadcrumbs: List<Breadcrumb>): Triple<Topic, Forum, Forum> {
        // Last link is this topic
        val lastIndex = breadcrumbs.size - 1
        val thisTopic = Topic(breadcrumbs[lastIndex].name, breadcrumbs[lastIndex].link, null)

        // Second to last link is this forum
        val thisForum = Forum(breadcrumbs[lastIndex - 1].name, breadcrumbs[lastIndex - 1].link)

        // Third to last link is parent forum
        val parentForum = Forum(breadcrumbs[lastIndex - 2].name, breadcrumbs[lastIndex - 2].link)

        return Triple(thisTopic, thisForum, parentForum)
    }

    /**
     * Parse posts list.
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
