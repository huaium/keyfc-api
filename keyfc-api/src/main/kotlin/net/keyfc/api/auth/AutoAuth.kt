package net.keyfc.api.auth

import net.keyfc.api.model.result.ManualAuthResult
import java.net.HttpCookie

/**
 * Handles automatic re-login when cookies expire.
 *
 * Wraps a [ManualAuth] instance and manages cookie expiration automatically.
 */
class AutoAuth(username: String, password: String) {

    // The underlying manual auth handler
    private val manualAuth = ManualAuth(username, password)

    /**
     * Get the current cookies, attempting to refresh them if expired.
     *
     * @return List of valid HttpCookie objects
     */
    suspend fun getCookies(): List<HttpCookie> {
        // If cookies are valid, return them immediately
        if (manualAuth.isLoggedInValid) {
            return manualAuth.getCookies()
        }

        // Otherwise, try to log in again
        val result = refreshLogin()
        return when (result) {
            is ManualAuthResult.Success -> result.cookies
            else -> throw RuntimeException("Failed to refresh login")
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