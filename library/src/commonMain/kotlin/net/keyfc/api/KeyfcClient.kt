package net.keyfc.api

import io.ktor.http.Cookie
import net.keyfc.api.model.AuthResult
import net.keyfc.api.model.forum.Topic
import net.keyfc.api.model.index.Forum
import net.keyfc.api.parser.FavouritesParser
import net.keyfc.api.parser.ForumParser
import net.keyfc.api.parser.InboxParser
import net.keyfc.api.parser.IndexParser
import net.keyfc.api.parser.MyPostsParser
import net.keyfc.api.parser.MyTopicsParser
import net.keyfc.api.parser.NotificationsParser
import net.keyfc.api.parser.SearchParser
import net.keyfc.api.parser.TopicParser
import net.keyfc.api.parser.UcParser

class KeyfcClient : AutoCloseable {

    private val repoClient by lazy { RepoClient() }

    private var authClient: AuthClient? = null

    /**
     * Cookies received after successful login, or empty if not logged in.
     */
    val cookies: List<Cookie>
        get() = authClient?.cookies ?: emptyList()

    /**
     * Indicates if a user is currently logged in, with valid cookies.
     */
    val isLoggedInValid: Boolean
        get() = authClient?.isLoggedInValid ?: false

    /**
     * Indicates if a user is logged in, regardless of cookie validity.
     */
    val isLoggedIn: Boolean
        get() = authClient?.isLoggedIn ?: false

    /**
     * Clears the current authentication state, and closes the underlying repository client.
     *
     * Notice: this function implements the [AutoCloseable] interface.
     */
    override fun close() {
        authClient?.logout()
        authClient = null
        repoClient.close()
    }

    /**
     * Logs in with the provided username and password.
     *
     * @param username The username to log in with
     * @param password The password for the user
     *
     * @return [AuthResult] indicating the success or failure of the login attempt
     */
    suspend fun login(username: String, password: String): AuthResult {
        authClient = AuthClient(username, password)
        return authClient!!.login()
    }

    /**
     * Logs out the current user, clearing the authentication state.
     */
    fun logout() {
        authClient?.logout()
        authClient = null
    }

    suspend fun fetchIndex(cookies: List<Cookie> = this.cookies) =
        IndexParser.parse(repoClient, cookies)

    suspend fun fetchForum(id: String, cookies: List<Cookie> = this.cookies) =
        ForumParser.parse(repoClient, id, cookies)

    suspend fun fetchForum(forum: Forum, cookies: List<Cookie> = this.cookies) =
        ForumParser.parse(repoClient, forum, cookies)

    suspend fun fetchTopic(id: String, cookies: List<Cookie> = this.cookies) =
        TopicParser.parse(repoClient, id, cookies)

    suspend fun fetchTopic(topic: Topic, cookies: List<Cookie> = this.cookies) =
        TopicParser.parse(repoClient, topic, cookies)

    suspend fun search(keyword: String, cookies: List<Cookie> = this.cookies) =
        SearchParser.search(repoClient, keyword, cookies)

    suspend fun fetchUc(cookies: List<Cookie> = this.cookies) =
        UcParser.parse(repoClient, cookies)

    suspend fun fetchNotifications(cookies: List<Cookie> = this.cookies, filter: String = "all") =
        NotificationsParser.parse(repoClient, cookies, filter)

    suspend fun fetchInbox(cookies: List<Cookie> = this.cookies) =
        InboxParser.parse(repoClient, cookies)

    suspend fun fetchMyTopics(cookies: List<Cookie> = this.cookies) =
        MyTopicsParser.parse(repoClient, cookies)

    suspend fun fetchMyPosts(cookies: List<Cookie> = this.cookies) =
        MyPostsParser.parse(repoClient, cookies)

    suspend fun fetchFavourites(cookies: List<Cookie> = this.cookies) =
        FavouritesParser.parse(repoClient, cookies)
}