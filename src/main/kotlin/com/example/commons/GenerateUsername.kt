package com.example.commons

import kotlin.random.Random

fun generateUsername(firstName: String): String {
    return Random.nextInt(100, 1000)
        .toString()
        .prependIndent(firstName)

}