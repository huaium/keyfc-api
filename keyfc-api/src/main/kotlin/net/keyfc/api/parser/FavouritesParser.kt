package net.keyfc.api.parser

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import net.keyfc.api.RepoClient
import net.keyfc.api.model.favourites.Favourite
import net.keyfc.api.model.favourites.FavouritesPage
import net.keyfc.api.model.search.User
import net.keyfc.api.result.parse.BaseParseResult
import net.keyfc.api.result.parse.FavouritesParseResult
import java.net.HttpCookie
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Parser for "Favourites" page.
 * This parser extracts the user's favourites from the Favourites page.
 */
internal object FavouritesParser : BaseParser() {

    private const val FAVOURITES_URL = BASE_URL + "usercpsubscribe.aspx"
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

    /**
     * Retrieves and parses the Favourites page.
     *
     * @param repoClient The repository client to use for HTTP requests
     * @param cookies The cookies to include in the request
     * @return [FavouritesParseResult] containing the user's favourites if successful
     */
    suspend fun parse(
        repoClient: RepoClient,
        cookies: List<HttpCookie> = emptyList(),
    ): FavouritesParseResult {
        try {
            val document = repoClient.parseUrl(
                url = FAVOURITES_URL,
                cookies = cookies,
            )

            // Parse the HTML response
            return parseFavouritesPage(document)
        } catch (e: Exception) {
            return FavouritesParseResult.Failure(
                "Failed to retrieve favourites: ${e.message}",
                e
            )
        }
    }

    /**
     * Parse the Favourites page and extract favourites.
     *
     * @param document The HTML document to parse
     * @return [FavouritesParseResult] containing the user's favourites if successfully parsed
     */
    private fun parseFavouritesPage(document: Document): FavouritesParseResult {
        val baseResult = super.parseBase(document)

        return when (baseResult) {
            is BaseParseResult.Success -> {
                try {
                    // Check for permission denial message
                    val errorMsgDiv = document.selectFirst("div.msg_inner.error_msg")
                    if (errorMsgDiv != null) {
                        val permissionMessage = errorMsgDiv.selectFirst("p")?.text() ?: "Permission denied"
                        return FavouritesParseResult.PermissionDenial(permissionMessage)
                    }

                    // Extract pagination information
                    // Using .html() instead of .text() to avoid character merging
                    val pagesText = document.selectFirst("div.pages")?.html() ?: ""
                    val pageRegex = "(\\d+)/(\\d+)页".toRegex()
                    val pageMatch = pageRegex.find(pagesText)
                    val currentPage = pageMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
                    val totalPages = pageMatch?.groupValues?.get(2)?.toIntOrNull() ?: 1

                    // Extract favourites
                    val favourites = mutableListOf<Favourite>()
                    val favouriteRows =
                        document.select("table.datatable > tbody > tr:not(.colplural)") // Skip the first row which contains column names

                    for (row in favouriteRows) {
                        val favourite = parseFavourite(row)
                        if (favourite != null) {
                            favourites.add(favourite)
                        }
                    }

                    val favouritesPage = FavouritesPage(
                        pageInfo = baseResult.pageInfo,
                        favourites = favourites,
                        currentPage = currentPage,
                        totalPages = totalPages
                    )

                    return FavouritesParseResult.Success(favouritesPage)

                } catch (e: Exception) {
                    return FavouritesParseResult.Failure(
                        "Failed to parse favourites page: ${e.message}",
                        e
                    )
                }
            }

            is BaseParseResult.Failure -> {
                FavouritesParseResult.Failure(baseResult.message, baseResult.exception)
            }
        }
    }

    /**
     * Parse a single favourite row element.
     *
     * @param row The favourite row element
     * @return [Favourite] or null if parsing fails
     */
    private fun parseFavourite(row: Element): Favourite? {
        try {
            // Extract checkbox with topic ID
            val checkbox = row.selectFirst("input[name=titemid]")
            val id = checkbox?.attr("value") ?: ""

            // Extract favourite title and URL
            val titleElement = row.selectFirst("td.datatitle > a")
            val title = titleElement?.text()?.trim() ?: ""
            val url = titleElement?.attr("href") ?: ""

            // Extract author information
            val authorElement = row.selectFirst("td:nth-child(3) > a")
            val authorName = authorElement?.text()?.trim() ?: ""
            val authorUrl = authorElement?.attr("href") ?: ""
            val authorId = extractIdFromUrl(authorUrl)
            val author = User(id = authorId, name = authorName)

            // Extract favourite date
            val dateElement = row.selectFirst("td.time")
            val dateText = dateElement?.text()?.trim() ?: ""
            val date = parseDateTime(dateText)

            return Favourite(
                id = id,
                title = title,
                url = BASE_URL + url,
                author = author,
                favouriteDate = date
            )
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Extracts the ID from a URL like "userinfo-12345.aspx" or "showtopic-12345.aspx" or "showforum-12345.aspx"
     */
    private fun extractIdFromUrl(url: String): String {
        val regex = "-(\\d+)".toRegex()
        val match = regex.find(url)
        return match?.groupValues?.get(1) ?: ""
    }

    /**
     * Parses date and time from the format "yyyy/MM/dd HH:mm:ss"
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