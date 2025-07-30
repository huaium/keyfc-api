package favourites

import net.keyfc.api.model.favourites.FavouritesPage
import java.time.format.DateTimeFormatter

fun printFavourites(result: Result<FavouritesPage>) {
    result.fold(
        onSuccess = { favouritesPage ->
            println("\nTitle: ${favouritesPage.pageInfo.title}")
            println("Keywords: ${favouritesPage.pageInfo.keywords}")
            println("Description: ${favouritesPage.pageInfo.description}")

            println("\nFavourites Page ${favouritesPage.pagination.currentPage}/${favouritesPage.pagination.totalPages}")

            if (favouritesPage.favourites.isEmpty()) {
                println("\nNo favourites found.")
            } else {
                println("\nFavourite List:")
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                favouritesPage.favourites.forEachIndexed { index, favourite ->
                    println("\n[${index + 1}] ${favourite.title}")
                    println("ID: ${favourite.id}")
                    println("Author: ${favourite.author.name} (ID: ${favourite.author.id})")
                    favourite.date?.let { println("Favourite Date: ${it.format(dateFormatter)}") }
                    println("Favourite Date Raw String: ${favourite.dateText}")
                    println("URL: ${favourite.url}")
                }
            }
        },
        onFailure = { exception ->
            exception.printStackTrace()
        }
    )
}