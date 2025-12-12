package net.keyfc.api.model.notifications

import com.fleeksoft.ksoup.nodes.Document
import net.keyfc.api.model.PageInfo
import net.keyfc.api.model.Pagination

data class NotificationsPage(
    val document: Document,
    val pageInfo: PageInfo,
    val notifications: List<Notification>,
    val pagination: Pagination
)