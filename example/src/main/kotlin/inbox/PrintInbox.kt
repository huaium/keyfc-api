package inbox

import net.keyfc.api.model.inbox.InboxPage
import java.time.format.DateTimeFormatter

fun printInbox(result: Result<InboxPage>) {
    result.fold(
        onSuccess = { inboxPage ->
            println("\nTitle: ${inboxPage.pageInfo.title}")
            println("Keywords: ${inboxPage.pageInfo.keywords}")
            println("Description: ${inboxPage.pageInfo.description}")

            println("\nInbox Page ${inboxPage.pagination.currentPage}/${inboxPage.pagination.totalPages}")
            println("Messages: ${inboxPage.messageCount}/${inboxPage.messageLimit}")

            if (inboxPage.messages.isEmpty()) {
                println("\nNo messages found.")
            } else {
                println("\nMessage List:")
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

                inboxPage.messages.forEachIndexed { index, message ->
                    println("\n[${index + 1}] ${message.subject} (${if (message.isRead) "Read" else "Unread"})")
                    println("From: ${message.sender.name} (ID: ${message.sender.id})")
                    message.date?.let { println("Date: ${it.format(dateFormatter)}") }
                    println("Date Raw String: ${message.dateText}")
                    println("Snippet: ${message.snippet}")
                    println("URL: ${message.url}")
                }
            }
        },
        onFailure = { exception ->
            exception.printStackTrace()
        }
    )
}