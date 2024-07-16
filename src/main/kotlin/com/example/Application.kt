package com.example

import com.example.db.util.DatabaseUtil
import com.example.plugins.auth.configureAuthentication
import com.example.plugins.cors.configurecORS
import com.example.plugins.error.configureException
import com.example.plugins.routing.configureRouting
import com.example.plugins.serialization.configureSerialization
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.resources.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val url = environment.config.property("storage.url").getString()
    val driver = environment.config.property("storage.driver").getString()
    val user = environment.config.property("storage.user").getString()
    val password = environment.config.property("storage.password").getString()

    val imgHippoApiKey = environment.config.property("imgHippo.apiKey").getString()
    val imgHippoUrl = environment.config.property("imgHippo.url").getString()

    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val realm = environment.config.property("jwt.realm").getString()

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
        }
        install(UserAgent) {
            agent = "Ktor-server-client"
        }
    }
    DatabaseUtil.init(url,driver, user, password)
    configureException()
    configureSerialization()
    configureAuthentication(
        secret = secret,
        issuer = issuer,
        jwtRealm = realm
    )
    install(Resources)
    configureRouting(client, imgHippoApiKey, imgHippoUrl, secret, issuer, audience)
    configurecORS()
}
