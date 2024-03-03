package com.example.plugins

import com.example.routing.authentication
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        authentication()
    }
}

