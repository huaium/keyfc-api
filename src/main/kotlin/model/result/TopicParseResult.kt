package net.keyfc.api.model.result

import net.keyfc.api.model.page.topic.TopicPage

sealed class TopicParseResult {
    data class Success(val topicPage: TopicPage) : TopicParseResult()

    data class PermissionDenied(
        val requiredPermissionLevel: Int,
        val currentIdentity: String,
    ) : TopicParseResult()

    data class Failure(val message: String, val exception: Exception) : TopicParseResult()
}