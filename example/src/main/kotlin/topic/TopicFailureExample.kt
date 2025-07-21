package topic

import kotlinx.coroutines.runBlocking
import net.keyfc.api.parser.TopicParser

fun main() {
    val result = runBlocking { TopicParser.parse("x") }

    printTopicResult(result)
}