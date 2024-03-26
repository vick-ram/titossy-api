package com.example.routing.order

import com.example.commands.queries.order.*
import com.example.models.order.OrderRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

fun Route.orderRoutes() {
    route("/order") {
        post {
            val orderRequest = call.receive<OrderRequest>()
            val createOrder = createOrder(orderRequest)
            createOrder?.let {
                call.respond(it)
            } ?: run {
                call.respond("Failed to create order")
            }
        }

        get {
            val orders = getOrders()
            try {
                call.respond(orders)
            } catch (e: Exception) {
                call.respond("Failed to get orders")
            }
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond("Invalid id")
            val order = getOrderById(id)
            order?.let {
                call.respond(it)
            } ?: run {
                call.respond("Order not found")
            }
        }

        get("/employee/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond("Invalid id")
            val orders = getOrdersByEmployeeId(id)
            try {
                call.respond(orders)
            } catch (e: Exception) {
                call.respond("Failed to get orders")
            }
        }

        get("/supplier/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond("Invalid id")
            val orders = getOrdersBySupplierId(id)
            try {
                call.respond(orders)
            } catch (e: Exception) {
                call.respond("Failed to get orders")
            }
        }

        get("/status/{status}") {
            val status = call.parameters["status"] ?: return@get call.respond("Invalid status")
            val orders = getOrdersByStatus(status)
            try {
                call.respond(orders)
            } catch (e: Exception) {
                call.respond("Failed to get orders")
            }
        }

        get("/byDate/{date}") {
            val dateString = call.parameters["date"]
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date = LocalDate.parse(dateString, formatter)
            val orderResponses = getOrdersByDate(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            if (!orderResponses.isNullOrEmpty()) {
                call.respond(HttpStatusCode.OK, orderResponses)
            } else {
                call.respond(HttpStatusCode.NotFound, "No orders found for the given date")
            }
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond("Invalid id")
            val status = call.receive<String>()
            val orderToUpdate = updateOrderStatus(id, status)
            orderToUpdate?.let {
                call.respond(it)
            } ?: run {
                call.respond("Failed to update order status")
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond("Invalid id")
            val orderToDelete = deleteOrder(id)
            orderToDelete.let {
                call.respond(it)
            }
        }
    }
}