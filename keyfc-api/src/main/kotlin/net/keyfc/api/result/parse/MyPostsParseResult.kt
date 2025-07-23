package net.keyfc.api.result.parse

import net.keyfc.api.model.myposts.MyPostsPage

sealed class MyPostsParseResult {
    data class Success(
        val myPostsPage: MyPostsPage
    ) : MyPostsParseResult()

    data class PermissionDenial(
        val message: String
    ) : MyPostsParseResult()

    data class Failure(val message: String, val exception: Exception) : MyPostsParseResult()
}