package com.example.plugins.auth

import com.example.auth.Config.ISSUER
import com.example.auth.Config.REALM
import com.example.auth.Config.SECRET
import com.example.auth.makeJWTVerifier
import com.example.commands.queries.customer.filteredCustomers
import com.example.commands.queries.employee.filteredEmployees
import com.example.commands.queries.supplier.filteredSuppliers
import com.example.exceptions.AccountPendingException
import com.example.exceptions.ApiResponse
import com.example.exceptions.NoRecordFoundException
import com.example.models.customer.CustomerResponse
import com.example.models.supplier.SupplierResponse
import com.example.models.util.ApprovalStatus
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

val lastException = ThreadLocal<Throwable>()

fun Application.configureAuthentication() {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(makeJWTVerifier(ISSUER, SECRET))
            realm = REALM

            validate { credential ->
                try {
                    val emailClaim = credential.payload.getClaim("email").asString()
                    val usernameClaim = credential.payload.getClaim("username").asString()
                    val role = credential.payload.getClaim("role").asString()

                    emailClaim?.let { email ->
                        usernameClaim?.let { username ->

                            val customer =
                                filteredCustomers { it.email == email || it.username == username }?.firstOrNull()
                            val supplier =
                                filteredSuppliers { it.email == email }?.firstOrNull()
                            val employee =
                                filteredEmployees { it.email == email || it.username == username }?.firstOrNull()

                            when {
                                customer != null -> {
                                    val customerStatus = (customer as? CustomerResponse)?.status
                                    if (customerStatus == ApprovalStatus.APPROVED) {
                                        JWTPrincipal(credential.payload)
                                    } else {
                                        throw AccountPendingException("Your account is pending approval")
                                    }
                                }

                                supplier != null -> {
                                    val supplierStatus = (supplier as? SupplierResponse)?.status
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