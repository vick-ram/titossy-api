package com.example.commons

import java.util.UUID

fun shortUUID(): String {
    return UUID.randomUUID()
        .toString()
        .substring(0, 10)
}