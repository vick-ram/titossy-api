package com.example.commons

fun generateRandomString(length: Int): String {
    val allowedCharacters = ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { allowedCharacters.random() }
        .joinToString("")
}