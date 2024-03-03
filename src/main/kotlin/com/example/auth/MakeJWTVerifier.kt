package com.example.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm

fun makeJWTVerifier(issuer: String, secret: String): JWTVerifier {
    return JWT
        .require(Algorithm.HMAC256(secret))
        .withIssuer(issuer)
        .build()
}