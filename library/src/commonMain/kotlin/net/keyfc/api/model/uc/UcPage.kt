package net.keyfc.api.model.uc

import com.fleeksoft.ksoup.nodes.Document
import net.keyfc.api.model.PageInfo

data class UcPage(
    val document: Document,
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