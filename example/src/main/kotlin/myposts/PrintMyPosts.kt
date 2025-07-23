package myposts

import net.keyfc.api.result.parse.MyPostsParseResult
import java.time.format.DateTimeFormatter

fun printMyPosts(result: MyPostsParseResult) {
    when (result) {
        is MyPostsParseResult.Success -> {
            val myPostsPage = result.myPostsPage

            println("\nTitle: ${myPostsPage.pageInfo.title}")
            println("Keywords: ${myPostsPage.pageInfo.keywords}")
            println("Description: ${myPostsPage.pageInfo.description}\n")

            println("My Posts Page ${myPostsPage.currentPage}/${myPostsPage.totalPages}")

            if (myPostsPage.posts.isEmpty()) {
                println("\nNo posts found.")
            } else {
                println("\nPost List:")
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

                myPostsPage.posts.forEachIndexed { index, post ->
                    println("\n[${index + 1}] ${post.title} ${if (post.isHot) "(HOT)" else ""}")
                    println("ID: ${post.id}")
                    println("Forum: ${post.forumName} (ID: ${post.forumId})")
                    println("Last Post: ${post.lastPostDate.format(dateFormatter)} by ${post.lastPostUser.name}")
                    println("URL: ${post.url}")
                }
            }
        }

        is MyPostsParseResult.PermissionDenial -> {
            println("MY POSTS ACCESS DENIED")
            println("Message: ${result.message}")
        }

        is MyPostsParseResult.Failure -> {
            println("MY POSTS ERROR")
            println("Message: ${result.message}")
            println("Exception: ${result.exception}")
        }
    }
}