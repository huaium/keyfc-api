package net.keyfc.api.model.inbox

import com.fleeksoft.ksoup.nodes.Document
import net.keyfc.api.model.PageInfo
import net.keyfc.api.model.Pagination

data class InboxPage(
    val document: Document,
    val pageInfo: PageInfo,
    val messages: List<InboxItem>,
    val pagination: Pagination,
    val messageCount: Int,
    val messageLimit: Int
)