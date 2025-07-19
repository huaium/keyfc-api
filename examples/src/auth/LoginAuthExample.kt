package auth

import net.keyfc.api.ApiApplication
import net.keyfc.api.auth.LoginAuth
import net.keyfc.api.ext.toHeaderString
import net.keyfc.api.model.result.LoginAuthResult
import java.net.HttpURLConnection
import java.net.URL

/**
 * Example demonstrating how to use the LoginAuth class for authenticating.
 */
fun main() {
    // Read credentials from stdin
    print("Enter username: ")
    val username = readLine() ?: ""

    print("Enter password: ")
    val password = readLine() ?: ""

    // Create LoginAuth instance
    val auth = LoginAuth(username, password)

    println("Attempting to login with username: $username")

    // Attempt login
    val result = auth.login()

    when (result) {
        is LoginAuthResult.Success -> {
            // Login successful
            println("Login successful!")
            println("Login status: ${auth.isLoggedIn}")

            // Display cookies
            println("\nCookies received:")
            result.cookies.forEach { cookie ->
                println("- ${cookie.name}: ${cookie.value}")
            }

            // Display cookie header that can be used in subsequent requests
            println("\nCookie header for subsequent requests:")
            val cookieHeader = result.cookies.toHeaderString()
            println(cookieHeader)

            // Test cookie usage in a subsequent request
            println("\nPerforming a test request using the login cookies...")
            testLoggedInRequest(cookieHeader)

            // Logout
            println("\nLogging out...")
            auth.logout()
            println("Login status after logout: ${auth.isLoggedIn}")
        }

        is LoginAuthResult.PasswordIncorrectDenial -> {
            // Password incorrect
            println("Login failed: Password incorrect")
            println("Failing times: ${result.failingTimes}")
            println("Max retries: ${result.maxRetries}")
        }

        is LoginAuthResult.UserNotFoundDenial -> {
            // User not found
            println("Login failed: User not found")
        }

        is LoginAuthResult.UnknownDenial -> {
            // Unknown failure
            println("Login failed: Unknown")
        }

        is LoginAuthResult.Failure -> {
            // Other failure
            println("Login failed: ${result.message}")
        }
    }
}

/**
 * Check the response line by line for "欢迎回来~" to verify login status.
 */
private fun testLoggedInRequest(cookieHeader: String) {
    try {
        // Create a connection to a page that requires login
        val url = "https://keyfc.net/bbs/usercp.aspx"
        val connection = URL(url).openConnection() as HttpURLConnection

        // Set the cookie header with our login cookies
        connection.setRequestProperty("Cookie", cookieHeader)
        connection.setRequestProperty("User-Agent", ApiApplication.USER_AGENT)

        // Get response code
        val responseCode = connection.responseCode
        println("Response Code: $responseCode")

        // Read the response line by line to check if we're logged in
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = connection.inputStream.bufferedReader()
            var line: String?
            var loginSuccessful = false

            while (reader.readLine().also { line = it } != null) {
                if (line?.contains("欢迎回来~") == true) {
                    loginSuccessful = true
                    break
                }
            }

            if (loginSuccessful) {
                println("Login status test SUCCESS")
            } else {
                println("Login status test FAILED - Welcome back message not found")
            }
        }
    } catch (e: Exception) {
        println("Error during test request: ${e.message}")
    }
}