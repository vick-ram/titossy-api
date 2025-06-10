package com.example.routing

import com.example.commons.ApiResponse
import com.example.controllers.getMetrics
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.application.*
import io.ktor.server.routing.get

fun Route.fetchMetrics() {
    route("/api/metrics") {
        get {
            val metrics = getMetrics()
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.Created,
                    metrics,
                    "Metrics fetched successfully"
                )
            )
        }
    }
}