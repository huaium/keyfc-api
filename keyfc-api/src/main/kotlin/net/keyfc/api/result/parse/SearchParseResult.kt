package net.keyfc.api.result.parse

import net.keyfc.api.model.search.SearchPage

sealed class SearchParseResult {
    data class Success(
        val searchPage: SearchPage
    ) : SearchParseResult()

    data class PermissionDenial(
        val message: String,
    ) : SearchParseResult()

    data class Failure(val message: String, val exception: Exception) : SearchParseResult()
}