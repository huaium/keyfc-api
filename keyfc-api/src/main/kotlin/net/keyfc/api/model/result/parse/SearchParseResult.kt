package net.keyfc.api.model.result.parse

import net.keyfc.api.model.page.search.SearchPage

sealed class SearchParseResult {
    data class Success(
        val searchResults: SearchPage
    ) : SearchParseResult()

    data class PermissionDenial(
        val message: String,
    ) : SearchParseResult()

    data class Failure(val message: String, val exception: Exception) : SearchParseResult()
}