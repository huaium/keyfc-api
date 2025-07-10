package net.keyfc.api.model.result

import net.keyfc.api.model.page.Breadcrumb
import net.keyfc.api.model.page.PageInfo
import org.jsoup.nodes.Document

sealed class BaseParseResult {
    data class Success(val pageInfo: PageInfo, val breadcrumbs: List<Breadcrumb>, val doc: Document) : BaseParseResult()

    data class Failure(val message: String, val exception: Exception) : BaseParseResult()
}