package net.keyfc.api.parser

import net.keyfc.api.ext.parsePagination
import net.keyfc.api.model.page.Breadcrumb
import net.keyfc.api.model.page.forum.ForumPage
import net.keyfc.api.model.page.forum.Topic
import net.keyfc.api.model.page.index.Forum
import net.keyfc.api.model.result.BaseParseResult
import net.keyfc.api.model.result.ForumParseResult
import org.jsoup.nodes.Document
import java.util.regex.Pattern

/**
 * The constructor accepts either relative URL or a [Forum] object, allowing for forms like `showforum-x.aspx` or `showforum-x-y.aspx`.
 *
 * After initialization, call parse() method to parse the forum page.
 *
 * @see <a href="https://keyfc.net/bbs/archiver/showforum-52.aspx">KeyFC Forum Sample</a>
 */
class ForumParser : BaseParser<ForumParseResult> {
    override fun validateUrl(relativeUrl: String) {
        // Only allow `showforum-x.aspx` or `showforum-x-y.aspx`
        if (!relativeUrl.matches(Regex("""showforum-\d+(-\d+)?\.aspx"""))) {
            throw IllegalArgumentException("The path format is invalid. It must be in the form `showforum-x.aspx` or `showforum-x-y.aspx`.")
        }
    }

    constructor(relativeUrl: String) : super(relativeUrl)

    constructor(forum: Forum) : super(forum.link)

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
    override fun parse(baseParseResult: BaseParseResult): ForumParseResult {
        return when (baseParseResult) {
            is BaseParseResult.Failure -> ForumParseResult.Failure(baseParseResult.message, baseParseResult.exception)

            is BaseParseResult.Success -> {
                validateDenial(baseParseResult.doc)?.let { return it }

                val (thisForum, parentForum) = parseForumStructure(baseParseResult.breadcrumbs)

                ForumParseResult.Success(
                    ForumPage(
                        pageInfo = baseParseResult.pageInfo,
                        breadcrumbs = baseParseResult.breadcrumbs,
                        parentForum = parentForum,
                        thisForum = thisForum,
                        topics = parseTopics(baseParseResult.doc),
                        pagination = baseParseResult.doc.parsePagination()
                    )
                )
            }
        }
    }

    /**
     * Validate the accessibility under current state.
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
     * Parse forum structure.
     * @return a pair of this forum and its parent forum
     */
    private fun parseForumStructure(breadcrumbs: List<Breadcrumb>): Pair<Forum, Forum> {
        // Last link is this forum
        val lastIndex = breadcrumbs.size - 1
        val thisForum = Forum(breadcrumbs[lastIndex].name, breadcrumbs[lastIndex].link)

        // Second to last link is parent forum
        val parentForum = Forum(breadcrumbs[lastIndex - 1].name, breadcrumbs[lastIndex - 1].link)

        return Pair(thisForum, parentForum)
    }

    /**
     * Parse topic list.
     * @return a list of topics
     */
    private fun parseTopics(doc: Document): List<Topic> {
        return doc.select("#wrap ol li").map { element ->
            val anchor = element.selectFirst("a")
            val title = anchor?.text() ?: ""
            val link = anchor?.attr("href") ?: ""

            // Parse reply count, format is usually `(X replies)`
            val replyCountText = element.ownText().trim()
            val replyCount = extractReplyCount(replyCountText)

            Topic(title, link, replyCount)
        }
    }

    /**
     * Extract reply count from text.
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
