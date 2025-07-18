package forum

import net.keyfc.api.parser.ForumParser

fun main() {
    val result = ForumParser.parse("showforum-27.aspx")

    printForumResult(result)
}