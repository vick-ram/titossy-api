package com.example.routing.cart

import com.example.commands.queries.cart.addProductToCart
import com.example.commands.queries.cart.clearCart
import com.example.commands.queries.cart.getProductsInCart
import com.example.commands.queries.cart.removeProductFromCart
import com.example.exceptions.ApiResponse
import com.example.models.cart.ProductCart
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.productCartRoutes() {
    route("/api/product/cart") {
        authenticate("auth-jwt") {
            post {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val employeeId = principal?.subject
                    val product = call.receive<ProductCart>()
                    addProductToCart(UUID.fromString(employeeId), product)
                    call.respond(
                        ApiResponse.success(
                            HttpStatusCode.Created,
                            null,
                            "product added to cart"
                        )
                    )
                } catch (e: Exception) {
                    when (e) {
                        is NotFoundException -> call.respond(
                            ApiResponse.error(
                                HttpStatusCode.NotFound,
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
            get {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val employeeId = principal?.subject
                    call.respond(
                        ApiResponse.success(
                            HttpStatusCode.OK,
                            getProductsInCart(UUID.fromString(employeeId)),
                            null
                        )
                    )
                } catch (e: Exception) {
                    when (e) {
                        is NotFoundException -> call.respond(
                            ApiResponse.error(
                                HttpStatusCode.NotFound,
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
            delete("/{productId}") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val employeeId = principal?.subject
                    val productId = call.parameters["productId"]
                    removeProductFromCart(UUID.fromString(employeeId), UUID.fromString(productId))
                    call.respond(
                        ApiResponse.success(
                            HttpStatusCode.OK,
                            null,
                            "product removed from cart"
                        )
                    )
                } catch (e: Exception) {
                    when (e) {
                        is NotFoundException -> call.respond(
                            ApiResponse.error(
                                HttpStatusCode.NotFound,
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
            delete("/clear") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val employeeId = principal?.subject
                    clearCart(UUID.fromString(employeeId))
                    call.respond(
                        ApiResponse.success(
                            HttpStatusCode.OK,
                            null,
                            "cart cleared"
                        )
                    )
                } catch (e: Exception) {
                    when (e) {
                        is NotFoundException -> call.respond(
                            ApiResponse.error(
                                HttpStatusCode.NotFound,
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
    }
}