package net.keyfc.api.auth

import net.keyfc.api.ApiConfig
import net.keyfc.api.ApiConfig.LOGIN_URL
import net.keyfc.api.ext.toFormData
import net.keyfc.api.model.result.LoginAuthResult
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpCookie
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * Handle cookie based login operations.
 */
class LoginAuth {
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
        get() = _cookies.isNotEmpty()

    /**
     * Login to the forum with provided username and password.
     *
     * @return True if login successful, false otherwise
     */
    fun login(username: String, password: String): LoginAuthResult {
        val loginUrl = "${LOGIN_URL}?stamp=${Math.random()}"

        val connection = URL(loginUrl).openConnection() as HttpURLConnection

        try {
            // Setup connection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.doInput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.setRequestProperty("User-Agent", ApiConfig.USER_AGENT)

            // Build form parameters
            val formParams = mapOf(
                "username" to username,
                "password" to password,
                "templateid" to TEMPLATE_ID,
                "login" to LOGIN,
                "expires" to DEFAULT_EXPIRES
            )

            // Write form parameters
            OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { writer ->
                writer.write(formParams.toFormData())
                writer.flush()
            }

            // Get response code
            val responseCode = connection.responseCode

            // If login successful, store cookies
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return tackleLoggedInPage(connection)
            }

            return LoginAuthResult.Failure(
                "Response code is not 200, instead: $responseCode",
                IOException("Response code is not 200, instead: $responseCode")
            )
        } catch (e: Exception) {
            return LoginAuthResult.Failure("Error occurred during login: ${e.message}", e)
        } finally {
            connection.disconnect()
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
     * @param connection HTTP connection after logging in
     * @return [LoginAuthResult] indicating the result of the login attempt
     */
    fun tackleLoggedInPage(connection: HttpURLConnection): LoginAuthResult {
        val html = connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }

        try {
            // Parse the HTML document
            val doc: Document = Jsoup.parse(html)

            // Find the element containing the error message
            val errorMsg = doc.select("div.msg_inner.error_msg p").first()?.text()

            // Extract cookies when login is successful
            if (errorMsg == null) {
                val cookiesHeader = connection.headerFields["Set-Cookie"]
                _cookies = cookiesHeader?.flatMap { HttpCookie.parse(it) } ?: emptyList()
                return LoginAuthResult.Success(_cookies)
            }

            // Check if user exists
            if (errorMsg.contains("用户不存在"))
                return LoginAuthResult.UserNotFound(errorMsg)

            // Use regular expression to extract failure count and max retry count
            val pattern = "密码或安全提问第(\\d+)次错误, 您最多有(\\d+)次机会重试".toRegex()
            val matchResult = pattern.find(errorMsg)
                ?: return LoginAuthResult.Failure(
                    "Pattern not found: $pattern",
                    RuntimeException("Pattern not found: $pattern")
                )

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
            return LoginAuthResult.PasswordIncorrect(failingTimes, maxRetries)
        } catch (e: Exception) {
            // Handle any exceptions that might occur during handling logged-in page
            return LoginAuthResult.Failure("Error occurred during handling logged-in page: ${e.message}", e)
        }
    }
}