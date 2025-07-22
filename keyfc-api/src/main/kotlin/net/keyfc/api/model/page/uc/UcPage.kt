package net.keyfc.api.model.page.uc

import net.keyfc.api.model.page.PageInfo

data class UcPage(
    val pageInfo: PageInfo,
    val username: String,
    val avatar: String,
    val stats: UserStats,
    val signature: String,
    val postCount: Int,
    val digestCount: Int,
    val newMessageCount: Int,
    val newNoticeCount: Int,
    val permissions: UserPermissions?
)