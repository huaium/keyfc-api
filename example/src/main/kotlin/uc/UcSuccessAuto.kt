package uc

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
        client.fetchUc()
    }

    when (result) {
        is FetchResult.Failure -> println("Failed to fetch user center: ${result.message}")
        is FetchResult.WithCookies -> printUc(result.result)
        is FetchResult.WithoutCookies -> {
            println("Successfully fetched user center, but without cookies.")
            printUc(result.result)
        }
    }
}