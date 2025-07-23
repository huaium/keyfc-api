package favourites

import kotlinx.coroutines.runBlocking
import net.keyfc.api.KeyfcAutoClient
import net.keyfc.api.result.FetchResult

fun main() {
    // Read credentials from stdin
    print("Enter username: ")
    val username = readLine() ?: ""

    print("Enter password: ")
    val password = readLine() ?: ""

    val client = KeyfcAutoClient(username, password)

    val result = runBlocking {
        client.fetchFavourites()
    }

    when (result) {
        is FetchResult.Failure -> println("Failed to fetch favourites: ${result.message}")
        is FetchResult.WithCookies -> printFavourites(result.result)
        is FetchResult.WithoutCookies -> {
            println("Successfully fetched favourites, but without cookies.")
            printFavourites(result.result)
        }
    }
}