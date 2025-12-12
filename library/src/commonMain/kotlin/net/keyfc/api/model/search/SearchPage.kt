package net.keyfc.api.model.search

import com.fleeksoft.ksoup.nodes.Document
import net.keyfc.api.model.PageInfo
import net.keyfc.api.model.Pagination

data class SearchPage(
    val document: Document,
    val pageInfo: PageInfo,
    val totalResults: Int,
    val pagination: Pagination,
    val items: List<SearchItem>
)