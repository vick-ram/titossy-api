package com.example.auth

object Config {
    val ISSUER = System.getenv("ISSUER") ?: "ktor.io"
    val SECRET = System.getenv("SECRET") ?: "ktor.io"
    val AUDIENCE = System.getenv("AUDIENCE") ?: "ktor.io"
    val REALM = System.getenv("REALM") ?: "ktor.io"
}