package com.example.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class TokePair(val accessToken: String, val refreshToken: String)

fun generateTokens(username: String, secret: String, issuer: String, audience: String): TokePair {
    val algorithm = Algorithm.HMAC256(secret)
    val now = System.currentTimeMillis()
    val accessToken = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("username", username)
        .withExpiresAt(Date(now + 3600000))
        .sign(algorithm)

    val refreshToken = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("username", username)
        .withExpiresAt(Date(now + 604800000))
        .sign(algorithm)

    return TokePair(accessToken, refreshToken)
}