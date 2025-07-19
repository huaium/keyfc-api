package net.keyfc.api.parser

import net.keyfc.api.ApiApplication
import net.keyfc.api.model.page.Breadcrumb
import net.keyfc.api.model.page.PageInfo
import net.keyfc.api.model.page.Pagination
import net.keyfc.api.model.result.ArchiverParseResult
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.HttpCookie
import java.net.URI

abstract class ArchiverParser<T> {

    companion object {
        val archiverUri = URI("https://keyfc.net/bbs/archiver/") // `/` is needed for concatenation
    }

    protected open val parsePagination = false

    protected fun uriToDocument(uri: URI, cookies: List<HttpCookie>): Document =
        Jsoup.connect(uri.toString())
            .userAgent(ApiApplication.USER_AGENT)
            .cookies(cookies.associate { it.name to it.value })
            .get()

    /**
     * Subclasses should call this function to parse the page.
     *
     * It will first parse basic information, and then call [parseAfter] overwritten by the subclass.
     */
    protected fun parse(document: Document): T {
        val archiverParseResult = try {
            ArchiverParseResult.Success(
                document = document,
                pageInfo = parsePageInfo(document),
                breadcrumbs = parseBreadcrumbs(document),
                pagination = if (parsePagination) parsePagination(document) else null
            )
        } catch (e: Exception) {
            ArchiverParseResult.Failure(e.message ?: "Unknown error occurred when parsing basic page info.", e)
        }

        return parseAfter(archiverParseResult)
    }

    protected abstract fun parseAfter(archiverParseResult: ArchiverParseResult): T

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