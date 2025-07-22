package net.keyfc.api.result.parse

import net.keyfc.api.model.topic.TopicPage

sealed class TopicParseResult {
    data class Success(val topicPage: TopicPage) : TopicParseResult()

    data class PermissionDenial(
        val requiredPermissionLevel: Int,
        val currentIdentity: String,
    ) : TopicParseResult()

    data class UnknownDenial(
        val message: String,
    ) : TopicParseResult()

    data class Failure(val message: String, val exception: Exception) : TopicParseResult()
}