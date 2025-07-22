package topic

import net.keyfc.api.model.result.parse.TopicParseResult

fun printTopic(result: TopicParseResult) {
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
            println("ID: ${topicPage.parentForum.id}")

            println("\nCurrent Forum:")
            println("Name: ${topicPage.thisForum.name}")
            println("ID: ${topicPage.thisForum.id}")

            println("\nThis Topic:")
            println("Name: ${topicPage.thisTopic.title}")
            println("ID: ${topicPage.thisTopic.id}")

            println("\nPosts (${topicPage.posts.size} total):")
            topicPage.posts.forEach { post ->
                println("Author: ${post.author}")
                println("Post Time: ${post.postTime}")
                println("Content: ${post.content.take(50)}...")  // Just show first 50 chars
            }

            topicPage.pagination?.let {
                println("\nPagination Information:")
                println("Current Page: ${it.currentPage}")
                println("Total Pages: ${it.totalPages}")
                println("Has Next Page: ${it.hasNextPage}")
                println("Has Previous Page: ${it.hasPreviousPage}")
                println("Next Page Link: ${it.nextPageLink}")
                println("Previous Page Link: ${it.previousPageLink}")
            }
        }

        is TopicParseResult.PermissionDenial -> {
            println("Required Permission Level: ${result.requiredPermissionLevel}")
            println("Current Identity: ${result.currentIdentity}")
        }

        is TopicParseResult.UnknownDenial -> {
            println("Unknown Denial: ${result.message}")
        }

        is TopicParseResult.Failure -> {
            println("Failed to parse topic page: ${result.message}")
            println("Exception: ${result.exception}")
        }
    }
}