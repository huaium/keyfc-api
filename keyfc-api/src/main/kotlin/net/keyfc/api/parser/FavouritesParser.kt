package net.keyfc.api.parser

import com.fleeksoft.ksoup.nodes.Element
import net.keyfc.api.RepoClient
import net.keyfc.api.RepoClient.Companion.BASE_URL
import net.keyfc.api.ext.*
import net.keyfc.api.model.User
import net.keyfc.api.model.favourites.Favourite
import net.keyfc.api.model.favourites.FavouritesPage
import java.net.HttpCookie
import java.time.format.DateTimeFormatter.ofPattern

internal object FavouritesParser {

    private const val FAVOURITES_URL = BASE_URL + "usercpsubscribe.aspx"

    private const val IS_ARCHIVER = false

    private fun parseDateTime(dateTimeText: String) = dateTimeText.parseDateTime(ofPattern("yyyy/M/d HH:mm:ss"))

    /**
     * Retrieves and parses the Favourites page.
     *
     * @param repoClient The repository client to use for HTTP requests
     * @param cookies The cookies to include in the request
     *
     * @return [Result] containing the parsed [FavouritesPage] or an error if parsing fails
     */
    suspend fun parse(
        repoClient: RepoClient,
        cookies: List<HttpCookie> = emptyList(),
    ): Result<FavouritesPage> {
        return runCatching {
            val document = repoClient.parseUrl(
                url = FAVOURITES_URL,
                cookies = cookies,
            ).apply { this.validate().getOrThrow() }

            // Extract favourites
            val favourites = document
                .select("table.datatable > tbody > tr:not(.colplural)") // Skip the first row which contains column names
                .mapNotNull { parseFavourite(it) }

            FavouritesPage(
                document = document,
                pageInfo = document.pageInfo(),
                favourites = favourites,
                pagination = document.pagination(IS_ARCHIVER)
            )
        }
    }

    /**
     * Parses a single favourite row element.
     *
     * @param row The favourite row element
     *
     * @return [Favourite] or null if parsing fails
     */
    private fun parseFavourite(row: Element): Favourite? {
        try {
            // Extract checkbox with topic ID
            // If any essential field is missing, return null
            val id = row.selectFirst("input[name=titemid]")?.attr("value") ?: return null

            // Extract favourite title and URL
            val titleElement = row.selectFirst("td.datatitle > a")
            val title = titleElement?.text()?.trim() ?: return null
            val url = titleElement.attr("href")

            // Extract author information
            val authorElement = row.selectFirst("td:nth-child(3) > a")
            val author = parseAuthor(authorElement) ?: return null

            // Extract favourite date
            val dateElement = row.selectFirst("td.time")
            val dateText = dateElement?.text()?.trim() ?: return null
            val date = parseDateTime(dateText)

            return Favourite(
                id = id,
                title = title,
                url = url,
                author = author,
                date = date,
                dateText = dateText
            )
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Parses author information from an element.
     */
    private fun parseAuthor(author: Element?): User? {
        val authorName = author?.text()?.trim() ?: return null
        val authorId = author.attr("href").parseId()

        return User(id = authorId, name = authorName)
    }
}