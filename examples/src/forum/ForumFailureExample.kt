package forum

import net.keyfc.api.parser.ForumParser

fun main() {
    val result = ForumParser("showforum-x.aspx").parse()

    printForumResult(result)
}