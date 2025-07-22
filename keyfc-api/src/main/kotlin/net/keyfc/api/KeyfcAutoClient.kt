package net.keyfc.api

import net.keyfc.api.auth.AutoAuth
import net.keyfc.api.model.page.forum.Topic
import net.keyfc.api.model.page.index.Forum
import net.keyfc.api.model.result.FetchResult
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
        fetchResultWrapper(
            autoLogin = autoLogin,
            withAuthCall = { cookies ->
                keyfcClient.fetchIndex(cookies)
            },
            withoutAuthCall = {
                keyfcClient.fetchIndex()
            }
        )

    suspend fun fetchForum(id: String, autoLogin: Boolean = true) =
        fetchResultWrapper(
            autoLogin = autoLogin,
            withAuthCall = { cookies ->
                keyfcClient.fetchForum(id, cookies)
            },
            withoutAuthCall = {
                keyfcClient.fetchForum(id)
            }
        )

    suspend fun fetchForum(forum: Forum, autoLogin: Boolean = true) =
        fetchForum(forum.id, autoLogin)

    suspend fun fetchTopic(id: String, autoLogin: Boolean = true) =
        fetchResultWrapper(
            autoLogin = autoLogin,
            withAuthCall = { cookies ->
                keyfcClient.fetchTopic(id, cookies)
            },
            withoutAuthCall = {
                keyfcClient.fetchTopic(id)
            }
        )

    suspend fun fetchTopic(topic: Topic, autoLogin: Boolean = true) =
        fetchTopic(topic.id, autoLogin)

    suspend fun search(keyword: String, autoLogin: Boolean = true) =
        fetchResultWrapper(
            autoLogin = autoLogin,
            withAuthCall = { cookies ->
                keyfcClient.search(keyword, cookies)
            },
            withoutAuthCall = {
                keyfcClient.search(keyword)
            }
        )

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
     * @param withAuthCall Function that performs the API call with authentication cookies
     * @param withoutAuthCall Function that performs the API call without authentication
     * @return [FetchResult] wrapping the API call result with appropriate login state
     */
    private suspend inline fun <T> fetchResultWrapper(
        autoLogin: Boolean = true,
        crossinline withAuthCall: suspend (cookies: List<HttpCookie>) -> T,
        crossinline withoutAuthCall: suspend () -> T
    ): FetchResult<T> {
        return try {
            // Try to get authentication cookies
            val cookies = _autoAuth.getCookies(autoLogin)

            // Call API with cookies
            val result = withAuthCall(cookies)

            // Return result with appropriate login state
            if (cookies.isEmpty()) {
                FetchResult.WithoutCookies(result)
            }

            FetchResult.WithCookies(result, _autoAuth.isLoggedInValid)
        } catch (_: Exception) {
            try {
                // If authenticated call fails, try unauthenticated call
                val result = withoutAuthCall()
                FetchResult.WithoutCookies(result)
            } catch (e: Exception) {
                // Both approaches failed, return failure
                val message = "Both authenticated and unauthenticated calls failed: ${e.message}, ${e.message}"
                FetchResult.Failure(message, e)
            }
        }
    }

}