package net.keyfc.api.model

data class Pagination(
    val currentPage: Int,
    val totalPages: Int,
    val hasNextPage: Boolean,
    val hasPreviousPage: Boolean,
    val nextPageLink: String?,
    val previousPageLink: String?
)