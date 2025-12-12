package net.keyfc.api.model.mytopics

import com.fleeksoft.ksoup.nodes.Document
import net.keyfc.api.model.PageInfo
import net.keyfc.api.model.Pagination

data class MyTopicsPage(
    val document: Document,
    val pageInfo: PageInfo,
    val topics: List<MyTopic>,
    val pagination: Pagination
)