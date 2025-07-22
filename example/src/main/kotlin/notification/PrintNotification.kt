package notification

import net.keyfc.api.result.parse.NotificationParseResult

fun printNotification(result: NotificationParseResult) {
    when (result) {
        is NotificationParseResult.Success -> {
            val notificationPage = result.notificationPage
            
            println("\nTitle: ${notificationPage.pageInfo.title}")
            println("Keywords: ${notificationPage.pageInfo.keywords}")
            println("Description: ${notificationPage.pageInfo.description}\n")
            
            println("Notifications Page ${notificationPage.currentPage}/${notificationPage.totalPages}")
            println("Total notifications: ${notificationPage.notifications.size}")
            
            if (notificationPage.notifications.isEmpty()) {
                println("\nNo notifications found.")
            } else {
                println("\nNotification List:")
                notificationPage.notifications.forEachIndexed { index, notification ->
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
        
        is NotificationParseResult.PermissionDenial -> {
            println("NOTIFICATION ACCESS DENIED")
            println("Message: ${result.message}")
        }
        
        is NotificationParseResult.Failure -> {
            println("NOTIFICATION ERROR")
            println("Message: ${result.message}")
            println("Exception: ${result.exception}")
        }
    }
}