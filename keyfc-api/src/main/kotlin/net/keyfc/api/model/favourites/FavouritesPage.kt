package net.keyfc.api.model.favourites

import com.fleeksoft.ksoup.nodes.Document
import net.keyfc.api.model.PageInfo
import net.keyfc.api.model.Pagination

data class FavouritesPage(
    val document: Document,
    val pageInfo: PageInfo,
    val favourites: List<Favourite>,
    val pagination: Pagination
)