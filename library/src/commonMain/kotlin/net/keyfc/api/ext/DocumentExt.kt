package net.keyfc.api.ext

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import net.keyfc.api.model.Breadcrumb
import net.keyfc.api.model.PageInfo
import net.keyfc.api.model.Pagination

/**
 * Parses basic page information.
 *
 * @return [PageInfo] with page title, keywords and description
 */
internal fun Document.pageInfo(): PageInfo {
    val title = this.title()
    val keywords = this.selectFirst("meta[name=keywords]")?.attr("content") ?: ""
    val description = this.selectFirst("meta[name=description]")?.attr("content") ?: ""

    return PageInfo(title, keywords, description)
}

/**
 * Parses breadcrumb navigation.
 *
 * Notice: for pages with no breadcrumbs, return an empty list.
 *
 * @return a list of [Breadcrumb]
 */
internal fun Document.archiverBreadcrumbs(): List<Breadcrumb> {
    val forumNavLinks = this.selectFirst("div.forumnav")?.select("a")

    return forumNavLinks?.map {
        Breadcrumb(it.text().trim(), it.attr("href"))
    } ?: emptyList()
}

/**
 * Parses pagination information.
 *
 * @return [Pagination] including page numbers, or null if not available
 */
internal fun Document.pagination(isArchiver: Boolean): Pagination {
    if (isArchiver)
        return this.archiverPagination()

    // Using .html() instead of .text() to avoid character merging
    val pagesText = this.selectFirst("div.pages")?.html() ?: ""
    val pageRegex = "(\\d+)/(\\d+)页".toRegex()
    val pageMatch = pageRegex.find(pagesText)
    val currentPage = pageMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
    val totalPages = pageMatch?.groupValues?.get(2)?.toIntOrNull() ?: 1

    return Pagination(
        currentPage = currentPage,
        totalPages = totalPages,
    )
}

private fun Document.archiverPagination(): Pagination {
    val paginationDiv = this.selectFirst("div.pagenumbers")

    // Current page is usually a span element
    val currentPageElement = paginationDiv?.selectFirst("span")
    val currentPage = currentPageElement?.text()?.toIntOrNull() ?: 1

    // Try to determine total pages from the last link
    val lastPageElement = paginationDiv?.select("a")?.last()
    val totalPages = lastPageElement?.text()?.toIntOrNull() ?: 1

    return Pagination(
        currentPage = currentPage,
        totalPages = totalPages,
    )
}

/**
 * Validates the document for errors.
 *
 * @return [Result] indicating success or failure with an error message
 */
internal fun Document.validate(): Result<Unit> {
    val selectorStrategies = listOf(
        "div.msg" to { element: Element -> element.text() },
        "div.msg_inner.error_msg" to { element: Element -> element.selectFirst("p")?.text() },
        "div.msgbox.error_msg" to { element: Element -> element.selectFirst("p")?.text() }
    )

    val errMsg = selectorStrategies.firstNotNullOfOrNull { (selector, extractor) ->
        selectFirst(selector)?.let(extractor)?.takeIf { it.isNotBlank() }
    }

    if (errMsg != null) {
        return Result.failure(RuntimeException(errMsg))
    }

    return Result.success(Unit)
}