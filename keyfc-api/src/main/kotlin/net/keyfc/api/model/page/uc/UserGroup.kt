package net.keyfc.api.model.page.uc

data class UserGroup(
    val name: String, // e.g., 永远光辉的季节
    val level: Int, // e.g., 1
    val type: String, // e.g., 会员用户组
    val startingPoints: Int, // e.g., 0
    val readPermissionLevel: Int, // e.g., 10
    val expirationTime: String? // null if no expiration
)