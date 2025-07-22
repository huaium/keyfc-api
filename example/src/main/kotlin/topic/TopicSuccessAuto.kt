package topic

import kotlinx.coroutines.runBlocking
import net.keyfc.api.KeyfcAutoClient
import net.keyfc.api.model.result.FetchResult

fun main() {
    // Read credentials from stdin
    print("Enter username: ")
    val username = readLine() ?: ""

    print("Enter password: ")
    val password = readLine() ?: ""

    val client = KeyfcAutoClient(username, password)

    val result = runBlocking {
        client.fetchTopic("70169")
    }

    when (result) {
        is FetchResult.Failure -> println("Failed to fetch topic: ${result.message}")
        is FetchResult.WithCookies -> printTopic(result.result)
        is FetchResult.WithoutCookies -> {
            println("Successfully fetched topic, but without cookies.")
            printTopic(result.result)
        }
    }
}