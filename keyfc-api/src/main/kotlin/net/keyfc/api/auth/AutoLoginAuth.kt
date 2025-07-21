package net.keyfc.api.auth

import net.keyfc.api.model.result.LoginAuthResult
import java.net.HttpCookie

/**
 * Handles automatic re-login when cookies expire.
 *
 * Wraps a [LoginAuth] instance and manages cookie expiration automatically.
 */
class AutoLoginAuth(
    username: String,
    password: String
) {
    // The underlying login auth handler
    private val loginAuth = LoginAuth(username, password)

    /**
     * Get the current cookies, attempting to refresh them if expired.
     *
     * @return List of valid HttpCookie objects
     */
    suspend fun getCookies(): List<HttpCookie> {
        // If cookies are valid, return them immediately
        if (loginAuth.isLoggedIn) {
            return loginAuth.cookies
        }

        // Otherwise, try to log in again
        val result = refreshLogin()
        return when (result) {
            is LoginAuthResult.Success -> result.cookies
            else -> throw RuntimeException("Failed to refresh login")
        }
    }

    /**
     * Force a new login attempt regardless of cookie state.
     *
     * @return LoginAuthResult indicating the outcome of the login attempt
     */
    suspend fun refreshLogin(): LoginAuthResult {
        return loginAuth.login()
    }

    /**
     * Clear all stored cookies and log out.
     */
    fun logout() {
        loginAuth.logout()
    }
}