package forum

import net.keyfc.api.parser.ForumParser

fun main() {
    val result = ForumParser("showforum-52.aspx").parse()

    printForumResult(result)
}