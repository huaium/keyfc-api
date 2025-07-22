package net.keyfc.api.model.index

import net.keyfc.api.model.PageInfo

data class IndexPage(
    val pageInfo: PageInfo,
    val categories: List<Forum>
)