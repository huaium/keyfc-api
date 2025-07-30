package index

import net.keyfc.api.model.index.Forum
import net.keyfc.api.model.index.IndexPage

fun printIndex(result: Result<IndexPage>) {
    result.fold(
        onSuccess = { indexPage ->
            println("Title: ${indexPage.pageInfo.title}")
            println("Keywords: ${indexPage.pageInfo.keywords}")
            println("Description: ${indexPage.pageInfo.description}")

            indexPage.categories.forEach { cat ->
                println("\n[Category]: ${cat.name} -> ${cat.id}")
                printForumTree(cat.subForums)
            }
        },
        onFailure = { exception ->
            exception.printStackTrace()
        }
    )
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