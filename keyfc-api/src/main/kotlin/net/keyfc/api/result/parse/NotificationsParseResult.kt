package net.keyfc.api.result.parse

import net.keyfc.api.model.notifications.NotificationsPage

sealed class NotificationsParseResult {
    data class Success(
        val notificationsPage: NotificationsPage
    ) : NotificationsParseResult()

    data class PermissionDenial(
        val message: String
    ) : NotificationsParseResult()

    data class Failure(val message: String, val exception: Exception) : NotificationsParseResult()
}