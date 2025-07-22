package index

import net.keyfc.api.model.index.Forum
import net.keyfc.api.result.parse.IndexParseResult

fun printIndex(result: IndexParseResult) {
    when (result) {
        is IndexParseResult.Success -> {
            val indexPage = result.indexPage
            println("Title: ${indexPage.pageInfo.title}")
            println("Keywords: ${indexPage.pageInfo.keywords}")
            println("Description: ${indexPage.pageInfo.description}")

            indexPage.categories.forEach { cat ->
                println("\n[Category]: ${cat.name} -> ${cat.id}")
                printForumTree(cat.subForums)
            }
        }

        is IndexParseResult.Failure -> {
            println("Failed to parse index page: ${result.message}")
            println("Exception: ${result.exception}")
        }
    }
}

/**
 * Prints the forum tree structure to console recursively.
 */
private fun printForumTree(forums: List<Forum>, level: Int = 1) {
    forums.forEach {
        println("${"-".repeat(level)} ${it.name} -> ${it.id}")
        printForumTree(it.subForums, level + 1)
    }
}