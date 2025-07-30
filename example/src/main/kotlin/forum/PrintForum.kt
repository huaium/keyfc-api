package forum

import net.keyfc.api.model.forum.ForumPage

fun printForum(result: Result<ForumPage>) {
    result.fold(
        onSuccess = { forumPage ->
            println("\nTitle: ${forumPage.pageInfo.title}")
            println("Keywords: ${forumPage.pageInfo.keywords}")
            println("Description: ${forumPage.pageInfo.description}")

            println("\nBreadcrumb Navigation:")
            forumPage.breadcrumbs.forEachIndexed { index, breadcrumb ->
                println("${index + 1}. ${breadcrumb.name} -> ${breadcrumb.link}")
            }

            forumPage.parentForum?.let {
                println("\nParent Forum:")
                println("Name: ${it.name}")
                println("ID: ${it.id}")
            }

            forumPage.thisForum?.let {
                println("\nCurrent Forum:")
                println("Name: ${it.name}")
                println("ID: ${it.id}")
            }

            println("\nTopic List:")
            forumPage.topics.forEachIndexed { index, topic ->
                println("${index + 1}. ${topic.title} (${topic.replyCount} replies) -> ${topic.id}")
            }

            forumPage.pagination.let {
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