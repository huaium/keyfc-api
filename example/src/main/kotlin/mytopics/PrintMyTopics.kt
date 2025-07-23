package mytopics

import net.keyfc.api.result.parse.MyTopicsParseResult
import java.time.format.DateTimeFormatter

fun printMyTopics(result: MyTopicsParseResult) {
    when (result) {
        is MyTopicsParseResult.Success -> {
            val myTopicsPage = result.myTopicsPage

            println("\nTitle: ${myTopicsPage.pageInfo.title}")
            println("Keywords: ${myTopicsPage.pageInfo.keywords}")
            println("Description: ${myTopicsPage.pageInfo.description}\n")

            println("My Topics Page ${myTopicsPage.currentPage}/${myTopicsPage.totalPages}")

            if (myTopicsPage.topics.isEmpty()) {
                println("\nNo topics found.")
            } else {
                println("\nTopic List:")
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

                myTopicsPage.topics.forEachIndexed { index, topic ->
                    println("\n[${index + 1}] ${topic.title} ${if (topic.isHot) "(HOT)" else ""}")
                    println("ID: ${topic.id}")
                    println("Forum: ${topic.forumName} (ID: ${topic.forumId})")
                    println("Last Post: ${topic.lastPostDate.format(dateFormatter)} by ${topic.lastPostUser.name}")
                    println("URL: ${topic.url}")
                }
            }
        }

        is MyTopicsParseResult.PermissionDenial -> {
            println("MY TOPICS ACCESS DENIED")
            println("Message: ${result.message}")
        }

        is MyTopicsParseResult.Failure -> {
            println("MY TOPICS ERROR")
            println("Message: ${result.message}")
            println("Exception: ${result.exception}")
        }
    }
}