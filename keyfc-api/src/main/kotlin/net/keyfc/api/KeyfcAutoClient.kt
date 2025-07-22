package net.keyfc.api

import net.keyfc.api.auth.AutoAuth
import net.keyfc.api.model.forum.Topic
import net.keyfc.api.model.index.Forum
import net.keyfc.api.result.FetchResult
import java.net.HttpCookie

class KeyfcAutoClient(username: String, password: String) : AutoCloseable {

    private val _autoAuth = AutoAuth(username, password)

    val autoAuto: AutoAuth
        get() = _autoAuth

    private val keyfcClient = KeyfcClient()

    override fun close() {
        keyfcClient.close()
    }

    suspend fun fetchIndex(autoLogin: Boolean = true) =
        fetch(
            autoLogin = autoLogin,
            callWithCookies = { cookies ->
                keyfcClient.fetchIndex(cookies)
            },
            callWithoutCookies = {
                keyfcClient.fetchIndex()
            }
        )

    suspend fun fetchForum(id: String, autoLogin: Boolean = true) =
        fetch(
            autoLogin = autoLogin,
            callWithCookies = { cookies ->
                keyfcClient.fetchForum(id, cookies)
            },
            callWithoutCookies = {
                keyfcClient.fetchForum(id)
            }
        )

    suspend fun fetchForum(forum: Forum, autoLogin: Boolean = true) =
        fetchForum(forum.id, autoLogin)

    suspend fun fetchTopic(id: String, autoLogin: Boolean = true) =
        fetch(
            autoLogin = autoLogin,
            callWithCookies = { cookies ->
                keyfcClient.fetchTopic(id, cookies)
            },
            callWithoutCookies = {
                keyfcClient.fetchTopic(id)
            }
        )

    suspend fun fetchTopic(topic: Topic, autoLogin: Boolean = true) =
        fetchTopic(topic.id, autoLogin)

    suspend fun search(keyword: String, autoLogin: Boolean = true) =
        fetchWithCookies(
            autoLogin = autoLogin,
        ) { cookies ->
            keyfcClient.search(keyword, cookies)
        }

    suspend fun fetchUc(autoLogin: Boolean = true) =
        fetchWithCookies(
            autoLogin = autoLogin,
        ) { cookies ->
            keyfcClient.fetchUc(cookies)
        }

    suspend fun fetchNotifications(autoLogin: Boolean = true, filter: String = "all") =
        fetchWithCookies(
            autoLogin = autoLogin,
        ) { cookies ->
            keyfcClient.fetchNotifications(cookies, filter)
        }

    fun logout() {
        _autoAuth.logout()
    }

    /**
     * Generic wrapper for API calls that handles authentication logic and returns appropriate [FetchResult].
     *
     * This function encapsulates the common pattern of trying authenticated calls first, then falling back
     * to unauthenticated calls if needed.
     *
     * @param autoLogin Whether to attempt automatic login if needed
     * @param callWithCookies Function that performs the API call with authentication cookies
     * @param callWithoutCookies Function that performs the API call without authentication
     * @return [FetchResult] wrapping the API call result with appropriate login state
     */
    private suspend inline fun <T> fetch(
        autoLogin: Boolean = true,
        crossinline callWithCookies: suspend (cookies: List<HttpCookie>) -> T,
        crossinline callWithoutCookies: suspend () -> T
    ): FetchResult<T> {
        return try {
            fetchWithCookies(autoLogin, callWithCookies = callWithCookies)
        } catch (_: Exception) {
            try {
                // If authenticated call fails, try unauthenticated call
                val result = callWithoutCookies()
                FetchResult.WithoutCookies(result)
            } catch (e: Exception) {
                // Both approaches failed, return failure
                val message = "Both authenticated and unauthenticated calls failed: ${e.message}, ${e.message}"
                FetchResult.Failure(message, e)
            }
        }
    }

    private suspend inline fun <T> fetchWithCookies(
        autoLogin: Boolean = true,
        crossinline callWithCookies: suspend (cookies: List<HttpCookie>) -> T,
    ): FetchResult<T> {
        return try {
            val cookies = _autoAuth.getCookies(autoLogin)

            // Call API with cookies
            val result = callWithCookies(cookies)

            // Return result with appropriate login state
            if (cookies.isEmpty()) {
                FetchResult.WithoutCookies(result)
            }

            FetchResult.WithCookies(result, _autoAuth.isLoggedInValid)
        } catch (e: Exception) {
            val message = "Authenticated call failed: ${e.message}"
            FetchResult.Failure(message, e)
        }
    }
}