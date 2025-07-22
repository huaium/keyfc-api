package forum

import kotlinx.coroutines.runBlocking
import net.keyfc.api.KeyfcAutoClient
import net.keyfc.api.result.FetchResult

fun main() {
    // Read credentials from stdin
    print("Enter username: ")
    val username = readLine() ?: ""

    print("Enter password: ")
    val password = readLine() ?: ""

    val result = runBlocking { KeyfcAutoClient(username, password).use { it.fetchForum("52") } }

    when (result) {
        is FetchResult.Failure -> println("Failed to fetch forum: ${result.message}")
        is FetchResult.WithCookies -> printForum(result.result)
        is FetchResult.WithoutCookies -> {
            println("Successfully fetched forum, but without cookies.")
            printForum(result.result)
        }
    }
}