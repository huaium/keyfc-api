package net.keyfc.api.model.inbox

import net.keyfc.api.model.User
import java.time.LocalDateTime

data class InboxItem(
    val id: String,
    val sender: User,
    val subject: String,
    val snippet: String,
    val date: LocalDateTime?,
    val dateText: String, // As an alternative field if date is null
    val isRead: Boolean,
    val url: String
)