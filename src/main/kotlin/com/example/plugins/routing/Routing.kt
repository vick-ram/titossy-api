package com.example.plugins.routing

import com.example.routing.booking.bookingRoute
import com.example.routing.customer.customerAllRoutes
import com.example.routing.employee.allEmployeeRoutes
import com.example.routing.employee.employeeAuthRoute
import com.example.routing.feedback.feedbackRoutes
import com.example.routing.order.orderRoutes
import com.example.routing.payment.paymentRoutes
import com.example.routing.product.productRoutes
import com.example.routing.service.serviceRoutes
import com.example.routing.supplier.supplierAllRoutes
import com.example.routing.supplier.supplierAuthRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        allEmployeeRoutes()
        customerAllRoutes()
        bookingRoute()
        supplierAllRoutes()
        serviceRoutes()
        productRoutes()
        paymentRoutes()
        orderRoutes()
        feedbackRoutes()
    }
}

