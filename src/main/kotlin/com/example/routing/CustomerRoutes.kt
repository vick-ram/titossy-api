package com.example.routing

import com.example.commons.*
import com.example.controllers.*
import com.example.models.CustomerRequest
import com.example.models.CustomerSignInData
import com.example.models.StatusUpdate
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.exceptions.ExposedSQLException

fun Route.customerRoutes(
    secret: String,
    issuer: String,
    audience: String
) {
    post<Customer.Auth.SignUp, CustomerRequest> { _, customerReg ->
        try {
            val customer = customerReg.validate()
            val newCustomer = createCustomer(customer, secret, this)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.Created,
                    newCustomer,
                    "Customer created successfully"
                )
            )

        } catch (e: Exception) {
            when (e) {
                is AlreadyExistsException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.Conflict,
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

    post<Customer.Auth.SignIn, CustomerSignInData> { _, customerCred ->
        val customer = customerCred.validate()
        try {
            val token = signInCustomer(customer, secret, issuer, audience, this)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    token,
                    "Signed in successfully"
                )
            )
        } catch (e: Exception) {
            when (e) {
                is NotFoundException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.Unauthorized,
                        e.message
                    )
                )

                is InvalidCredentials -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.Unauthorized,
                        e.message
                    )
                )

                is UnexpectedError -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.InternalServerError,
                        e.message
                    )
                )
            }
        }
    }

    authenticate("auth-jwt") {
        post("/api/customer/auth/sign_out") {
            val token = call.request.headers["Authorization"].toString().removePrefix("Bearer ")
            try {
                signOutCustomer(token, this)
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.NoContent,
                        null,
                        "Signed out successfully"
                    )
                )
            } catch (e: Exception) {
                when (e) {
                    is InvalidToken -> call.respond(
                        ApiResponse.error(
                            HttpStatusCode.BadRequest,
                            e.message
                        )
                    )

                    is TokenAlreadyBlacklisted -> call.respond(
                        ApiResponse.error(
                            HttpStatusCode.BadRequest,
                            e.message
                        )
                    )

                    is UnexpectedError -> call.respond(
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
        patch<Customer.ChangePassword, ChangePasswordRequest> { _, passwordChange ->
            try {
                forgotPassword(
                    passwordChange,
                    secret,
                    this
                )
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        null,
                        "Password changed successfully"
                    )
                )
            } catch (e: Exception) {
                when (e) {
                    is InvalidCredentials -> call.respond(
                        ApiResponse.error(
                            HttpStatusCode.BadRequest,
                            e.message
                        )
                    )

                    is ExposedSQLException -> call.respond(
                        ApiResponse.error(
                            HttpStatusCode.InternalServerError,
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

    get<Customer> { query ->
        when {
            query.email != null -> call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    filteredCustomers { it.email == query.email }?.firstOrNull(),
                    null
                )
            )

            query.status != null -> call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    filteredCustomers { it.status == ApprovalStatus.valueOf(query.status) },
                    null
                )
            )

            query.search != null -> call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    searchCustomer(query.search),
                    null
                )
            )

            else -> call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    filteredCustomers { true },
                    null
                )
            )
        }
    }

    get<Customer.Id> { param ->
        try {
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    filteredCustomers { it.id.value == param.id }?.firstOrNull(),
                    null
                )
            )
        } catch (e: Exception) {
            when (e) {
                is NoRecordFoundException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.NotFound,
                        e.message
                    )
                )

                is UnexpectedError -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.InternalServerError,
                        e.message
                    )
                )
            }
        }
    }

    authenticate("auth-jwt") {
        put<Customer.Id, CustomerRequest> { param, customerUpdate ->
            try {
                updateCustomer(param.id, customerUpdate.validate(), secret)
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        null,
                        "Customer updated successfully"
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

                    is FailedToCreate -> call.respond(
                        ApiResponse.error(
                            HttpStatusCode.Conflict,
                            e.message
                        )
                    )
                }
            }
        }
    }

    patch<Customer.Id, StatusUpdate> { param, status ->
        try {
            status.validate()
            updateCustomerStatus(param.id, status, this)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.Created,
                    "",
                    "Customer status updated successfully"
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

                is FailedToCreate -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.Conflict,
                        e.message
                    )
                )
            }
        }
    }

    delete<Customer.Id> { param ->
        try {
            deleteCustomer(param.id, this)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.NoContent,
                    null,
                    "Customer deleted successfully"
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

                is DeleteException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.BadRequest,
                        e.message
                    )
                )
            }
        }
    }

}