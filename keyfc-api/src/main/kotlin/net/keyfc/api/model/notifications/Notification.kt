package net.keyfc.api.model.notifications

import net.keyfc.api.model.User
import java.time.LocalDateTime

data class Notification(
    val content: String,
    val user: User,
    val topicId: String,
    val topicTitle: String,
    val topicUrl: String,
    val reason: String,
    val date: LocalDateTime?,
    val dateText: String
)