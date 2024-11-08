package com.example.commons

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

data class JwtPayload(
    val sub: String,
    val email: String,
    val role: String? = null,
    val exp: Date,
    val iss: String,
    val secret: String,
    val audience: String
)

fun generateToken(payload: JwtPayload): String {
    return JWT.create()
        .withSubject(payload.sub)
        .withAudience(payload.audience)
        .withIssuer(payload.iss)
        .withClaim("email", payload.email)
        .withExpiresAt(payload.exp)
        .sign(Algorithm.HMAC256(payload.secret))
}

fun generateEmployeeToken(payload: JwtPayload): String {
    return JWT.create()
        .withSubject(payload.sub)
        .withAudience(payload.audience)
        .withIssuer(payload.iss)
        .withClaim("email", payload.email)
        .withClaim("role", payload.role)
        .withExpiresAt(payload.exp)
        .sign(Algorithm.HMAC256(payload.secret))
}
