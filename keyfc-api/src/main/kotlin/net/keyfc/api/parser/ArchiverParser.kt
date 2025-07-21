package net.keyfc.api.parser

import com.fleeksoft.ksoup.nodes.Document
import net.keyfc.api.model.page.Breadcrumb
import net.keyfc.api.model.page.Pagination
import net.keyfc.api.model.result.ArchiverParseResult
import net.keyfc.api.model.result.BaseParseResult

internal abstract class ArchiverParser : BaseParser() {

    companion object {
        protected const val ARCHIVER_URL = BASE_URL + "archiver/" // `/` is needed
    }

    protected open val parsePagination = false

    /**
     * Subclasses should call this function to parse the page.
     *
     * It will first parse basic information for archiver page, and then return [ArchiverParseResult].
     */
    protected fun parseArchiver(document: Document): ArchiverParseResult {
        val baseParseResult = super.parseBase(document)

        when (baseParseResult) {
            is BaseParseResult.Success -> {
                val archiverParseResult = try {
                    ArchiverParseResult.Success(
                        document = document,
                        pageInfo = baseParseResult.pageInfo,
                        breadcrumbs = parseBreadcrumbs(document),
                        pagination = if (parsePagination) parsePagination(document) else null
                    )
                } catch (e: Exception) {
                    ArchiverParseResult.Failure(e.message ?: "Unknown error occurred when parsing basic page info.", e)
                }

                return archiverParseResult
            }

            is BaseParseResult.Failure -> {
                return ArchiverParseResult.Failure(baseParseResult.message, baseParseResult.exception)
            }
        }
    }

    /**
     * Parse breadcrumb navigation.
     *
     * Notice: for pages with no breadcrumbs, return an empty list.
     *
     * @return a list of breadcrumb navigation items
     */
    private fun parseBreadcrumbs(doc: Document): List<Breadcrumb> {
        val forumNavLinks = doc.selectFirst("div.forumnav")?.select("a")

        return forumNavLinks?.map {
            Breadcrumb(it.text().trim(), it.attr("href"))
        } ?: emptyList()
    }

    /**
     * Parse pagination information.
     *
     * @return pagination info including page numbers, links, etc.
     */
    private fun parsePagination(doc: Document): Pagination? {
        val paginationDiv = doc.selectFirst("div.pagenumbers")

        // Default values
        var currentPage: Int
        var totalPages = 1
        var nextPageLink: String? = null
        var previousPageLink: String? = null

        if (paginationDiv == null)
            return null

        // Current page is usually a span element
        val currentPageElement = paginationDiv.selectFirst("span")
        currentPage = currentPageElement?.text()?.toIntOrNull() ?: 1

        // Find all page links
        val pageLinks = paginationDiv.select("a")
        if (pageLinks.isNotEmpty()) {
            // Try to determine total pages from the last link
            val lastPageText = pageLinks.last()!!.text()
            totalPages = lastPageText.toIntOrNull() ?: 1

            // Find next page link
            for (link in pageLinks) {
                val pageNum = link.text().toIntOrNull() ?: continue
                if (pageNum == currentPage + 1) {
                    nextPageLink = link.attr("href")
                    break
                }
            }

            // Find previous page link
            for (link in pageLinks) {
                val pageNum = link.text().toIntOrNull() ?: continue
                if (pageNum == currentPage - 1) {
                    previousPageLink = link.attr("href")
                    break
                }
            }
        }

        return Pagination(
            currentPage = currentPage,
            totalPages = totalPages,
            hasNextPage = currentPage < totalPages,
            hasPreviousPage = currentPage > 1,
            nextPageLink = nextPageLink,
            previousPageLink = previousPageLink
        )
    }
}