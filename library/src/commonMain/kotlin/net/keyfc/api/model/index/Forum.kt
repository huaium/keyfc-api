package net.keyfc.api.model.index

data class Forum(
    val name: String,
    val id: String,
    val subForums: List<Forum> = emptyList()
)