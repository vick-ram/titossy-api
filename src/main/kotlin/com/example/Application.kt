package com.example

import com.example.db.util.DatabaseUtil
import com.example.plugins.auth.configureAuthentication
import com.example.plugins.cors.configurecORS
import com.example.plugins.error.configureException
import com.example.plugins.routing.configureRouting
import com.example.plugins.serialization.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseUtil.init()
    configureSerialization()
    configureAuthentication()
    configureRouting()
    configureException()
//    configureWebSockets()
    configurecORS()
}
