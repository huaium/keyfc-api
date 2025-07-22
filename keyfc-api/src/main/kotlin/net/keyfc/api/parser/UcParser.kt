package net.keyfc.api.parser

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import net.keyfc.api.RepoClient
import net.keyfc.api.model.page.uc.*
import net.keyfc.api.model.result.parse.BaseParseResult
import net.keyfc.api.model.result.parse.UcParseResult
import java.net.HttpCookie

/**
 * Parser for user control panel (UC) pages.
 * This parser extracts user information from the UC page.
 */
internal object UcParser : BaseParser() {

    private const val UC_URL = BASE_URL + "usercp.aspx"

    /**
     * Retrieves and parses the user control panel page.
     *
     * @param repoClient The repository client to use for HTTP requests
     * @param cookies The cookies to include in the request
     * @return [UcParseResult] containing the user information if successful
     */
    suspend fun parse(
        repoClient: RepoClient,
        cookies: List<HttpCookie> = emptyList()
    ): UcParseResult {
        try {
            val document = repoClient.parseUrl(
                url = UC_URL,
                cookies = cookies,
            )

            // Parse the HTML response
            return parseUcPage(document)
        } catch (e: Exception) {
            return UcParseResult.Failure(
                "Failed to retrieve user control panel: ${e.message}",
                e
            )
        }
    }

    /**
     * Parse the user control panel page and extract user information.
     *
     * @param document The HTML document to parse
     * @return [UcParseResult] containing the user information if successfully parsed
     */
    private fun parseUcPage(document: Document): UcParseResult {
        val baseResult = super.parseBase(document)

        return when (baseResult) {
            is BaseParseResult.Success -> {
                try {
                    // Check for permission denial message
                    val errorMsgDiv = document.selectFirst("div.msg_inner.error_msg")
                    if (errorMsgDiv != null) {
                        val permissionMessage = errorMsgDiv.selectFirst("p")?.text() ?: "Permission denied"
                        return UcParseResult.PermissionDenial(permissionMessage)
                    }

                    // Extract username
                    val usernameElement = document.selectFirst("div.cpuser ul.cprate li strong")
                    val username = usernameElement?.text() ?: ""

                    // Extract avatar URL
                    val avatarElement = document.selectFirst("div.cpuser img.cpavatar")
                    val avatarUrl = avatarElement?.attr("src") ?: ""

                    // Extract stats
                    val statsElements = document.select("div.cpuser ul.cprate li:not(:first-child)")

                    // Initialize with default values
                    var score = 0
                    var experience = 0
                    var popularity = 0
                    var kp = 0
                    var credits = 0
                    var goodPersonCards = 0
                    var favorability = 0

                    // Parse each stat element
                    for (statElement in statsElements) {
                        val statText = statElement.text().trim()
                        when {
                            statText.startsWith("积分") -> score = extractNumberFromStat(statText)
                            statText.startsWith("经验") -> experience = extractNumberFromStat(statText)
                            statText.startsWith("人气") -> popularity = extractNumberFromStat(statText)
                            statText.startsWith("ＫＰ") -> kp = extractNumberFromStat(statText)
                            statText.startsWith("学分") -> credits = extractNumberFromStat(statText)
                            statText.startsWith("好人卡") -> goodPersonCards = extractNumberFromStat(statText)
                            statText.startsWith("好感度") -> favorability = extractNumberFromStat(statText)
                        }
                    }

                    // Create UserStats object
                    val userStats = UserStats(
                        score = score,
                        experience = experience,
                        popularity = popularity,
                        kp = kp,
                        credits = credits,
                        goodPersonCards = goodPersonCards,
                        favorability = favorability
                    )

                    // Extract post count
                    val postCountElement = document.selectFirst("ul.cpinfo li:contains(总发帖数) a")
                    val postCount = postCountElement?.text()?.toIntOrNull() ?: 0

                    // Extract digest count
                    val digestCountElement = document.selectFirst("ul.cpinfo li:contains(精华帖数)")
                    val digestCount = digestCountElement?.ownText()?.trim()?.toIntOrNull() ?: 0

                    // Extract new message count
                    val newMessageCountElement = document.selectFirst("ul.cpinfo li:contains(新短消息数) script")
                    val newMessageCountScript = newMessageCountElement?.html() ?: ""
                    val newMessageCount = extractNewMessageCount(newMessageCountScript)

                    // Extract new notice count
                    val newNoticeCountElement = document.selectFirst("ul.cpinfo li:contains(新通知数)")
                    val newNoticeCount = newNoticeCountElement?.ownText()?.trim()?.toIntOrNull() ?: 0

                    // Extract signature
                    val signatureElement = document.selectFirst("div.cpsignature")
                    val signature = signatureElement?.ownText()?.substring(1)?.trim() ?: ""

                    // Parse user permissions
                    val permissions = parseUserPermissions(document)

                    // Create UcPage object
                    val ucPage = UcPage(
                        pageInfo = baseResult.pageInfo,
                        username = username,
                        avatar = avatarUrl,
                        stats = userStats,
                        signature = signature,
                        postCount = postCount,
                        digestCount = digestCount,
                        newMessageCount = newMessageCount,
                        newNoticeCount = newNoticeCount,
                        permissions = permissions
                    )

                    return UcParseResult.Success(ucPage)

                } catch (e: Exception) {
                    return UcParseResult.Failure(
                        "Failed to parse user control panel page: ${e.message}",
                        e
                    )
                }
            }

            is BaseParseResult.Failure -> {
                UcParseResult.Failure(baseResult.message, baseResult.exception)
            }
        }
    }

    /**
     * Extracts a number from a stat string like "积分: 76"
     */
    private fun extractNumberFromStat(statText: String): Int {
        val numberRegex = "\\d+".toRegex()
        val match = numberRegex.find(statText)
        return match?.value?.toIntOrNull() ?: 0
    }

