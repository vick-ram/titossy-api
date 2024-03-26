package com.example.routing.employee

import io.ktor.server.routing.*

fun Route.allEmployeeRoutes() {
    employeeRoutes()
    employeeAuthRoute()
}