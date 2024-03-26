package com.example.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

fun generateTokens(email: String, secret: String, issuer: String, audience: String): String {
    return JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("email", email)
        .withExpiresAt(Date(System.currentTimeMillis() + 31_536_000_000))
        .sign(Algorithm.HMAC256(secret))
}

//generate tokens for employee with roles
fun generateEmployeeTokens(email: String, secret: String, issuer: String, audience: String, role: String): String {
    return JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("email", email)
        .withClaim("role", role)
        .withExpiresAt(Date(System.currentTimeMillis() + 31_536_000_000))
        .sign(Algorithm.HMAC256(secret))
}