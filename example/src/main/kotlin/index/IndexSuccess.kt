package index

import kotlinx.coroutines.runBlocking
import net.keyfc.api.KeyfcClient

fun main() {
    val result = runBlocking { KeyfcClient().use { it.fetchIndex() } }

    printIndex(result)
}