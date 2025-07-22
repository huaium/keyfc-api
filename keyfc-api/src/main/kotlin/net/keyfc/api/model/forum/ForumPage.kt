package net.keyfc.api.model.forum

import net.keyfc.api.model.Breadcrumb
import net.keyfc.api.model.PageInfo
import net.keyfc.api.model.Pagination
import net.keyfc.api.model.index.Forum

data class ForumPage(
    val pageInfo: PageInfo,
    val breadcrumbs: List<Breadcrumb>,
    val parentForum: Forum,
    val thisForum: Forum,
    val topics: List<Topic>,
    val pagination: Pagination?
)