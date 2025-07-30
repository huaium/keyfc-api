package net.keyfc.api.parser

import com.fleeksoft.ksoup.nodes.Element
import net.keyfc.api.RepoClient
import net.keyfc.api.RepoClient.Companion.BASE_URL
import net.keyfc.api.ext.*
import net.keyfc.api.model.User
import net.keyfc.api.model.inbox.InboxItem
import net.keyfc.api.model.inbox.InboxPage
import java.net.HttpCookie
import java.time.format.DateTimeFormatter.ofPattern

internal object InboxParser {

    private const val INBOX_URL = BASE_URL + "usercpinbox.aspx"

    private const val IS_ARCHIVER = false

    private fun parseDateTime(dateTimeText: String) = dateTimeText.parseDateTime(ofPattern("yyyy-MM-dd HH:mm"))

    /**
     * Retrieves and parses the inbox page.
     *
     * @param repoClient The repository client to use for HTTP requests
     * @param cookies The cookies to include in the request
     *
     * @return [Result] containing [InboxPage] if successful, or an error if parsing fails
     */
    suspend fun parse(
        repoClient: RepoClient,
        cookies: List<HttpCookie> = emptyList(),
    ): Result<InboxPage> {
        return runCatching {
            val document = repoClient.parseUrl(
                url = INBOX_URL,
                cookies = cookies,
            ).apply { this.validate().getOrThrow() }

            // Extract message count and limit
            val pagesText = document.selectFirst("div.pages")?.html() ?: ""
            val messageCountRegex = "共有短消息:(\\d+)条".toRegex()
            val messageLimitRegex = "上限:(\\d+)条".toRegex()
            val messageCountMatch = messageCountRegex.find(pagesText)
            val messageLimitMatch = messageLimitRegex.find(pagesText)
            val messageCount = messageCountMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val messageLimit = messageLimitMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0

            // Extract inbox messages
            val messages = document
                .select("table.pm_list > tbody > tr")
                .mapNotNull { parseInboxItem(it) }

            InboxPage(
                document = document,
                pageInfo = document.pageInfo(),
                messages = messages,
                pagination = document.pagination(IS_ARCHIVER),
                messageCount = messageCount,
                messageLimit = messageLimit
            )
        }
    }

    /**
     * Parses a single inbox message row element.
     *
     * @param row The inbox message row element
     *
     * @return [InboxItem] or null if parsing fails
     */
    private fun parseInboxItem(row: Element): InboxItem? {
        try {
            // Extract message ID from the row ID attribute
            val rowId = row.id()
            if (rowId.isEmpty()) return null // If any essential field is missing, return null

            // Extract read status
            val statusImg = row.selectFirst("td.msg_icon img")
            val isRead = statusImg?.attr("title") == "已读"

            // Extract sender information
            val senderElement = row.selectFirst("td.name_and_date span.name a")
            val sender = parseSender(senderElement) ?: return null

            // Extract date
            val dateElement = row.selectFirst("td.name_and_date span.date")
            val dateText = dateElement?.text()?.trim() ?: return null
            val date = parseDateTime(dateText)

            // Extract subject and snippet
            val subjectElement = row.selectFirst("td.pmsubject p a")
            val subject = subjectElement?.text()?.trim() ?: return null
            val subjectUrl = subjectElement.attr("href")

            val snippetElement = row.selectFirst("td.pmsubject div.snippet_wrap a")
            val snippet = snippetElement?.text()?.trim() ?: return null

            return InboxItem(
                id = rowId,
                sender = sender,
                subject = subject,
                snippet = snippet,
                date = date,
                dateText = dateText,
                isRead = isRead,
                url = subjectUrl
            )
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Parses the sender information from an element.
     */
    private fun parseSender(sender: Element?): User? {
        val senderName = sender?.text()
        val senderUrl = sender?.attr("href")
        val senderId = senderUrl?.parseId()

        if (senderName == null || senderId == null)
            return null

        return User(id = senderId, name = senderName)
    }
}