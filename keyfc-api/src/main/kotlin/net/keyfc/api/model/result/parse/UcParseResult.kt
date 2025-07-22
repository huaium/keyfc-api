package net.keyfc.api.model.result.parse

import net.keyfc.api.model.page.uc.UcPage
import net.keyfc.api.model.page.uc.UserStats
import net.keyfc.api.model.page.uc.UserPermissions
import net.keyfc.api.model.page.uc.UserGroup
import net.keyfc.api.model.page.uc.BasicPermissions
import net.keyfc.api.model.page.uc.PostPermissions
import net.keyfc.api.model.page.uc.AttachmentPermissions

sealed class UcParseResult {
    data class Success(
        val ucPage: UcPage
    ) : UcParseResult()

    data class PermissionDenial(
        val message: String,
    ) : UcParseResult()

    data class Failure(val message: String, val exception: Exception) : UcParseResult()
}