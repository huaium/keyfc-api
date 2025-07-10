package net.keyfc.api.model.page.index

data class Forum(
    val name: String,
    val link: String,
    val subForums: List<Forum> = emptyList()
)