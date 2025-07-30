package net.keyfc.api.parser

import net.keyfc.api.RepoClient
import net.keyfc.api.RepoClient.Companion.BASE_URL
import net.keyfc.api.model.mytopics.MyTopicsPage
import java.net.HttpCookie

/**
 * It reuses [MyPostsParser], as both components share the same data structure.
 */
internal object MyTopicsParser {

    private const val MY_TOPICS_URL = BASE_URL + "mytopics.aspx"

    /**
     * Retrieves and parses the My Topics page.
     *
     * @param repoClient The repository client to use for HTTP requests
     * @param cookies The cookies to include in the request
     *
     * @return [Result] containing [MyTopicsPage] if successful, or an error if parsing fails
     */
    suspend fun parse(
        repoClient: RepoClient,
        cookies: List<HttpCookie> = emptyList(),
    ): Result<MyTopicsPage> = MyPostsParser.parse(repoClient, MY_TOPICS_URL, cookies).mapCatching { myPostsPage ->
        MyTopicsPage(
            myPostsPage.document,
            myPostsPage.pageInfo,
            myPostsPage.posts,
            myPostsPage.pagination,
        )
    }
}