package net.keyfc.api.model.search

import kotlinx.datetime.LocalDateTime
import net.keyfc.api.model.index.Forum
import net.keyfc.api.model.User

data class SearchItem(
    val id: String,
    val title: String,
    val url: String,
    val forum: Forum,
    val author: User,
    val postDate: LocalDateTime?,
    val postDateText: String,
    val replyCount: Int,
    val viewCount: Int,
    val lastPost: LastPost
)