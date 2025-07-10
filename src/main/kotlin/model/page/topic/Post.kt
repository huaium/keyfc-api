package net.keyfc.api.model.page.topic

import java.util.*

data class Post(
    val author: String,
    val postTime: Date,
    val content: String,
    val postNumber: Int
)