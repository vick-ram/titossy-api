package com.example

import com.example.db.DatabaseUtil
import com.example.plugins.*
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
    configureWebSockets()
}
