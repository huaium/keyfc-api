package forum

import net.keyfc.api.parser.ForumParser

fun main() {
    val result = ForumParser.parse("x")

    printForumResult(result)
}