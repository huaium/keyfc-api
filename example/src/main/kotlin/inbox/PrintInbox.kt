package inbox

import net.keyfc.api.result.parse.InboxParseResult
import java.time.format.DateTimeFormatter

fun printInbox(result: InboxParseResult) {
    when (result) {
        is InboxParseResult.Success -> {
            val inboxPage = result.inboxPage

            println("\nTitle: ${inboxPage.pageInfo.title}")
            println("Keywords: ${inboxPage.pageInfo.keywords}")
            println("Description: ${inboxPage.pageInfo.description}\n")

            println("Inbox Page ${inboxPage.currentPage}/${inboxPage.totalPages}")
            println("Messages: ${inboxPage.messageCount}/${inboxPage.messageLimit}")

            if (inboxPage.messages.isEmpty()) {
                println("\nNo messages found.")
            } else {
                println("\nMessage List:")
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

                inboxPage.messages.forEachIndexed { index, message ->
                    println("\n[${index + 1}] ${message.subject} (${if (message.isRead) "Read" else "Unread"})")
                    println("From: ${message.sender.name} (ID: ${message.sender.id})")
                    println("Date: ${message.date.format(dateFormatter)}")
                    println("Snippet: ${message.snippet}")
                    println("URL: ${message.url}")
                }
            }
        }

        is InboxParseResult.PermissionDenial -> {
            println("INBOX ACCESS DENIED")
            println("Message: ${result.message}")
        }

        is InboxParseResult.Failure -> {
            println("INBOX ERROR")
            println("Message: ${result.message}")
            println("Exception: ${result.exception}")
        }
    }
}