package net.keyfc.api.model.notifications

import net.keyfc.api.model.search.User
import java.time.LocalDateTime

data class Notification(
    val content: String, // Raw notification content
    val user: User?, // User who triggered the notification (can be null)
    val topicId: String?, // Topic ID if applicable (can be null)
    val topicTitle: String?, // Topic title if applicable (can be null)
    val topicUrl: String?, // Full URL to the topic if applicable (can be null)
    val reason: String?, // Reason for the notification if applicable (can be null)
    val date: LocalDateTime // When the notification was created
)