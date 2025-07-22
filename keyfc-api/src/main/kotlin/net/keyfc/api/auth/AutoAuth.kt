package net.keyfc.api.auth

import net.keyfc.api.result.ManualAuthResult
import java.net.HttpCookie

/**
 * Handles automatic re-login when cookies expire.
 *
 * Wraps a [ManualAuth] instance and manages cookie expiration automatically.
 */
class AutoAuth(username: String, password: String) {

    // The underlying manual auth handler
    private val manualAuth = ManualAuth(username, password)

    val isLoggedInValid: Boolean
        get() = manualAuth.isLoggedInValid

    val isLoggedIn: Boolean
        get() = manualAuth.isLoggedIn

    /**
     * Get the current cookies, attempting to refresh them if expired.
     *
     * Remind that cookies returned can be empty when [autoLogin] is false, so it is recommended to check [isLoggedIn].
     *
     * @return List of valid HttpCookie objects
     * @throws [RuntimeException] if it cannot return valid cookies
     */
    suspend fun getCookies(autoLogin: Boolean = true): List<HttpCookie> {
        // If cookies are valid, or auto login is disabled, return them immediately
        if (manualAuth.isLoggedInValid || !autoLogin) {
            return manualAuth.getCookies()
        }

        // Otherwise, try to log in again
        val result = refreshLogin()
        return when (result) {
            is ManualAuthResult.Success -> result.cookies
            is ManualAuthResult.PasswordIncorrectDenial -> throw RuntimeException("Password incorrect. Failing times: ${result.failingTimes}; Max retries: ${result.maxRetries}")
            is ManualAuthResult.UserNotFoundDenial -> throw RuntimeException(result.message)
            is ManualAuthResult.UnknownDenial -> throw RuntimeException(result.message)
            is ManualAuthResult.Failure -> throw RuntimeException("Login failed")
        }
    }

    /**
     * Force a new login attempt regardless of cookie state.
     *
     * @return [ManualAuthResult] indicating the outcome of the login attempt
     */
    suspend fun refreshLogin(): ManualAuthResult {
        return manualAuth.login()
    }

    /**
     * Clear all stored cookies and log out.
     */
    fun logout() {
        manualAuth.logout()
    }
}