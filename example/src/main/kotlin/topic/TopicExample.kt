package topic

import kotlinx.coroutines.runBlocking
import net.keyfc.api.KeyfcClient
import readCredentials

fun main() {
    val (username, password) = readCredentials()

    val result = runBlocking {
        KeyfcClient()
            .apply { login(username, password) }
            .use { it.fetchTopic("70019") } // Change to "70026" to get a probable exception, depending on your user level
    }

    printTopic(result)
}