package com.example

import com.example.commons.DatabaseUtil
import com.example.plugins.*
import com.example.routing.configureWebsocket
import io.ktor.client.*
import io.ktor.client.engine.cio.*
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

//    Imgbb
    val imgBbApiKey = environment.config.property("imgBb.apiKey").getString()
    val imgBbUrl = environment.config.property("imgBb.url").getString()

    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val realm = environment.config.property("jwt.realm").getString()

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }
    DatabaseUtil.init(
        url = url,
        driver = driver,
        /*        user = user,
                password = password*/
    )
    configureException()
    configureSerialization()
    configureAuthentication(
        secret = secret,
        issuer = issuer,
        audience = audience,
        jwtRealm = realm
    )
    install(Resources)
    configureRouting(
        client = client,
        imgHippoUrl = imgHippoUrl,
        imgHippoApiKey = imgHippoApiKey,
        imgBBUrl = imgBbUrl,
        imgBBApiKey = imgBbApiKey,
//        apiKey = imgHippoApiKey,
//        url = imgHippoUrl,
        secret = secret,
        issuer = issuer,
        audience = audience
    )
    configureCORS()
    configureWebsocket(secret, audience, issuer)
}
