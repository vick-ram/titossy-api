package com.example.routing.supplier

import com.example.commands.queries.supplier.*
import com.example.exceptions.ApiResponse
import com.example.exceptions.InvalidCredentials
import com.example.exceptions.UnexpectedError
import com.example.models.supplier.SupplierRequest
import com.example.models.supplier.SupplierSignInData
import com.example.models.supplier.SupplierStatusUpdate
import com.example.models.util.ApprovalStatus
import com.example.routing.util.Supplier
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.supplierRoutes(
    secret: String,
    issuer: String,
    audience: String
) {
    post<Supplier.Auth.SignUp, SupplierRequest> { _, supplierReq ->
        val supplier = supplierReq.validate()
        try {
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.Created,
                    createSupplier(supplier, secret),
                    "Account created successfully"
                )
            )
        } catch (e: Exception) {
            call.respond(
                ApiResponse.error(
                    HttpStatusCode.Unauthorized,
                    e.message
                )
            )
        }
    }

    post<Supplier.Auth.SignIn, SupplierSignInData> { _, cred ->
        val supplier = cred.validate()
        try {
            val tokens = signInSupplier(supplier, secret, issuer, audience)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    tokens,
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
        post("/api/supplier/auth/sign_out") {
            val token = call.request.headers["Authorization"].toString()
            try {
                signOutSupplier(token)
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.NoContent,
                        null,
                        "Signed out successfully"
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

    get<Supplier> { query ->

        when {
            query.email != null -> call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    filteredSuppliers { it.email == query.email }?.firstOrNull(),
                    null
                )
            )

            query.status != null -> call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    filteredSuppliers { it.status == ApprovalStatus.valueOf(query.status) },
                    null
                )
            )

            query.search != null -> call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    searchSupplier(query.search),
                    null
                )
            )

            else -> call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    filteredSuppliers { true },
                    null
                )
            )
        }

    }
    get<Supplier.Id> { param ->
        try {
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    filteredSuppliers { it.id.value == param.id },
                    null
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

    patch<Supplier.Id, SupplierStatusUpdate> { param, statusUpdate ->
        try {
            updateSupplierStatus(param.id, statusUpdate)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    null,
                    "Supplier status updated successfully"
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
    //update all supplier statuses
    patch<Supplier.ApproveAll, SupplierStatusUpdate> { _, statusUpdate ->

        try {
            updateAllSuppliersStatus(statusUpdate)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    null,
                    "All suppliers approved successfully"
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

    put<Supplier.Id, SupplierRequest> { param, supplierUpdate ->
        val supplierUpdateRequest = supplierUpdate.validate()
        try {
            updateSupplier(param.id, supplierUpdateRequest, secret)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    null,
                    "Supplier updated successfully"
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

    delete<Supplier.Id> { param ->
        try {
            deleteSupplier(param.id)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.NoContent,
                    null,
                    "Supplier deleted successfully"
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