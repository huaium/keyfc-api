package net.keyfc.api.model.page.index

data class Category(
    val name: String,
    val link: String,
    val forums: List<Forum>
)