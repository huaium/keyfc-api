package index

import kotlinx.coroutines.runBlocking
import net.keyfc.api.parser.IndexParser

fun main() {
    val result = runBlocking { IndexParser.parse() }

    printIndexResult(result)
}