    /**
     * Extracts new message count from a JavaScript snippet
     */
    private fun extractNewMessageCount(script: String): Int {
        // Example script: document.write(0*-1);
        val numberRegex = "\\((\\d+)\\*".toRegex()
        val match = numberRegex.find(script)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    /**
     * Extracts user level from a JavaScript element
     */
    private fun extractLevel(element: Element): Int {
        // Example script: ShowStars(1, 2);
        val script = element.selectFirst("script")?.html() ?: return 0
        val numberRegex = "ShowStars\\((\\d+),".toRegex()
        val match = numberRegex.find(script)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    /**
     * Extracts validation status from a JavaScript element
     */
    private fun extractValidation(element: Element): Boolean {
        // Example script: getvalidpic(0)
        val script = element.selectFirst("script")?.html() ?: return false
        val numberRegex = "getvalidpic\\((\\d+)\\)".toRegex()
        val match = numberRegex.find(script)
        return (match?.groupValues?.get(1)?.toIntOrNull() ?: 0) == 1
    }

    private fun extractSearchCapability(element: Element): SearchCapability {
        // Example script: searchtype(2)
        val script = element.selectFirst("script")?.html() ?: return SearchCapability.NOT_ALLOWED
        val numberRegex = "searchtype\\((\\d+)\\)".toRegex()
        val match = numberRegex.find(script)
        return SearchCapability.fromCodeOrDefault(match?.groupValues?.get(1)?.toIntOrNull() ?: 0)
    }

    /**
     * Parse user permissions from the user control panel page.
     *
     * @param document The HTML document to parse
     * @return [UserPermissions] object containing the user's permissions
     */
    private fun parseUserPermissions(document: Document): UserPermissions? {
        try {
            // Parse user group information
            val userGroupTable = document.selectFirst("div#list_memcp_main table tbody tr")
            if (userGroupTable == null) return null

            val cells = userGroupTable.select("td")
            if (cells.size < 6) return null

            val userGroup = UserGroup(
                name = cells[0].selectFirst("strong")?.text() ?: "",
                level = extractLevel(cells[1]),
                type = cells[2].text().trim(),
                startingPoints = cells[3].text().trim().toIntOrNull() ?: 0,
                readPermissionLevel = cells[4].text().trim().toIntOrNull() ?: 0,
                expirationTime = cells[5].text().trim().takeIf { it != "-" }
            )

            // Parse basic permissions
            val basicPermissionsTable = document.selectFirst("table#list_basic tbody tr:last-child")
            if (basicPermissionsTable == null) return null

            val basicCells = basicPermissionsTable.select("td")
            if (basicCells.size < 5) return null

            val basicPermissions = BasicPermissions(
                forumAccess = extractValidation(basicCells[0]),
                readPermissionLevel = basicCells[1].text().trim().toIntOrNull() ?: 0,
                viewUserProfiles = extractValidation(basicCells[2]),
                searchCapability = extractSearchCapability(basicCells[3]),
                messageInboxCapacity = basicCells[4].text().trim().toIntOrNull() ?: 0
            )

            // Parse post permissions
            val postPermissionsTable1 =
                document.select("div#list_post table").getOrNull(0)?.select("tbody tr:last-child")
            val postPermissionsTable2 =
                document.select("div#list_post table").getOrNull(1)?.select("tbody tr:last-child")

            if (postPermissionsTable1 == null || postPermissionsTable2 == null) return null

            val postCells1 = postPermissionsTable1.select("td")
            val postCells2 = postPermissionsTable2.select("td")

            if (postCells1.size < 7 || postCells2.size < 6) return null

            val postPermissions = PostPermissions(
                createTopics = extractValidation(postCells1[0]),
                replyToPosts = extractValidation(postCells1[1]),
                createPolls = extractValidation(postCells1[2]),
                voteInPolls = extractValidation(postCells1[3]),
                postRewards = extractValidation(postCells1[4]),
                postDebates = extractValidation(postCells1[5]),
                postTransactions = extractValidation(postCells1[6]),
                maxSignatureLength = postCells2[0].text().trim().toIntOrNull() ?: 0,
                useDiscuzCodeInSignature = extractValidation(postCells2[1]),
                useImgCodeInSignature = extractValidation(postCells2[2]),
                allowHtmlPosts = extractValidation(postCells2[3]),
                useHideCode = extractValidation(postCells2[4]),
                maxTopicPrice = postCells2[5].text().trim().toIntOrNull() ?: 0
            )

            // Parse attachment permissions
            val attachmentPermissionsTable = document.selectFirst("table#list_attachment tbody tr:last-child")
            if (attachmentPermissionsTable == null) return null

            val attachmentCells = attachmentPermissionsTable.select("td")
            if (attachmentCells.size < 6) return null

            val attachmentPermissions = AttachmentPermissions(
                downloadViewAttachments = extractValidation(attachmentCells[0]),
                uploadAttachments = extractValidation(attachmentCells[1]),
                setAttachmentPermissions = extractValidation(attachmentCells[2]),
                maxSingleAttachmentSize = attachmentCells[3].text().trim().toIntOrNull() ?: 0,
                maxDailyAttachmentSize = attachmentCells[4].text().trim().toIntOrNull() ?: 0,
                allowedAttachmentTypes = attachmentCells[5].text().trim()
            )

            return UserPermissions(
                userGroup = userGroup,
                basicPermissions = basicPermissions,
                postPermissions = postPermissions,
                attachmentPermissions = attachmentPermissions
            )
        } catch (_: Exception) {
            // If parsing permissions fails, return null rather than failing the entire parse
            return null
        }
    }
}