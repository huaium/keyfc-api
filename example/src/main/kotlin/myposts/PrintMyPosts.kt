package myposts

import net.keyfc.api.model.myposts.MyPostsPage
import java.time.format.DateTimeFormatter

fun printMyPosts(result: Result<MyPostsPage>) {
    result.fold(
        onSuccess = { myPostsPage ->
            println("\nTitle: ${myPostsPage.pageInfo.title}")
            println("Keywords: ${myPostsPage.pageInfo.keywords}")
            println("Description: ${myPostsPage.pageInfo.description}")

            println("\nMy Posts Page ${myPostsPage.pagination.currentPage}/${myPostsPage.pagination.totalPages}")

            if (myPostsPage.posts.isEmpty()) {
                println("\nNo posts found.")
            } else {
                println("\nPost List:")
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

                myPostsPage.posts.forEachIndexed { index, post ->
                    println("\n[${index + 1}] ${post.title} ${if (post.isHot) "(HOT)" else ""}")
                    println("ID: ${post.id}")
                    println("Forum: ${post.forumName} (ID: ${post.forumId})")
                    post.lastPostDate?.let { println("Last Post: ${it.format(dateFormatter)} by ${post.lastPostUser.name}") }
                    println("Last Post Date Raw String: ${post.lastPostDateText}")
                    println("URL: ${post.url}")
                }
            }
        },
        onFailure = { exception ->
            exception.printStackTrace()
        }
    )
}