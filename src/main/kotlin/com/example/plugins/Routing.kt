package com.example.plugins

import com.example.routing.*
import io.ktor.client.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    client: HttpClient,
    imgHippoUrl: String,
    imgHippoApiKey: String,
    imgBBUrl: String,
    imgBBApiKey: String,
//    apiKey: String,
//    url: String,
    secret: String,
    issuer: String,
    audience: String
) {
    routing {
        employeeRoutes(secret, issuer, audience)
        customerRoutes(secret, issuer, audience)
        supplierRoutes(secret, issuer, audience)
        bookingRoute()
        assignBookingRoutes()
        serviceRoutes(client,imgHippoUrl, imgHippoApiKey, imgBBUrl, imgBBApiKey,)
        serviceAddonRoutes(client,imgHippoUrl, imgHippoApiKey, imgBBUrl, imgBBApiKey,)
        productRoutes(client,imgHippoUrl, imgHippoApiKey, imgBBUrl, imgBBApiKey,)
        customerPaymentRoutes()
        supplierPaymentRoutes()
        orderRoutes()
        feedbackRoutes()
        serviceCartRoutes()
        productCartRoutes()
        activitiesRoutes()
        messageRoute()
        fetchMetrics()
    }
}

