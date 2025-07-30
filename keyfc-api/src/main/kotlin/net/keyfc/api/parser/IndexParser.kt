package net.keyfc.api.parser

import com.fleeksoft.ksoup.nodes.Element
import net.keyfc.api.RepoClient
import net.keyfc.api.RepoClient.Companion.ARCHIVER_URL
import net.keyfc.api.ext.pageInfo
import net.keyfc.api.ext.parseId
import net.keyfc.api.model.index.Forum
import net.keyfc.api.model.index.IndexPage
import java.net.HttpCookie

internal object IndexParser {

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

    private const val INDEX_URL = ARCHIVER_URL + "index.aspx"

    /**
     * Fetches and parses the index page.
     *
     * If it is accessible, structured data represented by [IndexPage] will be returned, wrapped in [Result].
     *
     * If parsing fails, this method will return [Result] with the error message and exception.
     */
    suspend fun parse(repoClient: RepoClient, cookies: List<HttpCookie> = emptyList()): Result<IndexPage> {
        return runCatching {
            val document = repoClient.parseUrl(INDEX_URL, cookies)

            // No need to validate as this is a public page

            // Select all category and forum items while maintaining order
            val elements = document.select("div.cateitem, div.forumitem")

            // Process elements in functional style using fold
            val categories = elements.fold(ParseState()) { state, element ->
                processElement(state, element)
            }.flushCategory()

            IndexPage(
                document = document,
                pageInfo = document.pageInfo(),
                categories = categories
            )
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
                    val categoryId = a.attr("href").parseId()
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
                    val id = a.attr("href").parseId()
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
