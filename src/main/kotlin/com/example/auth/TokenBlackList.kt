package com.example.auth

object TokenBlackList {
    private val blacklistedTokens = mutableSetOf<String>()

    fun blacklistToken(token: String) {
        blacklistedTokens.add(token)
    }

    fun isTokenBlacklisted(token: String): Boolean {
        return blacklistedTokens.contains(token)
    }
}