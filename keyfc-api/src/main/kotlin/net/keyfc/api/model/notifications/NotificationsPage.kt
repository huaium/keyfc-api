package net.keyfc.api.model.notifications

import net.keyfc.api.model.PageInfo

data class NotificationsPage(
    val pageInfo: PageInfo,
    val notifications: List<Notification>,
    val currentPage: Int,
    val totalPages: Int
)