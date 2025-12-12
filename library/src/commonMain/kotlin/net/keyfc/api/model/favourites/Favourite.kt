package net.keyfc.api.model.favourites

import kotlinx.datetime.LocalDateTime
import net.keyfc.api.model.User

data class Favourite(
    val id: String,
    val title: String,
    val url: String,
    val author: User,
    val date: LocalDateTime?, // May be null if datetime parsing fails
    val dateText: String // As an alternative field if date is null
)