package net.keyfc.api.model.favourites

import net.keyfc.api.model.PageInfo

/**
 * Represents the "Favourites" page containing the user's favourites.
 */
data class FavouritesPage(
    val pageInfo: PageInfo,
    val favourites: List<Favourite>,
    val currentPage: Int,
    val totalPages: Int
)