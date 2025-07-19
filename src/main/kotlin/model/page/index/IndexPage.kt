package net.keyfc.api.model.page.index

import net.keyfc.api.model.page.PageInfo

data class IndexPage(
    val pageInfo: PageInfo,
    val categories: List<Forum>
)