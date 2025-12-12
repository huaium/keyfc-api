package mytopics

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import net.keyfc.api.model.mytopics.MyTopicsPage

@OptIn(FormatStringsInDatetimeFormats::class)
fun printMyTopics(result: Result<MyTopicsPage>) {
    result.fold(
        onSuccess = { myTopicsPage ->
            println("\nTitle: ${myTopicsPage.pageInfo.title}")
            println("Keywords: ${myTopicsPage.pageInfo.keywords}")
            println("Description: ${myTopicsPage.pageInfo.description}")

            println("\nMy Topics Page ${myTopicsPage.pagination.currentPage}/${myTopicsPage.pagination.totalPages}")

            if (myTopicsPage.topics.isEmpty()) {
                println("\nNo topics found.")
            } else {
                println("\nTopic List:")
                val dateFormatter = LocalDateTime.Format { byUnicodePattern("yyyy-MM-dd HH:mm") }

                myTopicsPage.topics.forEachIndexed { index, topic ->
                    println("\n[${index + 1}] ${topic.title} ${if (topic.isHot) "(HOT)" else ""}")
                    println("ID: ${topic.id}")
                    println("Forum: ${topic.forumName} (ID: ${topic.forumId})")
                    topic.lastPostDate?.let { println("Last Post: ${it.format(dateFormatter)} by ${topic.lastPostUser.name}") }
                    println("Last Post Date Raw String: ${topic.lastPostDateText}")
                    println("URL: ${topic.url}")
                }
            }
        },
        onFailure = { exception ->
            exception.printStackTrace()
        }
    )
}