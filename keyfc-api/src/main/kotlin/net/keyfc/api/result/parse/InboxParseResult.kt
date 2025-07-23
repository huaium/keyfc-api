package net.keyfc.api.result.parse

import net.keyfc.api.model.inbox.InboxPage

sealed class InboxParseResult {
    data class Success(
        val inboxPage: InboxPage
    ) : InboxParseResult()

    data class PermissionDenial(
        val message: String
    ) : InboxParseResult()

    data class Failure(val message: String, val exception: Exception) : InboxParseResult()
}