package net.keyfc.api.model.page

data class Pagination(
    val currentPage: Int,
    val totalPages: Int,
    val hasNextPage: Boolean,
    val hasPreviousPage: Boolean,
    val nextPageLink: String?,
    val previousPageLink: String?
)