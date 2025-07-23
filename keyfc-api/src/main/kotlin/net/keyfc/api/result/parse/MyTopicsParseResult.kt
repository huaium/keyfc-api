package net.keyfc.api.result.parse

import net.keyfc.api.model.mytopics.MyTopicsPage

sealed class MyTopicsParseResult {
    data class Success(
        val myTopicsPage: MyTopicsPage
    ) : MyTopicsParseResult()

    data class PermissionDenial(
        val message: String
    ) : MyTopicsParseResult()

    data class Failure(val message: String, val exception: Exception) : MyTopicsParseResult()
}