package net.keyfc.api.model.forum

import com.fleeksoft.ksoup.nodes.Document
import net.keyfc.api.model.Breadcrumb
import net.keyfc.api.model.PageInfo
import net.keyfc.api.model.Pagination
import net.keyfc.api.model.index.Forum

data class ForumPage(
    val document: Document,
    val pageInfo: PageInfo,
    val breadcrumbs: List<Breadcrumb>,
    val parentForum: Forum?, // May be null if breadcrumbs is empty
    val thisForum: Forum?, // Make sure topics are parsed correctly, even if thisForum and parentForum is null
    val topics: List<Topic>,
    val pagination: Pagination
)