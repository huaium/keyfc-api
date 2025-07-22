package net.keyfc.api.result.parse

import net.keyfc.api.model.forum.ForumPage

sealed class ForumParseResult {
    data class Success(val forumPage: ForumPage) : ForumParseResult()

    data class PermissionDenial(
        val message: String,
    ) : ForumParseResult()

    data class UnknownDenial(
        val message: String,
    ) : ForumParseResult()

    data class Failure(val message: String, val exception: Exception) : ForumParseResult()
}