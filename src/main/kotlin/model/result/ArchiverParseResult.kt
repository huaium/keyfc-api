package net.keyfc.api.model.result

import net.keyfc.api.model.page.Breadcrumb
import net.keyfc.api.model.page.PageInfo
import net.keyfc.api.model.page.Pagination
import org.jsoup.nodes.Document

sealed class ArchiverParseResult {
    data class Success(
        val doc: Document,
        val pageInfo: PageInfo,
        val breadcrumbs: List<Breadcrumb>,
        val pagination: Pagination?
    ) :
        ArchiverParseResult()

    data class Failure(val message: String, val exception: Exception) : ArchiverParseResult()
}