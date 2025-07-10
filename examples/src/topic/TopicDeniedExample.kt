package topic

import net.keyfc.api.parser.TopicParser

fun main() {
    val result = TopicParser("showtopic-70026.aspx").parse()

    printTopicResult(result)
}