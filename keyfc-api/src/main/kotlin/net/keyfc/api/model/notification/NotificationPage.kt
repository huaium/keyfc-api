package net.keyfc.api.model.notification

import net.keyfc.api.model.PageInfo

data class NotificationPage(
    val pageInfo: PageInfo,
    val notifications: List<NotificationItem>,
    val currentPage: Int,
    val totalPages: Int
)