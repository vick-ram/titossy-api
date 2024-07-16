package com.example.plugins.routing

import com.example.routing.booking.assignBookingRoutes
import com.example.routing.booking.bookingRoute
import com.example.routing.cart.productCartRoutes
import com.example.routing.cart.serviceCartRoutes
import com.example.routing.customer.customerRoutes
import com.example.routing.employee.employeeRoutes
import com.example.routing.feedback.feedbackRoutes
import com.example.routing.order.orderRoutes
import com.example.routing.payment.customerPaymentRoutes
import com.example.routing.payment.supplierPaymentRoutes
import com.example.routing.product.productRoutes
import com.example.routing.service.serviceAddonRoutes
import com.example.routing.service.serviceRoutes
import com.example.routing.supplier.supplierRoutes
import io.ktor.client.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    client: HttpClient,
    apiKey: String,
    url: String,
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
        serviceRoutes(client, apiKey, url)
        serviceAddonRoutes(client, apiKey, url)
        productRoutes(client, apiKey, url)
        customerPaymentRoutes()
        supplierPaymentRoutes()
        orderRoutes()
        feedbackRoutes()
        serviceCartRoutes()
        productCartRoutes()
    }
}

