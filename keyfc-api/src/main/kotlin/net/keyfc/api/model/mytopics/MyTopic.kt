package net.keyfc.api.model.mytopics

import net.keyfc.api.model.search.User
import java.time.LocalDateTime

/**
 * Represents a topic in the user's "My Topics" list.
 */
data class MyTopic(
    val id: String,
    val title: String,
    val url: String,
    val forumName: String,
    val forumId: String,
    val forumUrl: String,
    val lastPostDate: LocalDateTime,
    val lastPostUser: User,
    val isHot: Boolean,
)