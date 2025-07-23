package notifications

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
        client.fetchNotifications()
    }

    when (result) {
        is FetchResult.Failure -> println("Failed to fetch notifications: ${result.message}")
        is FetchResult.WithCookies -> printNotifications(result.result)
        is FetchResult.WithoutCookies -> {
            println("Successfully fetched notifications, but without cookies.")
            printNotifications(result.result)
        }
    }
}