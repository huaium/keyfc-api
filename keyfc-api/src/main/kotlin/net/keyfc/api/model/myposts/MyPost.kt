package net.keyfc.api.model.myposts

import net.keyfc.api.model.User
import java.time.LocalDateTime

data class MyPost(
    val id: String,
    val title: String,
    val url: String,
    val forumName: String,
    val forumId: String,
    val forumUrl: String,
    val lastPostDate: LocalDateTime?,
    val lastPostDateText: String,
    val lastPostUser: User,
    val isHot: Boolean,
)