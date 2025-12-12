package net.keyfc.api.parser

import com.fleeksoft.ksoup.nodes.Element
import io.ktor.http.Cookie
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import net.keyfc.api.RepoClient
import net.keyfc.api.RepoClient.Companion.BASE_URL
import net.keyfc.api.ext.pageInfo
import net.keyfc.api.ext.pagination
import net.keyfc.api.ext.parseDateTime
import net.keyfc.api.ext.parseId
import net.keyfc.api.ext.validate
import net.keyfc.api.model.User
import net.keyfc.api.model.notifications.Notification
import net.keyfc.api.model.notifications.NotificationsFilter
import net.keyfc.api.model.notifications.NotificationsPage

internal object NotificationsParser {

    private const val NOTIFICATION_URL = BASE_URL + "usercpnotice.aspx"

    private const val IS_ARCHIVER = false

    @OptIn(FormatStringsInDatetimeFormats::class)
    private fun parseDateTime(dateTimeText: String) =
        dateTimeText.parseDateTime(LocalDateTime.Format { byUnicodePattern("yyyy-MM-dd HH:mm") })

    /**
     * Retrieves and parses the notification page.
     *
     * @param repoClient The repository client to use for HTTP requests
     * @param cookies The cookies to include in the request
     * @param filter Optional filter for notifications
     *
     * @return [Result] containing the notifications if successful, or an error if parsing fails
     */
    suspend fun parse(
        repoClient: RepoClient,
        cookies: List<Cookie> = emptyList(),
        filter: NotificationsFilter = NotificationsFilter.ALL
    ) = parse(repoClient, cookies, filter.value)

    /**
     * Retrieves and parses the notification page.
     *
     * Serves the same functionality as parse that accepts [NotificationsFilter], but it allows user to specify the filter string instead of limited options.
     *
     * @param repoClient The repository client to use for HTTP requests
     * @param cookies The cookies to include in the request
     * @param filter Optional filter for notifications (e.g., "all", "topicadmin", etc.)
     *
     * @return [Result] containing the notifications if successful, or an error if parsing fails
     */
    suspend fun parse(
        repoClient: RepoClient,
        cookies: List<Cookie> = emptyList(),
        filter: String = "all"
    ): Result<NotificationsPage> {
        return runCatching {
            val url = if (filter == "all") NOTIFICATION_URL else "$NOTIFICATION_URL?filter=$filter"

            val document = repoClient.parseUrl(
                url = url,
                cookies = cookies,
            ).apply { this.validate().getOrThrow() }

            val notifications = document
                .select("table.pm_list > tbody > tr")
                .mapNotNull { parseNotificationItem(it) }

            NotificationsPage(
                document = document,
                pageInfo = document.pageInfo(),
                notifications = notifications,
                pagination = document.pagination(IS_ARCHIVER),
            )
        }
    }

    /**
     * Parses a single notification row element.
     *
     * @param row The notification row element
     *
     * @return [Notification] or null if parsing fails
     */
    private fun parseNotificationItem(row: Element): Notification? {
        try {
            // Extract notification content
            val contentElement = row.selectFirst("td.notice_list")
            val content = contentElement?.text()?.trim() ?: return null

            // Extract date
            val dateElement = row.selectFirst("td.name_and_date span.date")
            val dateText = dateElement?.text()?.trim() ?: ""
            val date = parseDateTime(dateText)

            // Extract topic information if available
            val topicElement = contentElement.selectFirst("a[href*=showtopic]")
            val topicUrl = topicElement?.attr("href") ?: return null
            val topicId = topicUrl.parseId()
            val topicTitle = topicElement.text()

            // Extract user information if available
            val userElement = contentElement.selectFirst("a[href*=userinfo]")
            val user = parseUser(userElement) ?: return null

            // Extract reason if available
            val reasonText = content.substringAfterLast("理由:", "").takeIf { it != content } ?: return null

            return Notification(
                content = content,
                user = user,
                topicId = topicId,
                topicTitle = topicTitle,
                topicUrl = topicUrl,
                reason = reasonText,
                date = date,
                dateText = dateText
            )
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Parses user information from the user element.
     */
    private fun parseUser(user: Element?): User? {
        val userId = user?.attr("href")?.parseId() ?: return null
        val userName = user.text()

        return User(id = userId, name = userName)
    }
}