package net.keyfc.api.ext

import net.keyfc.api.model.page.Pagination
import org.jsoup.nodes.Document

/**
 * Parse pagination information.
 * @return pagination info including page numbers, links, etc.
 */
fun Document.parsePagination(): Pagination {
    val paginationDiv = this.selectFirst("div.pagenumbers")

    // Default values
    var currentPage = 1
    var totalPages = 1
    var nextPageLink: String? = null
    var previousPageLink: String? = null

    if (paginationDiv == null)
        throw RuntimeException("Pagination not found")

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