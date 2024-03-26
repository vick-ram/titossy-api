package com.example.routing.customer

import io.ktor.server.routing.*

fun Route.customerAllRoutes() {
    customerAuthRoutes()
    customerRoutes()
}