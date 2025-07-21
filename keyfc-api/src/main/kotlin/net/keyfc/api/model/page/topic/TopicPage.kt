package net.keyfc.api.model.page.topic

import net.keyfc.api.model.page.Breadcrumb
import net.keyfc.api.model.page.PageInfo
import net.keyfc.api.model.page.Pagination
import net.keyfc.api.model.page.forum.Topic
import net.keyfc.api.model.page.index.Forum

data class TopicPage(
    val pageInfo: PageInfo,
    val breadcrumbs: List<Breadcrumb>,
    val thisTopic: Topic,
    val thisForum: Forum,
    val parentForum: Forum,
    val posts: List<Post>,
    val pagination: Pagination?
)