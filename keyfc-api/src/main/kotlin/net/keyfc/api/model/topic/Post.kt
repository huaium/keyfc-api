package net.keyfc.api.model.topic

import java.util.Date

data class Post(
    val author: String,
    val postTime: Date,
    val content: String,
    val postNumber: Int
)