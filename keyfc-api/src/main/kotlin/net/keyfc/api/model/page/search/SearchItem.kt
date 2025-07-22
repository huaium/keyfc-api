package net.keyfc.api.model.page.search

import java.time.LocalDateTime

/**
 * Represents a single search result item
 */
data class SearchItem(
    val id: Int,
    val title: String,
    val url: String,
    val forum: ForumReference,
    val author: UserReference,
    val postDate: LocalDateTime,
    val replyCount: Int,
    val viewCount: Int,
    val lastPost: LastPost
)

/**
 * Reference to a forum
 */
data class ForumReference(
    val id: Int,
    val name: String,
    val url: String
)

/**
 * Reference to a user
 */
data class UserReference(
    val id: Int,
    val name: String,
    val url: String
)

/**
 * Information about the last post in a topic
 */
data class LastPost(
    val date: LocalDateTime,
    val url: String,
    val author: UserReference
)