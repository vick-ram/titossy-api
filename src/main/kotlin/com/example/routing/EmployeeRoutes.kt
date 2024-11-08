package com.example.routing

import com.example.commons.*
import com.example.controllers.*
import com.example.models.EmployeeCredentials
import com.example.models.EmployeeRequest
import com.example.models.UpdateEmployeeAvailability
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import javax.security.auth.login.FailedLoginException

fun Route.employeeRoutes(
    secret: String,
    issuer: String,
    audience: String
) {
    post<Employee.Auth.SignUp, EmployeeRequest> { _, employeeRequest ->
        val employee = employeeRequest.validate()
        try {
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.Created,
                    createEmployee(employee, secret, this),
                    "Employee created successfully"
                )
            )
        } catch (e: Exception) {
            call.respond(
                ApiResponse.error(
                    HttpStatusCode.Conflict,
                    e.message
                )
            )
        }
    }

    post<Employee.Auth.SignIn, EmployeeCredentials> { _, cred ->
        val employee = cred.validate()
        try {
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    signInEmployee(employee, secret, issuer, audience, this),
                    "Signed in successfully"
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

                is InvalidCredentials -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.BadRequest,
                        e.message
                    )
                )

                is FailedLoginException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.InternalServerError,
                        e.message
                    )
                )
            }

        }
    }

    authenticate("auth-jwt") {
        post("/api/employee/auth/sign_out") {
            val token = call.request.headers["Authorization"].toString().removePrefix("Bearer ")
            try {
                signOutEmployee(token)
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        "",
                        "Signed out successfully"
                    )
                )
            } catch (e: Exception) {
                when (e) {

                    is TokenAlreadyBlacklisted -> call.respond(
                        ApiResponse.error(
                            HttpStatusCode.BadRequest,
                            e.message
                        )
                    )

                    is InvalidToken -> call.respond(
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
        patch<Employee.ChangePassword, ChangePasswordRequest> { _, passwordChange ->
            try {
                empForgotPassword(
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


    get<Employee> { query ->
        when {
            query.email != null -> call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    filteredEmployees { it.email == query.email }?.firstOrNull(),
                    "Employee fetched successfully"
                )
            )

            query.username != null -> call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    filteredEmployees { it.username == query.username }?.firstOrNull(),
                    null
                )
            )

            query.status != null -> call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    filteredEmployees { it.availability == Availability.valueOf(query.status) },
                    null
                )
            )

            query.role != null -> call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    filteredEmployees { it.role == Roles.valueOf(query.role) },
                    null
                )
            )

            query.search != null -> call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    searchEmployee(query.search),
                    null
                )
            )

            else -> call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    filteredEmployees { true },
                    null
                )
            )
        }
    }


    get<Employee.Id> { param ->
        try {
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    filteredEmployees { it.id.value == param.id }?.firstOrNull(),
                    "Employee fetched successfully"
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
        get("/api/employee/admin/{id}") {
            val id = call.parameters["id"] ?: return@get
            try {
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        getAdmin(id),
                        "Admin fetched successfully"
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
    }


    authenticate("auth-jwt") {
        put<Employee.Id, EmployeeRequest> { param, employeeUpdate ->
            val employee = employeeUpdate.validate()
            try {
                updateEmployee(param.id, employee, secret, this)
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        null,
                        "Employee updated successfully"
                    )
                )
            } catch (e: Exception) {
                when (e) {
                    is FailedToCreate -> call.respond(
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
    }

    authenticate("auth-jwt") {
        patch<Employee.Id, UpdateEmployeeAvailability> { param, updateEmployeeAvailability ->
            try {
                updateEmployeeAvailability(param.id, updateEmployeeAvailability)
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        "",
                        "Employee availability updated successfully"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    ApiResponse.error(
                        HttpStatusCode.Conflict,
                        e.message
                    )
                )
            }
        }
    }

    delete<Employee.Id> { param ->
        try {
            deleteEmployee(param.id)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.NoContent,
                    "",
                    "Employee deleted successfully"
                )
            )
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.Conflict,
                        e.message
                    )
                )
            }
        }
    }
}