package com.example.routing.cart

import com.example.commands.queries.cart.ServiceCartRepositoryImpl
import com.example.dao.ServiceCartRepository
import com.example.exceptions.ApiResponse
import com.example.models.cart.AddServiceAddonToCart
import com.example.models.cart.AddServiceToCart
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.serviceCartRoutes() {
    val serviceCartDao: ServiceCartRepository = ServiceCartRepositoryImpl()

    authenticate("auth-jwt") {
        route("/api/cart") {
            post("/add-service") {
                val principal = call.principal<JWTPrincipal>()
                val customerId = principal?.subject?.let(UUID::fromString)
                if (customerId != null) {
                    val serviceCartRequest = call.receive<AddServiceToCart>()
                    try {
                        serviceCartDao.addServiceToCart(customerId, serviceCartRequest)
                        println("Received request: $serviceCartRequest")
                        call.respond(
                            ApiResponse.success(
                                HttpStatusCode.Created,
                                null,
                                "Service added to cart"
                            )
                        )
                    } catch (e: Exception) {
                        call.respond(
                            ApiResponse.error(
                                HttpStatusCode.InternalServerError,
                                e.message
                            )
                        )
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid user ID")
                }
            }
        }
    }


    authenticate("auth-jwt") {
        route("/api/cart") {
            post("/add-service-addon") {
                val principal = call.principal<JWTPrincipal>()
                val customerId = UUID.fromString(principal?.subject)
                val serviceCartRequest = call.receive<AddServiceAddonToCart>()
                try {
                    serviceCartDao.addServiceAddOnToCart(customerId, serviceCartRequest)
                    call.respond(
                        ApiResponse.success(
                            HttpStatusCode.Created,
                            null,
                            "Service add on added to cart"
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        ApiResponse.error(
                            HttpStatusCode.BadRequest,
                            e.message
                        )
                    )
                }
            }
        }
    }
    authenticate("auth-jwt") {
        get("/api/cart") {
            val principal = call.principal<JWTPrincipal>()
            val customerId = principal?.subject

            try {
                customerId?.let {
                    call.respond(
                        ApiResponse.success(
                            HttpStatusCode.OK,
                            serviceCartDao.getCart(UUID.fromString(it)),
                            null
                        )
                    )
                }
            } catch (e: Exception) {
                when (e) {
                    is IllegalArgumentException -> {
                        call.respond(
                            ApiResponse.error(
                                HttpStatusCode.BadRequest,
                                e.message
                            )
                        )
                    }

                    else -> {
                        call.respond(
                            ApiResponse.error(
                                HttpStatusCode.InternalServerError,
                                e.message
                            )
                        )
                    }
                }
            }
        }
    }

    authenticate("auth-jwt") {
        delete("/api/cart/{id}") {
            val principal = call.principal<JWTPrincipal>()
            val customerId = principal?.subject.let { UUID.fromString(it) }
            val serviceId = call.parameters["id"]?.let { UUID.fromString(it) }
                ?: throw BadRequestException("Invalid serviceAddonId")

            try {
                serviceCartDao.removeServiceFromCart(
                    customerId = customerId,
                    serviceId = serviceId
                )
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.NoContent,
                        null,
                        "Service removed successfully"
                    )
                )

            } catch (e: Exception) {
                when (e) {
                    is BadRequestException -> call.respond(
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
    authenticate("auth-jwt") {
        delete("/api/cart/addon/{id}") {
            val principal = call.principal<JWTPrincipal>()
            val customerId = principal?.subject.let { UUID.fromString(it) }
            val serviceAddonId = call.parameters["id"]?.let { UUID.fromString(it) }
                ?: throw BadRequestException("Invalid serviceAddonId")

            try {
                serviceCartDao.removeAddonFromCart(
                    customerId = customerId,
                    serviceAddon = serviceAddonId
                )
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.NoContent,
                        null,
                        "Service add-on removed successfully"
                    )
                )

            } catch (e: Exception) {
                when (e) {
                    is BadRequestException -> call.respond(
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

    authenticate("auth-jwt") {
        delete("/api/cart/clear") {
            val principal = call.principal<JWTPrincipal>()
            val customerId = principal?.subject.let { UUID.fromString(it) }

            try {
                serviceCartDao.clearCart(customerId)
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        null,
                        "Cart cleared successfully"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    ApiResponse.error(
                        HttpStatusCode.InternalServerError,
                        e.message
                    )
                )
            }
        }
    }
}
