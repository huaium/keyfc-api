package net.keyfc.api

import net.keyfc.api.model.forum.Topic
import net.keyfc.api.model.index.Forum
import net.keyfc.api.parser.*
import java.net.HttpCookie

class KeyfcClient : AutoCloseable {

    private val repoClient by lazy { RepoClient() }

    override fun close() {
        repoClient.close()
    }

    suspend fun fetchIndex(cookies: List<HttpCookie> = emptyList()) = IndexParser.parse(repoClient, cookies)

    suspend fun fetchForum(id: String, cookies: List<HttpCookie> = emptyList()) =
        ForumParser.parse(repoClient, id, cookies)

    suspend fun fetchForum(forum: Forum, cookies: List<HttpCookie> = emptyList()) =
        ForumParser.parse(repoClient, forum, cookies)

    suspend fun fetchTopic(id: String, cookies: List<HttpCookie> = emptyList()) =
        TopicParser.parse(repoClient, id, cookies)

    suspend fun fetchTopic(topic: Topic, cookies: List<HttpCookie> = emptyList()) =
        TopicParser.parse(repoClient, topic, cookies)

    suspend fun search(keyword: String, cookies: List<HttpCookie> = emptyList()) =
        SearchParser.search(repoClient, keyword, cookies)

    suspend fun fetchUc(cookies: List<HttpCookie> = emptyList()) =
        UcParser.parse(repoClient, cookies)

    suspend fun fetchNotifications(cookies: List<HttpCookie> = emptyList(), filter: String = "all") =
        NotificationsParser.parse(repoClient, cookies, filter)

    suspend fun fetchInbox(cookies: List<HttpCookie> = emptyList()) =
        InboxParser.parse(repoClient, cookies)

    suspend fun fetchMyTopics(cookies: List<HttpCookie> = emptyList()) =
        MyTopicsParser.parse(repoClient, cookies)

    suspend fun fetchMyPosts(cookies: List<HttpCookie> = emptyList()) =
        MyPostsParser.parse(repoClient, cookies)

    suspend fun fetchFavourites(cookies: List<HttpCookie> = emptyList()) =
        FavouritesParser.parse(repoClient, cookies)
}