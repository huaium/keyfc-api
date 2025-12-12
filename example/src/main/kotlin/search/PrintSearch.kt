package search

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import net.keyfc.api.model.search.SearchItem
import net.keyfc.api.model.search.SearchPage

fun printSearch(result: Result<SearchPage>) {
    result.fold(
        onSuccess = { searchPage ->
            println("\nTitle: ${searchPage.pageInfo.title}")
            println("Keywords: ${searchPage.pageInfo.keywords}")
            println("Description: ${searchPage.pageInfo.description}")

            println("\nTotal Results: ${searchPage.totalResults}")
            println("Page: ${searchPage.pagination.currentPage}/${searchPage.pagination.totalPages}")

            if (searchPage.items.isEmpty()) {
                println("\nNo results found.")
            } else {
                searchPage.items.forEachIndexed { index, item ->
                    printSearchItem(index + 1, item)
                }
            }
        },
        onFailure = { exception ->
            exception.printStackTrace()
        }
    )
}

@OptIn(FormatStringsInDatetimeFormats::class)
private fun printSearchItem(index: Int, item: SearchItem) {
    val dateFormatter = LocalDateTime.Format { byUnicodePattern("yyyy-MM-dd HH:mm") }

    println("\nRESULT #$index")
    println("Title: ${item.title}")
    println("Topic ID: ${item.id}")
    println("URL: ${item.url}")

    println("Forum: ${item.forum.name} (ID: ${item.forum.id})")
    println("Author: ${item.author.name} (ID: ${item.author.id})")

    item.postDate?.let { println("Posted: ${it.format(dateFormatter)}") }
    println("Posted Raw String: ${item.postDateText}")
    println("Stats: ${item.replyCount} replies, ${item.viewCount} views")

    println("Last Post:")
    item.lastPost.date?.let { println("Date: ${it.format(dateFormatter)}") }
    println("Date Raw String: ${item.lastPost.dateText}")
    println("By: ${item.lastPost.author.name} (ID: ${item.lastPost.author.id})")
    println("URL: ${item.lastPost.url}")
}