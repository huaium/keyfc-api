package forum

import net.keyfc.api.model.result.ForumParseResult

fun printForumResult(result: ForumParseResult) {
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
            println("Link: ${forumPage.parentForum.link}")

            println("\nCurrent Forum:")
            println("Name: ${forumPage.thisForum.name}")
            println("Link: ${forumPage.thisForum.link}")

            println("\nTopic List:")
            forumPage.topics.forEachIndexed { index, topic ->
                println("${index + 1}. ${topic.title} (${topic.replyCount} replies) -> ${topic.link}")
            }

            println("\nPagination Information:")
            println("Current Page: ${forumPage.pagination.currentPage}")
            println("Total Pages: ${forumPage.pagination.totalPages}")
            println("Has Next Page: ${forumPage.pagination.hasNextPage}")
            println("Has Previous Page: ${forumPage.pagination.hasPreviousPage}")
            println("Next Page Link: ${forumPage.pagination.nextPageLink}")
            println("Previous Page Link: ${forumPage.pagination.previousPageLink}")
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