package net.keyfc.api.model.inbox

import net.keyfc.api.model.PageInfo

/**
 * Represents an inbox page with messages.
 *
 * @property pageInfo Basic information about the page
 * @property messages List of inbox messages
 * @property currentPage The current page number
 * @property totalPages The total number of pages
 * @property messageCount The total number of messages in the inbox
 * @property messageLimit The maximum number of messages allowed in the inbox
 */
data class InboxPage(
    val pageInfo: PageInfo,
    val messages: List<InboxItem>,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val messageCount: Int = 0,
    val messageLimit: Int = 0
)