package net.keyfc.api.model.page.search

import net.keyfc.api.model.page.PageInfo

data class SearchPage(
    val pageInfo: PageInfo,
    val totalResults: Int,
    val currentPage: Int,
    val totalPages: Int,
    val items: List<SearchItem>
)