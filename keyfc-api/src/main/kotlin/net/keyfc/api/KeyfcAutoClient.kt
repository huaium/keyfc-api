package net.keyfc.api

import net.keyfc.api.auth.AutoAuth
import net.keyfc.api.model.page.forum.Topic
import net.keyfc.api.model.page.index.Forum

class KeyfcAutoClient(username: String, password: String) : AutoCloseable {

    private val autoAuth = AutoAuth(username, password)

    private val keyfcClient = KeyfcClient()

    override fun close() {
        keyfcClient.close()
    }

    suspend fun fetchIndex() = keyfcClient.fetchIndex(autoAuth.getCookies())

    suspend fun fetchForum(id: String) = keyfcClient.fetchForum(id, autoAuth.getCookies())

    suspend fun fetchForum(forum: Forum) = keyfcClient.fetchForum(forum, autoAuth.getCookies())

    suspend fun fetchTopic(id: String) = keyfcClient.fetchTopic(id, autoAuth.getCookies())

    suspend fun fetchTopic(topic: Topic) = keyfcClient.fetchTopic(topic, autoAuth.getCookies())

    fun logout() {
        autoAuth.logout()
    }
}