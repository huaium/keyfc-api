package topic

import net.keyfc.api.parser.TopicParser

fun main() {
    val result = TopicParser.parse("showtopic-70169.aspx")

    printTopicResult(result)
}