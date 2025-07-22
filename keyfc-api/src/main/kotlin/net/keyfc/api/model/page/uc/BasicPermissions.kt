package net.keyfc.api.model.page.uc

data class BasicPermissions(
    val forumAccess: Boolean,
    val readPermissionLevel: Int,
    val viewUserProfiles: Boolean,
    val searchCapability: SearchCapability,
    val messageInboxCapacity: Int
)