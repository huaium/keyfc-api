package net.keyfc.api.result.parse

import net.keyfc.api.model.notification.NotificationPage

sealed class NotificationParseResult {
    data class Success(
        val notificationPage: NotificationPage
    ) : NotificationParseResult()

    data class PermissionDenial(
        val message: String
    ) : NotificationParseResult()

    data class Failure(val message: String, val exception: Exception) : NotificationParseResult()
}