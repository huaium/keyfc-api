package net.keyfc.api.model.page.search

/**
 * Represents search results page
 */
data class SearchPage(
    val totalResults: Int,
    val currentPage: Int,
    val totalPages: Int,
    val items: List<SearchItem>
)