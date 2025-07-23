package notifications

import net.keyfc.api.result.parse.NotificationsParseResult

fun printNotifications(result: NotificationsParseResult) {
    when (result) {
        is NotificationsParseResult.Success -> {
            val notificationsPage = result.notificationsPage

            println("\nTitle: ${notificationsPage.pageInfo.title}")
            println("Keywords: ${notificationsPage.pageInfo.keywords}")
            println("Description: ${notificationsPage.pageInfo.description}\n")

            println("Notifications Page ${notificationsPage.currentPage}/${notificationsPage.totalPages}")
            println("Total notifications: ${notificationsPage.notifications.size}")

            if (notificationsPage.notifications.isEmpty()) {
                println("\nNo notifications found.")
            } else {
                println("\nNotification List:")
                notificationsPage.notifications.forEachIndexed { index, notification ->
                    println("\n[${index + 1}] ${notification.date}")
                    println("Content: ${notification.content}")

                    notification.user?.let {
                        println("From User: ${it.name} (ID: ${it.id})")
                    }

                    notification.topicTitle?.let {
                        println("Related Topic: $it (ID: ${notification.topicId})")
                        println("Topic URL: ${notification.topicUrl}")
                    }

                    notification.reason?.let {
                        if (it.isNotBlank()) {
                            println("Reason: $it")
                        }
                    }
                }
            }
        }

        is NotificationsParseResult.PermissionDenial -> {
            println("NOTIFICATION ACCESS DENIED")
            println("Message: ${result.message}")
        }

        is NotificationsParseResult.Failure -> {
            println("NOTIFICATION ERROR")
            println("Message: ${result.message}")
            println("Exception: ${result.exception}")
        }
    }
}