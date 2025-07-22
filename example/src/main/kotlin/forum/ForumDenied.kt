package forum

import kotlinx.coroutines.runBlocking
import net.keyfc.api.KeyfcClient

fun main() {
    val result = runBlocking { KeyfcClient().use { it.fetchForum("27") } }

    printForum(result)
}