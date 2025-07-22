package net.keyfc.api.model.search

import net.keyfc.api.model.PageInfo

data class SearchPage(
    val pageInfo: PageInfo,
    val totalResults: Int,
    val currentPage: Int,
    val totalPages: Int,
    val items: List<SearchItem>
)