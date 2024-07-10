package com.example

import com.example.db.util.DatabaseUtil
import com.example.plugins.auth.configureAuthentication
import com.example.plugins.cors.configurecORS
import com.example.plugins.error.configureException
import com.example.plugins.routing.configureRouting
import com.example.plugins.serialization.configureSerialization
import com.pusher.pushnotifications.PushNotifications
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
    DatabaseUtil.init()
    configureException()
    configureSerialization()
    configureAuthentication()
    install(Resources)
    configureRouting(client)
    configurecORS()
}
