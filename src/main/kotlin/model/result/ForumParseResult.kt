package net.keyfc.api.model.result

import net.keyfc.api.model.page.forum.ForumPage

sealed class ForumParseResult {
    data class Success(val forumPage: ForumPage) : ForumParseResult()

    data class PermissionDenied(
        val message: String,
    ) : ForumParseResult()

    data class Failure(val message: String, val exception: Exception) : ForumParseResult()
}