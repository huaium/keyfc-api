package forum

import net.keyfc.api.result.parse.ForumParseResult

fun printForum(result: ForumParseResult) {
    when (result) {
        is ForumParseResult.Success -> {
            val forumPage = result.forumPage
            println("Title: ${forumPage.pageInfo.title}")
            println("Keywords: ${forumPage.pageInfo.keywords}")
            println("Description: ${forumPage.pageInfo.description}")

            println("\nBreadcrumb Navigation:")
            forumPage.breadcrumbs.forEachIndexed { index, breadcrumb ->
                println("${index + 1}. ${breadcrumb.name} -> ${breadcrumb.link}")
            }

            println("\nParent Forum:")
            println("Name: ${forumPage.parentForum.name}")
            println("ID: ${forumPage.parentForum.id}")

            println("\nCurrent Forum:")
            println("Name: ${forumPage.thisForum.name}")
            println("ID: ${forumPage.thisForum.id}")

            println("\nTopic List:")
            forumPage.topics.forEachIndexed { index, topic ->
                println("${index + 1}. ${topic.title} (${topic.replyCount} replies) -> ${topic.id}")
            }

            forumPage.pagination?.let {
                println("\nPagination Information:")
                println("Current Page: ${it.currentPage}")
                println("Total Pages: ${it.totalPages}")
                println("Has Next Page: ${it.hasNextPage}")
                println("Has Previous Page: ${it.hasPreviousPage}")
                println("Next Page Link: ${it.nextPageLink}")
                println("Previous Page Link: ${it.previousPageLink}")
            }
        }

        is ForumParseResult.PermissionDenial -> {
            println("Permission Denied: ${result.message}")
        }

        is ForumParseResult.UnknownDenial -> {
            println("Unknown Denial: ${result.message}")
        }

        is ForumParseResult.Failure -> {
            println("Failed to parse forum page: ${result.message}")
            println("Exception: ${result.exception}")
        }
    }
}