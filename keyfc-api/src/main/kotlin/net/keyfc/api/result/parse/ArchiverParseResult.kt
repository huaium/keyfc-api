package net.keyfc.api.result.parse

import com.fleeksoft.ksoup.nodes.Document
import net.keyfc.api.model.Breadcrumb
import net.keyfc.api.model.PageInfo
import net.keyfc.api.model.Pagination

sealed class ArchiverParseResult {
    data class Success(
        val document: Document,
        val pageInfo: PageInfo,
        val breadcrumbs: List<Breadcrumb>,
        val pagination: Pagination?
    ) : ArchiverParseResult()

    data class Failure(val message: String, val exception: Exception) : ArchiverParseResult()
}