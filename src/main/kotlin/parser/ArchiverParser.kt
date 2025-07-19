package net.keyfc.api.parser

import net.keyfc.api.ApiApplication
import net.keyfc.api.ext.doc
import net.keyfc.api.ext.plus
import net.keyfc.api.model.page.Breadcrumb
import net.keyfc.api.model.page.PageInfo
import net.keyfc.api.model.page.Pagination
import net.keyfc.api.model.result.ArchiverParseResult
import org.jsoup.nodes.Document
import java.net.HttpCookie

abstract class ArchiverParser<T> {

    protected open val parsePagination = false

    /**
     * Whether this parser uses the archiver prefix in URLs.
     * Override in subclasses to change default behavior.
     */
    protected open fun parse(relativeUrl: String, cookies: List<HttpCookie>): T {
        validateUrl(relativeUrl)

        val url = ApiApplication.archiverUrl + relativeUrl

        val archiverParseResult = try {
            val doc = url.doc(cookies)
            ArchiverParseResult.Success(
                doc = doc,
                pageInfo = parsePageInfo(doc),
                breadcrumbs = parseBreadcrumbs(doc),
                pagination = if (parsePagination) parsePagination(doc) else null
            )
        } catch (e: Exception) {
            ArchiverParseResult.Failure(e.message ?: "Unknown error occurred when parsing basic page info.", e)
        }

        return parse(archiverParseResult)
    }

    protected abstract fun validateUrl(relativeUrl: String)

    protected abstract fun parse(archiverParseResult: ArchiverParseResult): T

    /**
     * Parse basic page information.
     *
     * @return [PageInfo] with page title, keywords and description
     */
    private fun parsePageInfo(doc: Document): PageInfo {
        val title = doc.title()
        val keywords = doc.selectFirst("meta[name=keywords]")?.attr("content") ?: ""
        val description = doc.selectFirst("meta[name=description]")?.attr("content") ?: ""

        return PageInfo(title, keywords, description)
    }

    /**
     * Parse breadcrumb navigation.
     *
     * Notice: for pages with no breadcrumbs, return an empty list.
     *
     * @return a list of breadcrumb navigation items
     */
    private fun parseBreadcrumbs(doc: Document): List<Breadcrumb> {
        val forumNav = doc.selectFirst("div.forumnav")
        return forumNav?.select("a")?.map {
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
        var currentPage = 1
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