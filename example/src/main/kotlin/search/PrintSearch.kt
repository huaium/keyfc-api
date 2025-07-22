package search

import net.keyfc.api.model.search.SearchItem
import net.keyfc.api.result.parse.SearchParseResult
import java.time.format.DateTimeFormatter

fun printSearch(result: SearchParseResult) {
    when (result) {
        is SearchParseResult.Success -> {
            val searchResults = result.searchPage

            println("\nTitle: ${searchResults.pageInfo.title}")
            println("Keywords: ${searchResults.pageInfo.keywords}")
            println("Description: ${searchResults.pageInfo.description}")
            println("Total Results: ${searchResults.totalResults}")
            println("Page: ${searchResults.currentPage}/${searchResults.totalPages}\n")

            if (searchResults.items.isEmpty()) {
                println("No results found.")
            } else {
                searchResults.items.forEachIndexed { index, item ->
                    printSearchItem(index + 1, item)
                }
            }
        }

        is SearchParseResult.PermissionDenial -> {
            println("SEARCH PERMISSION DENIED")
            println("Message: ${result.message}")
        }

        is SearchParseResult.Failure -> {
            println("SEARCH ERROR")
            println("Message: ${result.message}")
            println("Exception: ${result.exception}")
        }
    }
}

private fun printSearchItem(index: Int, item: SearchItem) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    println("RESULT #$index")
    println("Title: ${item.title}")
    println("Topic ID: ${item.id}")
    println("URL: ${item.url}")

    println("Forum: ${item.forum.name} (ID: ${item.forum.id})")
    println("Author: ${item.author.name} (ID: ${item.author.id})")
    println("Posted: ${item.postDate.format(dateFormatter)}")
    println("Stats: ${item.replyCount} replies, ${item.viewCount} views")

    println("Last Post:")
    println("Date: ${item.lastPost.date.format(dateFormatter)}")
    println("By: ${item.lastPost.author.name} (ID: ${item.lastPost.author.id})")
    println("URL: ${item.lastPost.url}")
}