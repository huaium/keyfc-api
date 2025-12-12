package topic

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import net.keyfc.api.model.topic.TopicPage

@OptIn(FormatStringsInDatetimeFormats::class)
fun printTopic(result: Result<TopicPage>) {
    result.fold(
        onSuccess = { topicPage ->
            println("\nTitle: ${topicPage.pageInfo.title}")
            println("Keywords: ${topicPage.pageInfo.keywords}")
            println("Description: ${topicPage.pageInfo.description}")

            println("\nBreadcrumb Navigation:")
            topicPage.breadcrumbs.forEachIndexed { index, breadcrumb ->
                println("${index + 1}. ${breadcrumb.name} -> ${breadcrumb.link}")
            }

            topicPage.parentForum?.let {
                println("\nParent Forum:")
                println("Name: ${it.name}")
                println("ID: ${it.id}")
            }

            topicPage.thisForum?.let {
                println("\nCurrent Forum:")
                println("Name: ${it.name}")
                println("ID: ${it.id}")
            }

            topicPage.thisTopic?.let {
                println("\nThis Topic:")
                println("Name: ${it.title}")
                println("ID: ${it.id}")
            }

            val dateFormatter = LocalDateTime.Format { byUnicodePattern("yyyy-MM-dd HH:mm") }
            println("\nPosts (${topicPage.posts.size} total):")
            topicPage.posts.forEach { post ->
                println("Author: ${post.author}")
                post.postTime?.let { println("Post Time: ${it.format(dateFormatter)}") }
                println("Post Time Raw String: ${post.postTimeText}")
                println("Content: ${post.content.take(50)}...")  // Just show first 50 chars
            }

            topicPage.pagination.let {
                println("\nPagination Information:")
                println("Current Page: ${it.currentPage}")
                println("Total Pages: ${it.totalPages}")
            }
        },
        onFailure = { exception ->
            exception.printStackTrace()
        }
    )
}