package com.example.plugins

import com.example.auth.Config.ISSUER
import com.example.auth.Config.REALM
import com.example.auth.Config.SECRET
import com.example.auth.makeJWTVerifier
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureAuthentication() {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(makeJWTVerifier(ISSUER, SECRET))
            realm = REALM
            validate { jwtCredential ->
                if (jwtCredential.payload.getClaim("username").asString() != null) {
                    JWTPrincipal(jwtCredential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "You need to login to access this resource")
            }
        }
    }
}