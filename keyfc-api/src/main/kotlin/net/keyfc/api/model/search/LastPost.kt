package net.keyfc.api.model.search

import net.keyfc.api.model.User
import java.time.LocalDateTime

data class LastPost(
    val date: LocalDateTime?,
    val dateText: String,
    val url: String,
    val author: User
)