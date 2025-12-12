package net.keyfc.api.model.myposts

import com.fleeksoft.ksoup.nodes.Document
import net.keyfc.api.model.PageInfo
import net.keyfc.api.model.Pagination

data class MyPostsPage(
    val document: Document,
    val pageInfo: PageInfo,
    val posts: List<MyPost>,
    val pagination: Pagination
)