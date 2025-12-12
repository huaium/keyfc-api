package net.keyfc.api.model.uc

data class UserPermissions(
    val userGroup: UserGroup,
    val basicPermissions: BasicPermissions,
    val postPermissions: PostPermissions,
    val attachmentPermissions: AttachmentPermissions
)