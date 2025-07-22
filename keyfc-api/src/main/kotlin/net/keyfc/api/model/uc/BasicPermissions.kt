package net.keyfc.api.model.uc

data class BasicPermissions(
    val forumAccess: Boolean,
    val readPermissionLevel: Int,
    val viewUserProfiles: Boolean,
    val searchCapability: SearchCapability,
    val messageInboxCapacity: Int
)