package net.keyfc.api.model.index

import com.fleeksoft.ksoup.nodes.Document
import net.keyfc.api.model.PageInfo

data class IndexPage(
    val document: Document,
    val pageInfo: PageInfo,
    val categories: List<Forum>
)