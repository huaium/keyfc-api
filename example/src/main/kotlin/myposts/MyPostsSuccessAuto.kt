package myposts

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
        client.fetchMyPosts()
    }

    when (result) {
        is FetchResult.Failure -> println("Failed to fetch my posts: ${result.message}")
        is FetchResult.WithCookies -> printMyPosts(result.result)
        is FetchResult.WithoutCookies -> {
            println("Successfully fetched my posts, but without cookies.")
            printMyPosts(result.result)
        }
    }
}