package inbox

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
        client.fetchInbox()
    }

    when (result) {
        is FetchResult.Failure -> println("Failed to fetch inbox: ${result.message}")
        is FetchResult.WithCookies -> printInbox(result.result)
        is FetchResult.WithoutCookies -> {
            println("Successfully fetched inbox, but without cookies.")
            printInbox(result.result)
        }
    }
}