package favourites

import net.keyfc.api.result.parse.FavouritesParseResult
import java.time.format.DateTimeFormatter

fun printFavourites(result: FavouritesParseResult) {
    when (result) {
        is FavouritesParseResult.Success -> {
            val favouritesPage = result.favouritesPage

            println("\nTitle: ${favouritesPage.pageInfo.title}")
            println("Keywords: ${favouritesPage.pageInfo.keywords}")
            println("Description: ${favouritesPage.pageInfo.description}\n")

            println("Favourites Page ${favouritesPage.currentPage}/${favouritesPage.totalPages}")

            if (favouritesPage.favourites.isEmpty()) {
                println("\nNo favourites found.")
            } else {
                println("\nFavourite List:")
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                favouritesPage.favourites.forEachIndexed { index, favourite ->
                    println("\n[${index + 1}] ${favourite.title}")
                    println("ID: ${favourite.id}")
                    println("Author: ${favourite.author.name} (ID: ${favourite.author.id})")
                    println("Favourite Date: ${favourite.favouriteDate.format(dateFormatter)}")
                    println("URL: ${favourite.url}")
                }
            }
        }

        is FavouritesParseResult.PermissionDenial -> {
            println("FAVOURITES ACCESS DENIED")
            println("Message: ${result.message}")
        }

        is FavouritesParseResult.Failure -> {
            println("FAVOURITES ERROR")
            println("Message: ${result.message}")
            println("Exception: ${result.exception}")
        }
    }
}