package net.keyfc.api.model.topic

import com.fleeksoft.ksoup.nodes.Document
import net.keyfc.api.model.forum.Topic
import net.keyfc.api.model.index.Forum
import net.keyfc.api.model.Breadcrumb
import net.keyfc.api.model.PageInfo
import net.keyfc.api.model.Pagination

data class TopicPage(
    val document: Document,
    val pageInfo: PageInfo,
    val breadcrumbs: List<Breadcrumb>,
    val thisTopic: Topic?,
    val thisForum: Forum?,
    val parentForum: Forum?, // Make sure posts are parsed correctly, even if thisTopic, thisForum and parentForum is null
    val posts: List<Post>,
    val pagination: Pagination
)