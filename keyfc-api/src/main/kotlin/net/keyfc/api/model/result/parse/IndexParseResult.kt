package net.keyfc.api.model.result.parse

import net.keyfc.api.model.page.index.IndexPage

sealed class IndexParseResult {
    data class Success(val indexPage: IndexPage) : IndexParseResult()

    data class Failure(val message: String, val exception: Exception) : IndexParseResult()
}