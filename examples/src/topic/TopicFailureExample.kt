package topic

import net.keyfc.api.parser.TopicParser

fun main() {
    val result = TopicParser("showtopic-x.aspx").parse()

    printTopicResult(result)
}