package topic

import net.keyfc.api.model.result.TopicParseResult

fun printTopicResult(result: TopicParseResult) {
    when (result) {
        is TopicParseResult.Success -> {
            val topicPage = result.topicPage
            println("Title: ${topicPage.pageInfo.title}")
            println("Keywords: ${topicPage.pageInfo.keywords}")
            println("Description: ${topicPage.pageInfo.description}")

            println("\nBreadcrumb Navigation:")
            topicPage.breadcrumbs.forEachIndexed { index, breadcrumb ->
                println("${index + 1}. ${breadcrumb.name} -> ${breadcrumb.link}")
            }

            println("\nParent Forum:")
            println("Name: ${topicPage.parentForum.name}")
            println("Link: ${topicPage.parentForum.link}")

            println("\nCurrent Forum:")
            println("Name: ${topicPage.thisForum.name}")
            println("Link: ${topicPage.thisForum.link}")

            println("\nThis Topic:")
            println("Name: ${topicPage.thisTopic.title}")
            println("Link: ${topicPage.thisTopic.link}")

            println("\nPosts (${topicPage.posts.size} total):")
            topicPage.posts.forEach { post ->
                println("Author: ${post.author}")
                println("Post Time: ${post.postTime}")
                println("Content: ${post.content.take(50)}...")  // Just show first 50 chars
            }

            println("\nPagination Information:")
            println("Current Page: ${topicPage.pagination.currentPage}")
            println("Total Pages: ${topicPage.pagination.totalPages}")
            println("Has Next Page: ${topicPage.pagination.hasNextPage}")
            println("Has Previous Page: ${topicPage.pagination.hasPreviousPage}")
            println("Next Page Link: ${topicPage.pagination.nextPageLink}")
            println("Previous Page Link: ${topicPage.pagination.previousPageLink}")
        }

        is TopicParseResult.PermissionDenied -> {
            println("Required Permission Level: ${result.requiredPermissionLevel}")
            println("Current Identity: ${result.currentIdentity}")
        }

        is TopicParseResult.Failure -> {
            println("Failed to parse topic page: ${result.message}")
            println("Exception: ${result.exception}")
        }
    }
}