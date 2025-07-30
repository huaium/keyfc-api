package net.keyfc.api.model

import java.net.HttpCookie

sealed class AuthResult {
    data class Success(val cookies: List<HttpCookie>) : AuthResult()

    data class PasswordIncorrectDenial(val failingTimes: Int, val maxRetries: Int) : AuthResult()

    data class UserNotFoundDenial(val message: String) : AuthResult()

    data class UnknownDenial(val message: String) : AuthResult()

    data class Failure(val exception: Exception) : AuthResult()
}