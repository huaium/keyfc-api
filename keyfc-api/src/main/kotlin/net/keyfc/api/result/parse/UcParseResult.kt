package net.keyfc.api.result.parse

import net.keyfc.api.model.uc.UcPage

sealed class UcParseResult {
    data class Success(
        val ucPage: UcPage
    ) : UcParseResult()

    data class PermissionDenial(
        val message: String,
    ) : UcParseResult()

    data class Failure(val message: String, val exception: Exception) : UcParseResult()
}