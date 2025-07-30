package net.keyfc.api.model.topic

import java.time.LocalDateTime

data class Post(
    val author: String,
    val postTime: LocalDateTime?,
    val postTimeText: String,
    val content: String,
    val postNumber: Int
)