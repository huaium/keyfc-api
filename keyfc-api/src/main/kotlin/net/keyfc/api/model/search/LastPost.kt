package net.keyfc.api.model.search

import java.time.LocalDateTime

data class LastPost(
    val date: LocalDateTime,
    val url: String,
    val author: User
)