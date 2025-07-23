package net.keyfc.api.parser

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import net.keyfc.api.RepoClient
import net.keyfc.api.model.inbox.InboxItem
import net.keyfc.api.model.inbox.InboxPage
import net.keyfc.api.model.search.User
import net.keyfc.api.result.parse.BaseParseResult
import net.keyfc.api.result.parse.InboxParseResult
import java.net.HttpCookie
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Parser for inbox pages.
 * This parser extracts messages from the user's inbox page.
 */
internal object InboxParser : BaseParser() {

    private const val INBOX_URL = BASE_URL + "usercpinbox.aspx"
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    /**
     * Retrieves and parses the inbox page.
     *
     * @param repoClient The repository client to use for HTTP requests
     * @param cookies The cookies to include in the request
     * @return [InboxParseResult] containing the inbox messages if successful
     */
    suspend fun parse(
        repoClient: RepoClient,
        cookies: List<HttpCookie> = emptyList(),
    ): InboxParseResult {
        try {
            val document = repoClient.parseUrl(
                url = INBOX_URL,
                cookies = cookies,
            )

            // Parse the HTML response
            return parseInboxPage(document)
        } catch (e: Exception) {
            return InboxParseResult.Failure(
                "Failed to retrieve inbox: ${e.message}",
                e
            )
        }
    }

    /**
     * Parse the inbox page and extract messages.
     *
     * @param document The HTML document to parse
     * @return [InboxParseResult] containing the inbox messages if successfully parsed
     */
    private fun parseInboxPage(document: Document): InboxParseResult {
        val baseResult = super.parseBase(document)

        return when (baseResult) {
            is BaseParseResult.Success -> {
                try {
                    // Check for permission denial message
                    val errorMsgDiv = document.selectFirst("div.msg_inner.error_msg")
                    if (errorMsgDiv != null) {
                        val permissionMessage = errorMsgDiv.selectFirst("p")?.text() ?: "Permission denied"
                        return InboxParseResult.PermissionDenial(permissionMessage)
                    }

                    // Extract pagination information
                    val pagesText = document.selectFirst("div.pages")?.text() ?: ""
                    val pageRegex = "(\\d+)/(\\d+)页".toRegex()
                    val pageMatch = pageRegex.find(pagesText)
                    val currentPage = pageMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
                    val totalPages = pageMatch?.groupValues?.get(2)?.toIntOrNull() ?: 1

                    // Extract message count and limit
                    val messageCountRegex = "共有短消息:(\\d+)条".toRegex()
                    val messageLimitRegex = "上限:(\\d+)条".toRegex()
                    val messageCountMatch = messageCountRegex.find(pagesText)
                    val messageLimitMatch = messageLimitRegex.find(pagesText)
                    val messageCount = messageCountMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
                    val messageLimit = messageLimitMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0

                    // Extract inbox messages
                    val inboxItems = mutableListOf<InboxItem>()
                    val messageRows = document.select("table.pm_list > tbody > tr")

                    for (row in messageRows) {
                        val inboxItem = parseInboxItem(row)
                        if (inboxItem != null) {
                            inboxItems.add(inboxItem)
                        }
                    }

                    val inboxPage = InboxPage(
                        pageInfo = baseResult.pageInfo,
                        messages = inboxItems,
                        currentPage = currentPage,
                        totalPages = totalPages,
                        messageCount = messageCount,
                        messageLimit = messageLimit
                    )

                    return InboxParseResult.Success(inboxPage)

                } catch (e: Exception) {
                    return InboxParseResult.Failure(
                        "Failed to parse inbox page: ${e.message}",
                        e
                    )
                }
            }

            is BaseParseResult.Failure -> {
                InboxParseResult.Failure(baseResult.message, baseResult.exception)
            }
        }
    }

    /**
     * Parse a single inbox message row element.
     *
     * @param row The inbox message row element
     * @return [InboxItem] or null if parsing fails
     */
    private fun parseInboxItem(row: Element): InboxItem? {
        try {
            // Extract message ID from the row ID attribute
            val rowId = row.id()
            if (rowId.isEmpty()) return null

            // Extract read status
            val statusImg = row.selectFirst("td.msg_icon img")
            val isRead = statusImg?.attr("title") == "已读"

            // Extract sender information
            val senderElement = row.selectFirst("td.name_and_date span.name a")
            val senderUrl = senderElement?.attr("href") ?: ""
            val senderId = extractIdFromUrl(senderUrl)
            val senderName = senderElement?.text() ?: ""
            val sender = User(id = senderId, name = senderName)

            // Extract date
            val dateElement = row.selectFirst("td.name_and_date span.date")
            val dateText = dateElement?.text()?.trim() ?: ""
            val date = parseDateTime(dateText)

            // Extract subject and snippet
            val subjectElement = row.selectFirst("td.pmsubject p a")
            val subject = subjectElement?.text()?.trim() ?: ""
            val subjectUrl = subjectElement?.attr("href") ?: ""

            val snippetElement = row.selectFirst("td.pmsubject div.snippet_wrap a")
            val snippet = snippetElement?.text()?.trim() ?: ""

            return InboxItem(
                id = rowId,
                sender = sender,
                subject = subject,
                snippet = snippet,
                date = date,
                isRead = isRead,
                url = BASE_URL + subjectUrl
            )
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Extracts the ID from a URL like "userinfo-12345.aspx"
     */
    private fun extractIdFromUrl(url: String): String {
        val regex = "-(\\d+)".toRegex()
        val match = regex.find(url)
        return match?.groupValues?.get(1) ?: ""
    }

    /**
     * Parses date and time from the format "yyyy-MM-dd HH:mm"
     */
    private fun parseDateTime(dateTimeText: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateTimeText.trim(), dateFormatter)
        } catch (_: DateTimeParseException) {
            // If standard format fails, try alternative formats or return current time
            LocalDateTime.now()
        }
    }
}