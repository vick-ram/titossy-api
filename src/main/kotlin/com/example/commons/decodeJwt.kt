package com.example.commons

import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.interfaces.DecodedJWT

fun decodeJwt(token: String, jwtVerifier: JWTVerifier): DecodedJWT? {
    return try {
        jwtVerifier.verify(token)
    } catch (e: Exception) {
        println("Invalid token: ${e.message}")
        return null
    }
}