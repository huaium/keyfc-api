package net.keyfc.api

import net.keyfc.api.model.page.forum.Topic
import net.keyfc.api.model.page.index.Forum
import net.keyfc.api.parser.ForumParser
import net.keyfc.api.parser.IndexParser
import net.keyfc.api.parser.SearchParser
import net.keyfc.api.parser.TopicParser
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
}