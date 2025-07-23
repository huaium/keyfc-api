package net.keyfc.api.model.favourites

import net.keyfc.api.model.search.User
import java.time.LocalDateTime

/**
 * Represents a favourite item in the user's favourites list.
 */
data class Favourite(
    val id: String,
    val title: String,
    val url: String,
    val author: User,
    val favouriteDate: LocalDateTime,
)