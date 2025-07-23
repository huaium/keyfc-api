package net.keyfc.api.model.inbox

import net.keyfc.api.model.search.User
import java.time.LocalDateTime

/**
 * Represents an inbox message item.
 *
 * @property id The unique identifier of the message
 * @property sender The sender information
 * @property subject The subject of the message
 * @property snippet A snippet/preview of the message content
 * @property date The date when the message was sent
 * @property isRead Whether the message has been read
 * @property url The URL to view the full message
 */
data class InboxItem(
    val id: String,
    val sender: User,
    val subject: String,
    val snippet: String,
    val date: LocalDateTime,
    val isRead: Boolean,
    val url: String
)