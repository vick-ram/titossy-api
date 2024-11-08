package com.example.commons

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm

fun makeJWTVerifier(
    secret: String,
    audience: String,
    issuer: String
): JWTVerifier {
    return JWT
        .require(Algorithm.HMAC256(secret))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()
}