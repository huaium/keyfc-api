package mytopics

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
        client.fetchMyTopics()
    }

    when (result) {
        is FetchResult.Failure -> println("Failed to fetch my topics: ${result.message}")
        is FetchResult.WithCookies -> printMyTopics(result.result)
        is FetchResult.WithoutCookies -> {
            println("Successfully fetched my topics, but without cookies.")
            printMyTopics(result.result)
        }
    }
}