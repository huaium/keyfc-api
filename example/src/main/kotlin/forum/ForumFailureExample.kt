package forum

import kotlinx.coroutines.runBlocking
import net.keyfc.api.parser.ForumParser

fun main() {
    val result = runBlocking { ForumParser.parse("x") }

    printForumResult(result)
}