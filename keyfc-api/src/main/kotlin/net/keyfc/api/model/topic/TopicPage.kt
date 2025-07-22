package net.keyfc.api.model.topic

import net.keyfc.api.model.Breadcrumb
import net.keyfc.api.model.PageInfo
import net.keyfc.api.model.Pagination
import net.keyfc.api.model.forum.Topic
import net.keyfc.api.model.index.Forum

data class TopicPage(
    val pageInfo: PageInfo,
    val breadcrumbs: List<Breadcrumb>,
    val thisTopic: Topic,
    val thisForum: Forum,
    val parentForum: Forum,
    val posts: List<Post>,
    val pagination: Pagination?
)