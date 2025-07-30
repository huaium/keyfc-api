package net.keyfc.api.model.uc

data class UserGroup(
    val name: String,
    val level: Int,
    val type: String,
    val startingPoints: Int,
    val readPermissionLevel: Int,
    val expirationTime: String?
)