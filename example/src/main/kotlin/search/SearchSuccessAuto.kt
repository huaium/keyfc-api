package search

import kotlinx.coroutines.runBlocking
import net.keyfc.api.KeyfcAutoClient
import net.keyfc.api.result.FetchResult

fun main() {
    // Read credentials from stdin
    print("Enter username: ")
    val username = readLine() ?: ""

    print("Enter password: ")
    val password = readLine() ?: ""

    val result = runBlocking { KeyfcAutoClient(username, password).use { it.search("test") } }

    when (result) {
        is FetchResult.Failure -> println("Failed to fetch search results: ${result.message}")
        is FetchResult.WithCookies -> printSearch(result.result)
        is FetchResult.WithoutCookies -> {
            println("Successfully fetched search results, but without cookies.")
            printSearch(result.result)
        }
    }
}