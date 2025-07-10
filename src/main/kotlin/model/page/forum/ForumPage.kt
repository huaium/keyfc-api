package net.keyfc.api.model.page.forum

import net.keyfc.api.model.page.Breadcrumb
import net.keyfc.api.model.page.PageInfo
import net.keyfc.api.model.page.Pagination
import net.keyfc.api.model.page.index.Forum

data class ForumPage(
    val pageInfo: PageInfo,
    val breadcrumbs: List<Breadcrumb>,
    val parentForum: Forum,
    val thisForum: Forum,
    val topics: List<Topic>,
    val pagination: Pagination
)