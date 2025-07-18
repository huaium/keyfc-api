package net.keyfc.api.model.result

import java.net.HttpCookie

sealed class LoginAuthResult {
    data class Success(val cookies: List<HttpCookie>) : LoginAuthResult()

    data class PasswordIncorrectDenial(val failingTimes: Int, val maxRetries: Int) : LoginAuthResult()

    data class UserNotFoundDenial(val message: String) : LoginAuthResult()

    data class UnknownDenial(val message: String) : LoginAuthResult()

    data class Failure(val message: String, val exception: Exception) : LoginAuthResult()
}