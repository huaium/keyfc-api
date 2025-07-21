package topic

import kotlinx.coroutines.runBlocking
import net.keyfc.api.KeyfcClient

fun main() {
    val result = runBlocking { KeyfcClient().use { it.fetchTopic("70169") } }

    printTopicResult(result)
}