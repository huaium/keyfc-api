package inbox

import kotlinx.coroutines.runBlocking
import net.keyfc.api.KeyfcClient
import readCredentials

fun main() {
    val (username, password) = readCredentials()

    val result =
        runBlocking {
            KeyfcClient()
                .apply { login(username, password) }
                .use { it.fetchInbox() }
        }

    printInbox(result)
}