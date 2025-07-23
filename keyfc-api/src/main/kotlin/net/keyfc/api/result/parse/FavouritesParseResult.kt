package net.keyfc.api.result.parse

import net.keyfc.api.model.favourites.FavouritesPage

sealed class FavouritesParseResult {
    data class Success(
        val favouritesPage: FavouritesPage
    ) : FavouritesParseResult()

    data class PermissionDenial(
        val message: String
    ) : FavouritesParseResult()

    data class Failure(val message: String, val exception: Exception) : FavouritesParseResult()
}