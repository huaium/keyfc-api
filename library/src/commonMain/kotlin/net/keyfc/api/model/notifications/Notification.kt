package net.keyfc.api.model.notifications

import kotlinx.datetime.LocalDateTime
import net.keyfc.api.model.User

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