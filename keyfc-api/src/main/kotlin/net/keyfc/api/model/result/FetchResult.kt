package net.keyfc.api.model.result

sealed class FetchResult<T> {
    data class WithCookies<T>(val result: T, val isCookiesValid: Boolean) : FetchResult<T>()

    data class WithoutCookies<T>(val result: T) : FetchResult<T>()

    data class Failure<T>(val message: String, val exception: Exception) : FetchResult<T>()
}