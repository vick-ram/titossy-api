package com.example.routing.payment

import com.example.commands.queries.payment.*
import com.example.models.payment.CustomerPaymentRequest
import com.example.models.payment.PaymentStatus
import com.example.models.payment.SupplierPaymentRequest
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.paymentRoutes() {
    route("/payment") {
        get {
            val allPayments = netPayments()
            try {
                call.respond(allPayments)
            } catch (e: Exception) {
                call.respond("Nothing")
            }
        }
        post("/customer") {
            val paymentRequest = call.receive<CustomerPaymentRequest>()
            try {
                val payment = makePayment(paymentRequest)
                payment?.let { call.respond(it) } ?: call.respond("Payment not made")
            } catch (e: Exception) {
                call.respond("Payment failed")
            }
        }

        get("/customer/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond("Invalid ID")
            val customerPayments = getCustomerPayment(id)
            customerPayments?.let {
                call.respond(it)
            } ?: call.respond("Payment not found")
        }
        get("/customer/booking/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond("Invalid ID")
            val customerPayments = getPaymentsByBooking(id)
            try {
                call.respond(customerPayments)
            } catch (e: Exception) {
                call.respond("No payments found")
            }
        }
        put("/customer/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond("Invalid ID")
            val status = call.receive<PaymentStatus>()
            val customerPayment = updateCustomerPaymentStatus(id, status)
            customerPayment?.let { call.respond(it) } ?: call.respond("Payment not found")
        }

        delete("/customer/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond("Invalid ID")
            val customerPayment = deletePayment(id)
            customerPayment.let {
                call.respond(it)
            }
        }

        post("/supplier") {
            val paymentRequest = call.receive<SupplierPaymentRequest>()
            try {
                val payment = makePayment(paymentRequest)
                payment?.let { call.respond(it) } ?: call.respond("Payment not made")
            } catch (e: Exception) {
                call.respond("Payment failed")
            }
        }

        get("/supplier/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond("Invalid ID")
            val supplierPayment = getSupplierPayment(id)
            supplierPayment?.let {
                call.respond(it)
            } ?: call.respond("Payment not found")
        }

        get("/supplier/order/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond("Invalid ID")
            val supplierPayments = getPaymentsByOrder(id)
            try {
                call.respond(supplierPayments)
            } catch (e: Exception) {
                call.respond("No payments found")
            }
        }

        put("/supplier/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond("Invalid ID")
            val status = call.receive<PaymentStatus>()
            val supplierPayment = updateSupplierPaymentStatus(id, status)
            supplierPayment?.let { call.respond(it) } ?: call.respond("Payment not found")
        }

        delete("/supplier/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond("Invalid ID")
            val supplierPayment = deleteSupplierPayment(id)
            supplierPayment.let {
                call.respond(it)
            }
        }
    }
}