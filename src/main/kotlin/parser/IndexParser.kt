package net.keyfc.api.parser

import net.keyfc.api.ApiApplication
import net.keyfc.api.ext.plus
import net.keyfc.api.model.page.index.Forum
import net.keyfc.api.model.page.index.IndexPage
import net.keyfc.api.model.result.ArchiverParseResult
import net.keyfc.api.model.result.IndexParseResult
import org.jsoup.nodes.Element
import java.net.HttpCookie

/**
 * Call parse method to parse the index page.
 *
 * @see <a href="https://keyfc.net/bbs/archiver/index.aspx">KeyFC Index</a>
 */
object IndexParser : ArchiverParser<IndexParseResult>() {
    /**
     * Parse state class that holds temporary state during processing.
     */
    private class ParseState(
        val categories: MutableList<Forum> = mutableListOf(),
        val currentCategoryName: String = "",
        val currentCategoryId: String = "",
        val forumBuffer: MutableList<Triple<String, String, Int>> = mutableListOf() // (name, id, level)
    ) {
        /**
         * Adds current buffer content to the categories list.
         *
         * @return updated categories list
         */
        fun flushCategory(): MutableList<Forum> {
            return if (currentCategoryName.isNotEmpty()) {
                categories.apply {
                    add(
                        Forum(
                            name = currentCategoryName,
                            id = currentCategoryId,
                            subForums = buildForumTree(forumBuffer)
                        )
                    )
                }
            } else {
                categories
            }
        }

        /**
         * Starts a new category, first adding the old category to the list.
         *
         * @return new state with existing categories, new name, new id, and an empty forum buffer
         */
        fun startNewCategory(name: String, id: String): ParseState {
            return ParseState(
                categories = flushCategory(),
                currentCategoryName = name,
                currentCategoryId = id,
                forumBuffer = mutableListOf()
            )
        }

        /**
         * Adds a forum to the current buffer.
         *
         * @return updated state after adding the forum to the buffer
         */
        fun addForum(name: String, id: String, level: Int): ParseState {
            forumBuffer.add(Triple(name, id, level))
            return this
        }
    }

    fun parse(cookies: List<HttpCookie> = emptyList()): IndexParseResult =
        parse(uriToDocument(ApiApplication.archiverUri + "index.aspx", cookies))

    /**
     * Fetches and parses the index page.
     *
     * If basic page info is successfully parsed by super class, this method will reuse the HTML document provided,
     * and parses it into structured data represented by [IndexPage], wrapped in [IndexParseResult.Success].
     *
     * If parsing fails, this method will return [IndexParseResult.Failure] with the error message and exception.
     */
    override fun parseAfter(archiverParseResult: ArchiverParseResult): IndexParseResult {
        return when (archiverParseResult) {
            is ArchiverParseResult.Failure -> IndexParseResult.Failure(
                archiverParseResult.message,
                archiverParseResult.exception
            )

            is ArchiverParseResult.Success -> {
                // Select all category and forum items while maintaining order
                val elements = archiverParseResult.document.select("div.cateitem, div.forumitem")

                // Process elements in functional style using fold
                val categories = elements.fold(ParseState()) { state, element ->
                    processElement(state, element)
                }.flushCategory()

                IndexParseResult.Success(
                    IndexPage(
                        pageInfo = archiverParseResult.pageInfo,
                        categories = categories
                    )
                )
            }
        }

    }

    /**
     * Processes a single element.
     *
     * @return updated parse state
     */
    private fun processElement(state: ParseState, element: Element): ParseState {
        return when {
            element.hasClass("cateitem") -> {
                val a = element.selectFirst("h2 a")
                if (a != null) {
                    val categoryName = a.text().trim()
                    val categoryId = Forum.extractId(a.attr("href")) ?: ""
                    state.startNewCategory(categoryName, categoryId)
                } else {
                    state
                }
            }

            element.hasClass("forumitem") -> {
                val h3 = element.selectFirst("h3")
                val a = h3?.selectFirst("a")
                if (h3 != null && a != null) {
                    val name = a.text().trim()
                    val id = Forum.extractId(a.attr("href")) ?: ""
                    // Calculate level by counting spaces
                    val level = h3.wholeText().replace("\\S".toRegex(), "").length
                    state.addForum(name, id, level)
                } else {
                    state
                }
            }

            else -> state
        }
    }

    /**
     * Builds a forum tree structure from flat forum list.
     *
     * Only top-level nodes are treated as first-level forums, with child nodes nested to sub forums.
     *
     * @return a list of first-level forums
     */
    private fun buildForumTree(flatForums: List<Triple<String, String, Int>>): List<Forum> {
        data class Node(
            val name: String,
            val id: String,
            val level: Int,
            val children: MutableList<Node> = mutableListOf()
        )

        fun Node.toForum(): Forum = Forum(name, id, children.map { it.toForum() })

        // Build tree structure using fold operation
        val (roots, stack) = flatForums.fold(
            Pair(
                mutableListOf<Node>(),
                mutableListOf<Node>()
            )
        ) { (roots, stack), (name, id, level) ->
            val node = Node(name, id, level)

            // Adjust stack to find the correct parent node
            while (stack.isNotEmpty() && stack.last().level >= level) {
                val done = stack.removeAt(stack.lastIndex)
                if (stack.isEmpty()) {
                    roots.add(done)
                } else {
                    stack.last().children.add(done)
                }
            }

            stack.add(node)
            Pair(roots, stack)
        }

        // Process remaining nodes in the stack
        while (stack.isNotEmpty()) {
            val done = stack.removeAt(stack.lastIndex)
            if (stack.isEmpty()) {
                roots.add(done)
            } else {
                stack.last().children.add(done)
            }
        }

        // Convert and return the forum tree
        return roots.map { it.toForum() }
    }
}
