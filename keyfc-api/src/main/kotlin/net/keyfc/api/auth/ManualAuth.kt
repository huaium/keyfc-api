package net.keyfc.api.auth

import io.ktor.client.statement.*
import io.ktor.http.*
import net.keyfc.api.RepoClient
import net.keyfc.api.model.result.ManualAuthResult
import java.io.IOException
import java.net.HttpCookie
import java.time.Instant

/**
 * Handle cookie-based login operations.
 */
class ManualAuth(val username: String, val password: String) {
    companion object {
        /**
         * Default expiration time in seconds (12 hours).
         */
        private const val DEFAULT_EXPIRES = "43200"
        private const val TEMPLATE_ID = "0"
        private const val LOGIN = ""

        private const val LOGIN_URL = "https://keyfc.net/bbs/login.aspx"
    }

    /**
     * Cookies received after successful login.
     */
    private var _cookies: List<HttpCookie> = emptyList()

    fun getCookies(): List<HttpCookie> = _cookies.toList()

    /**
     * Indicates if a user is currently logged in.
     */
    val isLoggedInValid: Boolean
        get() = isLoggedIn && !isSessionExpired()

    val isLoggedIn: Boolean
        get() = _cookies.isNotEmpty()

    /**
     * Login to the forum with provided username and password.
     *
     * @return A [ManualAuthResult] representing the outcome of the login attempt
     */
    suspend fun login(): ManualAuthResult {
        // Does not retain the repoClient as its property,
        // since login behavior is not expected to occur frequently
        val repoClient = RepoClient()

        try {
            // Execute request
            val response = repoClient.postFormData(
                url = "$LOGIN_URL?stamp=${Math.random()}",
                formDataMap = mapOf(
                    "username" to username,
                    "password" to password,
                    "templateid" to TEMPLATE_ID,
                    "login" to LOGIN,
                    "expires" to DEFAULT_EXPIRES
                )
            )

            if (response.status.isSuccess()) {
                return tackleLoggedInPage(repoClient, response)
            }

            return ManualAuthResult.Failure(
                "Response code is not 200, instead: ${response.status.value}",
                IOException("Response code is not 200, instead: ${response.status.value}")
            )
        } catch (e: Exception) {
            return ManualAuthResult.Failure("Error occurred during login: ${e.message}", e)
        } finally {
            repoClient.close()
        }
    }

    /**
     * Check if the session has expired based on the 'expires' cookie.
     *
     * @return True if the session has expired or no expiration cookie is found, false otherwise
     */
    private fun isSessionExpired(): Boolean {
        val expiresCookie = _cookies.find { it.name == "expires" } ?: return true

        return try {
            val expirationTime = Instant.parse(expiresCookie.value)
            val currentTime = Instant.now()

            currentTime.isAfter(expirationTime)
        } catch (_: Exception) {
            // If we can't parse the expiration date, assume the session is expired
            true
        }
    }

    /**
     * Clear stored cookies.
     */
    fun logout() {
        _cookies = emptyList()
    }

    /**
     * Extracts login failure attempts and maximum retry count from KeyFC forum error page.
     *
     * @param response Ktor HttpResponse after logging in
     * @return [ManualAuthResult] indicating the result of the login attempt
     */
    private suspend fun tackleLoggedInPage(repoClient: RepoClient, response: HttpResponse): ManualAuthResult {
        try {
            // Parse the HTML document
            val document = repoClient.parseHtml(response.bodyAsText())

            // Find the element containing the error message
            val errorMsg = document.select("div.msg_inner.error_msg p").first()?.text()

            // Extract cookies when login is successful
            if (errorMsg == null) {
                val cookiesHeaders = response.headers.getAll(HttpHeaders.SetCookie) ?: emptyList()
                _cookies = cookiesHeaders.flatMap { HttpCookie.parse(it) }
                return ManualAuthResult.Success(getCookies())
            }

            // Check if user exists
            if (errorMsg.contains("用户不存在"))
                return ManualAuthResult.UserNotFoundDenial(errorMsg)

            // Check if password is incorrect
            "密码或安全提问第(\\d+)次错误, 您最多有(\\d+)次机会重试".toRegex().find(errorMsg)?.let { matchResult ->
                return tacklePasswordIncorrect(matchResult)
            }

            // Or else, error message is unrecognized
            return ManualAuthResult.UnknownDenial(errorMsg)

        } catch (e: Exception) {
            // Handle any exceptions that might occur during handling logged-in page
            return ManualAuthResult.Failure("Error occurred during handling logged-in page: ${e.message}", e)
        }
    }

    private fun tacklePasswordIncorrect(matchResult: MatchResult): ManualAuthResult {
        // Use regular expression to extract failure count and max retry count
        // Extract and convert the numeric values
        val failingTimes = matchResult.groupValues[1].toIntOrNull() ?: return ManualAuthResult.Failure(
            "Failing times not found",
            RuntimeException("Failing times not found")
        )
        val maxRetries = matchResult.groupValues[2].toIntOrNull() ?: return ManualAuthResult.Failure(
            "Max retries not found",
            RuntimeException("Max retries not found")
        )

        // Return the extracted information as a pair
        return ManualAuthResult.PasswordIncorrectDenial(failingTimes, maxRetries)
    }
}