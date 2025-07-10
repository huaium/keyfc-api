package forum

import net.keyfc.api.parser.ForumParser

fun main() {
    val result = ForumParser("showforum-27.aspx").parse()

    printForumResult(result)
}