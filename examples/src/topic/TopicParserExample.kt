package topic

import net.keyfc.api.parser.TopicParser

fun main() {
    val result = TopicParser("showtopic-70169.aspx").parse()

    printTopicResult(result)
}