package net.keyfc.api

import io.ktor.client.statement.*
import io.ktor.http.*
import net.keyfc.api.RepoClient.Companion.BASE_URL
import net.keyfc.api.model.AuthResult
import java.io.IOException
import java.net.HttpCookie
import java.time.Instant

internal class AuthClient(val username: String, val password: String) {

    companion object {
        /**
         * Default expiration time in seconds (12 hours).
         */
        private const val DEFAULT_EXPIRES = "43200"
        private const val TEMPLATE_ID = "0"
        private const val LOGIN = ""

        private const val LOGIN_URL = BASE_URL + "login.aspx"
    }

    private var _cookies: List<HttpCookie> = emptyList()

    /**
     * Cookies received after successful login, or empty if not logged in.
     */
    val cookies: List<HttpCookie>
        get() = _cookies.toList()

    /**
     * Indicates if a user is currently logged in, with valid cookies.
     */
    val isLoggedInValid: Boolean
        get() = isLoggedIn && !isSessionExpired()

    /**
     * Indicates if a user is logged in, regardless of cookie validity.
     */
    val isLoggedIn: Boolean
        get() = _cookies.isNotEmpty()

    /**
     * Logs in to the forum with provided username and password.
     *
     * @return [AuthResult] representing the outcome of the login attempt
     */
    suspend fun login(): AuthResult {
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

            return AuthResult.Failure(
                IOException("Response code is not 200, instead: ${response.status.value}")
            )
        } catch (e: Exception) {
            return AuthResult.Failure(e)
        } finally {
            repoClient.close()
        }
    }

    /**
     * Checks if the session has expired based on the 'expires' cookie.
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
     * Clears stored cookies.
     */
    fun logout() {
        _cookies = emptyList()
    }

    /**
     * Extracts login failure attempts and maximum retry count from KeyFC forum error page.
     *
     * @param response Ktor HttpResponse after logging in
     *
     * @return [AuthResult] indicating the result of the login attempt
     */
    private suspend fun tackleLoggedInPage(repoClient: RepoClient, response: HttpResponse): AuthResult {
        try {
            // Parse the HTML document
            val document = repoClient.parseHtml(response.bodyAsText())

            // Find the element containing the error message
            val errorMsg = document.select("div.msg_inner.error_msg p").first()?.text()

            // Extract cookies when login is successful
            if (errorMsg == null) {
                val cookiesHeaders = response.headers.getAll(HttpHeaders.SetCookie) ?: emptyList()
                _cookies = cookiesHeaders.flatMap { HttpCookie.parse(it) }
                return AuthResult.Success(cookies)
            }

            // Check if user exists
            if (errorMsg.contains("用户不存在"))
                return AuthResult.UserNotFoundDenial(errorMsg)

            // Check if password is incorrect
            "密码或安全提问第(\\d+)次错误, 您最多有(\\d+)次机会重试".toRegex().find(errorMsg)?.let { matchResult ->
                return tacklePasswordIncorrect(matchResult)
            }

            // Or else, error message is unrecognized
            return AuthResult.UnknownDenial(errorMsg)

        } catch (e: Exception) {
            // Handle any exceptions that might occur during handling logged-in page
            return AuthResult.Failure(e)
        }
    }

    /**
     * Handles the case when the password is incorrect.
     *
     * @param matchResult The regex match result containing failure count and max retry count
     *
     * @return [AuthResult] indicating the password is incorrect with failure and max retry counts
     */
    private fun tacklePasswordIncorrect(matchResult: MatchResult): AuthResult {
        // Use regular expression to extract failure count and max retry count
        // Extract and convert the numeric values
        val failingTimes = matchResult.groupValues[1].toIntOrNull() ?: return AuthResult.Failure(
            RuntimeException("Failing times not found")
        )
        val maxRetries = matchResult.groupValues[2].toIntOrNull() ?: return AuthResult.Failure(
            RuntimeException("Max retries not found")
        )

        // Return the extracted information as a pair
        return AuthResult.PasswordIncorrectDenial(failingTimes, maxRetries)
    }
}