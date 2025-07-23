package net.keyfc.api.model.myposts

import net.keyfc.api.model.search.User
import java.time.LocalDateTime

/**
 * Represents a post in the user's "My Posts" list.
 */
data class MyPost(
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