package net.keyfc.api.model.result.parse

import com.fleeksoft.ksoup.nodes.Document
import net.keyfc.api.model.page.PageInfo

sealed class BaseParseResult {
    data class Success(
        val document: Document,
        val pageInfo: PageInfo,
    ) : BaseParseResult()

    data class Failure(val message: String, val exception: Exception) : BaseParseResult()
}