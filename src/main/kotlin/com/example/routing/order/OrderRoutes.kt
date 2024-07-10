package com.example.routing.order

import com.example.commands.queries.order.PurchaseOrderRepositoryImpl
import com.example.dao.PurchaseOrderRepository
import com.example.exceptions.ApiResponse
import com.example.models.purchase_order.OrderStatus
import com.example.models.purchase_order.PurchaseOrderRequest
import com.example.models.purchase_order.UpdateOrderStatus
import com.example.routing.util.Order
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.orderRoutes() {
    val orderImpl: PurchaseOrderRepository = PurchaseOrderRepositoryImpl()
    authenticate("auth-jwt") {
        post<Order, PurchaseOrderRequest> { _, order ->
            val principal = call.principal<JWTPrincipal>()
            val employeeId = principal?.subject
            val createOrder = employeeId?.let {
                orderImpl.createPurchaseOrder(
                    UUID.fromString(it),
                    order.validate()
                )
            }
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.Created,
                    createOrder,
                    "Order created successfully"
                )
            )
        }
    }

    get<Order> { query ->
        try {
            when {
                query.search != null -> call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        orderImpl.searchPurchaseOrder(query.search),
                        null
                    )
                )

                query.status != null -> call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        orderImpl.getFilteredPurchaseOrders {
                            it.orderStatus == OrderStatus.valueOf(query.status)
                        },
                        "Orders by status"
                    )
                )

                else -> call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        orderImpl.getFilteredPurchaseOrders { true },
                        "All orders"
                    )
                )
            }
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.BadRequest,
                        e.message
                    )
                )

                else -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.InternalServerError,
                        e.message
                    )
                )
            }
        }
    }

    get<Order.Id> { param ->
        try {
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    orderImpl.getFilteredPurchaseOrders { it.id.value == param.id },
                    "Order found"
                )
            )
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.BadRequest,
                        e.message
                    )
                )

                else -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.InternalServerError,
                        e.message
                    )
                )
            }
        }
    }

    authenticate("auth-jwt") {
        put<Order.Id, PurchaseOrderRequest> { param, orderUpdate ->
            try {
                val principal = call.principal<JWTPrincipal>()
                val employeeId = principal?.subject
                employeeId?.let {
                    orderImpl.updatePurchaseOrder(
                        UUID.fromString(it),
                        param.id,
                        orderUpdate.validate()
                    )
                }
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        null,
                        "Order updated successfully"
                    )
                )
            } catch (e: Exception) {
                when (e) {
                    is IllegalArgumentException -> call.respond(
                        ApiResponse.error(
                            HttpStatusCode.BadRequest,
                            e.message
                        )
                    )

                    else -> call.respond(
                        ApiResponse.error(
                            HttpStatusCode.InternalServerError,
                            e.message
                        )
                    )
                }
            }
        }
    }

    patch<Order.Id, UpdateOrderStatus> { param, orderStatus ->
        try {
            orderImpl.updatePurchaseOrderStatus(param.id, orderStatus)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    null,
                    "Order status updated successfully"
                )
            )
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.BadRequest,
                        e.message
                    )
                )

                else -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.InternalServerError,
                        e.message
                    )
                )
            }
        }

    }

    delete<Order.Id> { param ->
        try {
            orderImpl.deletePurchaseOrder(param.id)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    null,
                    "Order deleted successfully"
                )
            )
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.BadRequest,
                        e.message
                    )
                )

                else -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.InternalServerError,
                        e.message
                    )
                )
            }
        }
    }
}