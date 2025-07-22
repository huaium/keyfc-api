package net.keyfc.api.parser

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import net.keyfc.api.RepoClient
import net.keyfc.api.model.notification.NotificationFilter
import net.keyfc.api.model.notification.NotificationItem
import net.keyfc.api.model.notification.NotificationPage
import net.keyfc.api.model.search.User
import net.keyfc.api.result.parse.BaseParseResult
import net.keyfc.api.result.parse.NotificationParseResult
import java.net.HttpCookie
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Parser for notification pages.
 * This parser extracts notifications from the notification page.
 */
internal object NotificationParser : BaseParser() {

    private const val NOTIFICATION_URL = BASE_URL + "usercpnotice.aspx"
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    /**
     * Retrieves and parses the notification page.
     *
     * @param repoClient The repository client to use for HTTP requests
     * @param cookies The cookies to include in the request
     * @param filter Optional filter for notifications
     * @return [NotificationParseResult] containing the notifications if successful
     */
    suspend fun parse(
        repoClient: RepoClient,
        cookies: List<HttpCookie> = emptyList(),
        filter: NotificationFilter = NotificationFilter.ALL
    ) = parse(repoClient, cookies, filter.value)

    /**
     * Retrieves and parses the notification page.
     *
     * Serves the same functionality as parse that accepts [NotificationFilter], but it allows user to specify the filter string instead of limited options.
     *
     * @param repoClient The repository client to use for HTTP requests
     * @param cookies The cookies to include in the request
     * @param filter Optional filter for notifications (e.g., "all", "topicadmin", etc.)
     * @return [NotificationParseResult] containing the notifications if successful
     */
    suspend fun parse(
        repoClient: RepoClient,
        cookies: List<HttpCookie> = emptyList(),
        filter: String = "all"
    ): NotificationParseResult {
        try {
            val url = if (filter == "all") NOTIFICATION_URL else "$NOTIFICATION_URL?filter=$filter"

            val document = repoClient.parseUrl(
                url = url,
                cookies = cookies,
            )

            // Parse the HTML response
            return parseNotificationPage(document)
        } catch (e: Exception) {
            return NotificationParseResult.Failure(
                "Failed to retrieve notifications: ${e.message}",
                e
            )
        }
    }

    /**
     * Parse the notification page and extract notifications.
     *
     * @param document The HTML document to parse
     * @return [NotificationParseResult] containing the notifications if successfully parsed
     */
    private fun parseNotificationPage(document: Document): NotificationParseResult {
        val baseResult = super.parseBase(document)

        return when (baseResult) {
            is BaseParseResult.Success -> {
                try {
                    // Check for permission denial message
                    val errorMsgDiv = document.selectFirst("div.msg_inner.error_msg")
                    if (errorMsgDiv != null) {
                        val permissionMessage = errorMsgDiv.selectFirst("p")?.text() ?: "Permission denied"
                        return NotificationParseResult.PermissionDenial(permissionMessage)
                    }

                    // Extract pagination information
                    val pagesText = document.selectFirst("div.pages")?.text() ?: ""
                    val pageRegex = "(\\d+)/(\\d+)页".toRegex()
                    val pageMatch = pageRegex.find(pagesText)
                    val currentPage = pageMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
                    val totalPages = pageMatch?.groupValues?.get(2)?.toIntOrNull() ?: 1

                    // Extract notifications
                    val notificationItems = mutableListOf<NotificationItem>()
                    val notificationRows = document.select("table.pm_list > tbody > tr")

                    for (row in notificationRows) {
                        val notificationItem = parseNotificationItem(row)
                        if (notificationItem != null) {
                            notificationItems.add(notificationItem)
                        }
                    }

                    val notificationPage = NotificationPage(
                        pageInfo = baseResult.pageInfo,
                        notifications = notificationItems,
                        currentPage = currentPage,
                        totalPages = totalPages
                    )

                    return NotificationParseResult.Success(notificationPage)

                } catch (e: Exception) {
                    return NotificationParseResult.Failure(
                        "Failed to parse notification page: ${e.message}",
                        e
                    )
                }
            }

            is BaseParseResult.Failure -> {
                NotificationParseResult.Failure(baseResult.message, baseResult.exception)
            }
        }
    }

    /**
     * Parse a single notification row element.
     *
     * @param row The notification row element
     * @return [NotificationItem] or null if parsing fails
     */
    private fun parseNotificationItem(row: Element): NotificationItem? {
        try {
            // Extract notification content
            val contentElement = row.selectFirst("td.notice_list")
            val content = contentElement?.text()?.trim() ?: return null

            // Extract date
            val dateElement = row.selectFirst("td.name_and_date span.date")
            val dateText = dateElement?.text()?.trim() ?: return null
            val date = parseDateTime(dateText)

            // Extract topic information if available
            val topicElement = contentElement.selectFirst("a[href*=showtopic]")
            val topicUrl = topicElement?.attr("href")
            val topicId = topicUrl?.let { extractIdFromUrl(it) }
            val topicTitle = topicElement?.text()

            // Extract user information if available
            val userElement = contentElement.selectFirst("a[href*=userinfo]")
            val userId = userElement?.attr("href")?.let { extractIdFromUrl(it) }
            val userName = userElement?.text()
            val user = if (userId != null && userName != null) User(id = userId, name = userName) else null

            // Extract reason if available
            val reasonText = content.substringAfterLast("理由:", "").takeIf { it != content }

            return NotificationItem(
                content = content,
                user = user,
                topicId = topicId,
                topicTitle = topicTitle,
                topicUrl = topicUrl,
                reason = reasonText,
                date = date
            )
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Extracts the ID from a URL like "showtopic-12345.aspx" or "userinfo-12345.aspx"
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
        } catch (e: DateTimeParseException) {
            // If standard format fails, try alternative formats or return current time
            LocalDateTime.now()
        }
    }
}