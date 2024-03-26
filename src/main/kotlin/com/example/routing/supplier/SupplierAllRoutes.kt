package com.example.routing.supplier

import io.ktor.server.routing.*

fun Route.supplierAllRoutes(){
    supplierAuthRoutes()
    supplierRoutes()
}