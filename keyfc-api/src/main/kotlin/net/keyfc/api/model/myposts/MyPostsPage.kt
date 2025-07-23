package net.keyfc.api.model.myposts

import net.keyfc.api.model.PageInfo

/**
 * Represents the "My Posts" page containing the user's posts.
 */
data class MyPostsPage(
    val pageInfo: PageInfo,
    val posts: List<MyPost>,
    val currentPage: Int,
    val totalPages: Int
)