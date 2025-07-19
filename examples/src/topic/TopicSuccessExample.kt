package topic

import net.keyfc.api.parser.TopicParser

fun main() {
    val result = TopicParser.parse("70169")

    printTopicResult(result)
}