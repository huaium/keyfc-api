package net.keyfc.api.model.result

import java.net.HttpCookie

sealed class ManualAuthResult {
    data class Success(val cookies: List<HttpCookie>) : ManualAuthResult()

    data class PasswordIncorrectDenial(val failingTimes: Int, val maxRetries: Int) : ManualAuthResult()

    data class UserNotFoundDenial(val message: String) : ManualAuthResult()

    data class UnknownDenial(val message: String) : ManualAuthResult()

    data class Failure(val message: String, val exception: Exception) : ManualAuthResult()
}