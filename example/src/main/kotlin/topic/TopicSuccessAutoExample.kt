package topic

import kotlinx.coroutines.runBlocking
import net.keyfc.api.KeyfcAutoClient

fun main() {
    // Read credentials from stdin
    print("Enter username: ")
    val username = readLine() ?: ""

    print("Enter password: ")
    val password = readLine() ?: ""

    val result = runBlocking { KeyfcAutoClient(username, password).use { it.fetchTopic("70169") } }

    printTopicResult(result)
}