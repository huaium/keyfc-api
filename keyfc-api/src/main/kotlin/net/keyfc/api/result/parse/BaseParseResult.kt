package net.keyfc.api.result.parse

import com.fleeksoft.ksoup.nodes.Document
import net.keyfc.api.model.PageInfo

sealed class BaseParseResult {
    data class Success(
        val document: Document,
        val pageInfo: PageInfo,
    ) : BaseParseResult()

    data class Failure(val message: String, val exception: Exception) : BaseParseResult()
}