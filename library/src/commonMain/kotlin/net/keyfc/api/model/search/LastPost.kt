package net.keyfc.api.model.search

import kotlinx.datetime.LocalDateTime
import net.keyfc.api.model.User

data class LastPost(
    val date: LocalDateTime?,
    val dateText: String,
    val url: String,
    val author: User
)