package net.keyfc.api.model.page.search

import net.keyfc.api.model.page.index.Forum
import java.time.LocalDateTime

data class SearchItem(
    val id: String,
    val title: String,
    val url: String,
    val forum: Forum,
    val author: User,
    val postDate: LocalDateTime,
    val replyCount: Int,
    val viewCount: Int,
    val lastPost: LastPost
)