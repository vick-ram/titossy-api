package com.example.auth

import com.example.exceptions.InvalidToken
import com.example.exceptions.TokenAlreadyBlacklisted

object TokenBlackList {
    private val blacklistedTokens = mutableSetOf<String>()

    fun blacklistToken(token: String) {
        if (token.isBlank()) {
            throw InvalidToken("You must be signed in to perform this action.")
        }
        if (isTokenBlacklisted(token)) {
            throw TokenAlreadyBlacklisted("You have already signed out.")
        }
        blacklistedTokens.add(token)
    }

    private fun isTokenBlacklisted(token: String): Boolean {
        return blacklistedTokens.contains(token)
    }
}
