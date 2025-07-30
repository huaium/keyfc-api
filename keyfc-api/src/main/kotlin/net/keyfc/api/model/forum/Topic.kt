package net.keyfc.api.model.forum

data class Topic(
    val title: String,
    val id: String,
    val replyCount: Int? // May be null when some situations like parsing breadcrumbs
)