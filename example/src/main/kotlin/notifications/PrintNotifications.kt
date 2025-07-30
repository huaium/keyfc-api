package notifications

import net.keyfc.api.model.notifications.NotificationsPage
import java.time.format.DateTimeFormatter

fun printNotifications(result: Result<NotificationsPage>) {
    result.fold(
        onSuccess = { notificationsPage ->
            println("\nTitle: ${notificationsPage.pageInfo.title}")
            println("Keywords: ${notificationsPage.pageInfo.keywords}")
            println("Description: ${notificationsPage.pageInfo.description}")

            println("\nNotifications Page ${notificationsPage.pagination.currentPage}/${notificationsPage.pagination.totalPages}")
            println("Total notifications: ${notificationsPage.notifications.size}")

            if (notificationsPage.notifications.isEmpty()) {
                println("\nNo notifications found.")
            } else {
                println("\nNotification List:")
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

                notificationsPage.notifications.forEachIndexed { index, notification ->
                    notification.date?.let { println("\n[${index + 1}] ${it.format(dateFormatter)}") }
                    println("Date Raw String: ${notification.dateText}")

                    println("Content: ${notification.content}")

                    notification.user.let {
                        println("From User: ${it.name} (ID: ${it.id})")
                    }

                    notification.topicTitle.let {
                        println("Related Topic: $it (ID: ${notification.topicId})")
                        println("Topic URL: ${notification.topicUrl}")
                    }

                    notification.reason.let {
                        if (it.isNotBlank()) {
                            println("Reason: $it")
                        }
                    }
                }
            }
        },
        onFailure = { exception ->
            exception.printStackTrace()
        }
    )
}