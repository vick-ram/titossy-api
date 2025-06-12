package com.example.plugins

import com.example.commons.*
import com.example.controllers.filteredCustomers
import com.example.controllers.filteredEmployees
import com.example.controllers.filteredSuppliers
import com.example.models.CustomerResponse
import com.example.models.SupplierResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

val lastException = ThreadLocal<Throwable>()

fun Application.configureAuthentication(
    secret: String,
    issuer: String,
    audience: String,
    jwtRealm: String
) {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                makeJWTVerifier(
                    secret = secret,
                    issuer = issuer,
                    audience = audience
                )
            )

            validate { credential ->
                try {
                    val emailClaim = credential.payload.getClaim("email").asString()
                    val role = credential.payload.getClaim("role").asString()

                    emailClaim?.let { email ->

                        val customer = filteredCustomers { it.email == email }?.firstOrNull()
                        val supplier = filteredSuppliers { it.email == email }?.firstOrNull()
                        val employee = filteredEmployees { it.email == email }?.firstOrNull()

                        when {
                            customer != null -> {
                                val customerStatus = customer.status
                                if (customerStatus == ApprovalStatus.APPROVED) {
                                    JWTPrincipal(credential.payload)
                                } else {
                                    throw AccountPendingException("Your account is pending approval")
                                }
                            }

                            supplier != null -> {
                                val supplierStatus = supplier.status
                                if (supplierStatus == ApprovalStatus.APPROVED) {
                                    JWTPrincipal(credential.payload)
                                } else {
                                    throw AccountPendingException("Your account is pending approval")
                                }
                            }

                            employee != null && role != null -> {
                                JWTPrincipal(credential.payload)
                            }

                            else -> {
                                null
                            }
                        }
                    }
                } catch (e: Exception) {
                    lastException.set(e)
                    null
                }
            }

            challenge { _, _ ->
                when (val exception = lastException.get()) {
                    is AccountPendingException -> {
                        call.respond(
                            ApiResponse.error(
                                HttpStatusCode.Forbidden,
                                exception.message ?: "Your account is pending approval"
                            )
                        )
                    }

                    is NoRecordFoundException -> {
                        call.respond(
                            ApiResponse.error(
                                HttpStatusCode.BadRequest,
                                exception.message ?: "Email is missing in the JWT token"
                            )
                        )
                    }

                    else -> {
                        call.respond(
                            ApiResponse.error(
                                HttpStatusCode.Unauthorized,
                                "You need to sign in to access this route"
                            )
                        )
                    }
                }
                lastException.remove()
            }


        }
    }
}