package net.keyfc.api.model.mytopics

import net.keyfc.api.model.PageInfo

/**
 * Represents the "My Topics" page containing the user's topics.
 */
data class MyTopicsPage(
    val pageInfo: PageInfo,
    val topics: List<MyTopic>,
    val currentPage: Int,
    val totalPages: Int
)