package net.keyfc.api.auth

import net.keyfc.api.ApiApplication
import net.keyfc.api.ApiApplication.loginUri
import net.keyfc.api.model.result.LoginAuthResult
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.HttpCookie
import java.time.Instant

/**
 * Handle cookie-based login operations.
 */
class LoginAuth(val username: String, val password: String) {
    companion object {
        /**
         * Default expiration time in seconds (12 hours).
         */
        private const val DEFAULT_EXPIRES = "43200"
        private const val TEMPLATE_ID = "0"
        private const val LOGIN = ""
    }

    /**
     * Cookies received after successful login.
     */
    private var _cookies: List<HttpCookie> = emptyList()

    val cookies: List<HttpCookie>
        get() = _cookies.toList()

    /**
     * Indicates if a user is currently logged in.
     */
    val isLoggedIn: Boolean
        get() = _cookies.isNotEmpty() && !isSessionExpired()

    /**
     * Check if the session has expired based on the 'expires' cookie.
     *
     * @return True if the session has expired or no expiration cookie is found, false otherwise
     */
    fun isSessionExpired(): Boolean {
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
     * Login to the forum with provided username and password.
     *
     * @return A LoginAuthResult representing the outcome of the login attempt
     */
    fun login(): LoginAuthResult {
        try {
            // Build form parameters
            val formBodyBuilder = FormBody.Builder().apply {
                add("username", username)
                add("password", password)
                add("templateid", TEMPLATE_ID)
                add("login", LOGIN)
                add("expires", DEFAULT_EXPIRES)
            }

            val request = Request.Builder()
                .url("${loginUri}?stamp=${Math.random()}")
                .header("User-Agent", ApiApplication.USER_AGENT)
                .post(formBodyBuilder.build())
                .build()

            // Execute request
            ApiApplication.httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    return tackleLoggedInPage(response)
                }

                return LoginAuthResult.Failure(
                    "Response code is not 200, instead: ${response.code}",
                    IOException("Response code is not 200, instead: ${response.code}")
                )
            }
        } catch (e: Exception) {
            return LoginAuthResult.Failure("Error occurred during login: ${e.message}", e)
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
     * @param response OkHttp response after logging in
     * @return [LoginAuthResult] indicating the result of the login attempt
     */
    private fun tackleLoggedInPage(response: Response): LoginAuthResult {
        try {
            // Parse the HTML document
            val doc: Document = Jsoup.parse(response.body.string())

            // Find the element containing the error message
            val errorMsg = doc.select("div.msg_inner.error_msg p").first()?.text()

            // Extract cookies when login is successful
            if (errorMsg == null) {
                val cookiesHeader = response.headers("Set-Cookie")
                _cookies = cookiesHeader.flatMap { HttpCookie.parse(it) }
                return LoginAuthResult.Success(_cookies)
            }

            // Check if user exists
            if (errorMsg.contains("用户不存在"))
                return LoginAuthResult.UserNotFoundDenial(errorMsg)

            // Check if password is incorrect
            "密码或安全提问第(\\d+)次错误, 您最多有(\\d+)次机会重试".toRegex().find(errorMsg)?.let { matchResult ->
                return tacklePasswordIncorrect(matchResult)
            }

            // Or else, error message is unrecognized
            return LoginAuthResult.UnknownDenial(errorMsg)

        } catch (e: Exception) {
            // Handle any exceptions that might occur during handling logged-in page
            return LoginAuthResult.Failure("Error occurred during handling logged-in page: ${e.message}", e)
        }
    }

    private fun tacklePasswordIncorrect(matchResult: MatchResult): LoginAuthResult {
        // Use regular expression to extract failure count and max retry count
        // Extract and convert the numeric values
        val failingTimes = matchResult.groupValues[1].toIntOrNull() ?: return LoginAuthResult.Failure(
            "Failing times not found",
            RuntimeException("Failing times not found")
        )
        val maxRetries = matchResult.groupValues[2].toIntOrNull() ?: return LoginAuthResult.Failure(
            "Max retries not found",
            RuntimeException("Max retries not found")
        )

        // Return the extracted information as a pair
        return LoginAuthResult.PasswordIncorrectDenial(failingTimes, maxRetries)
    }
}